/*
 * Sone - IdentityManager.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.wotns.freenet.wot;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.collection.SetBuilder;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.object.Default;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.wotns.freenet.plugin.PluginException;

/**
 * The identity manager takes care of loading and storing identities, their
 * contexts, and properties. It does so in a way that does not expose errors via
 * exceptions but it only logs them and tries to return sensible defaults.
 * <p>
 * It is also responsible for polling identities from the Web of Trust plugin
 * and notifying registered {@link IdentityListener}s when {@link Identity}s and
 * {@link OwnIdentity}s are discovered or disappearing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityManager extends AbstractService {

	/** Object used for synchronization. */
	private final Object syncObject = new Object() {
		/* inner class for better lock names. */
	};

	/** The logger. */
	private static final Logger logger = Logging.getLogger(IdentityManager.class);

	/** The event manager. */
	private final IdentityListenerManager identityListenerManager = new IdentityListenerManager();

	/** The Web of Trust connector. */
	private final WebOfTrustConnector webOfTrustConnector;

	/** The context to filter for. */
	private volatile String context;

	/** The currently known own identities. */
	/* synchronize access on syncObject. */
	private Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

	/** The currently trusted identities. */
	private Map<OwnIdentity, Collection<Identity>> currentTrustedIdentities = new HashMap<OwnIdentity, Collection<Identity>>();

	/**
	 * Creates a new identity manager.
	 *
	 * @param webOfTrustConnector
	 *            The Web of Trust connector
	 */
	public IdentityManager(WebOfTrustConnector webOfTrustConnector) {
		super("Sone Identity Manager", false);
		this.webOfTrustConnector = webOfTrustConnector;
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds a listener for identity events.
	 *
	 * @param identityListener
	 *            The listener to add
	 */
	public void addIdentityListener(IdentityListener identityListener) {
		identityListenerManager.addListener(identityListener);
	}

	/**
	 * Removes a listener for identity events.
	 *
	 * @param identityListener
	 *            The listener to remove
	 */
	public void removeIdentityListener(IdentityListener identityListener) {
		identityListenerManager.removeListener(identityListener);
	}

	//
	// ACCESSORS
	//

	/**
	 * Sets the context to filter own identities and trusted identities for.
	 *
	 * @param context
	 *            The context to filter for, or {@code null} to not filter
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Returns whether the Web of Trust plugin could be reached during the last
	 * try.
	 *
	 * @return {@code true} if the Web of Trust plugin is connected, {@code
	 *         false} otherwise
	 */
	public boolean isConnected() {
		try {
			webOfTrustConnector.ping();
			return true;
		} catch (PluginException pe1) {
			/* not connected, ignore. */
			return false;
		}
	}

	/**
	 * Returns the own identity with the given ID.
	 *
	 * @param id
	 *            The ID of the own identity
	 * @return The own identity, or {@code null} if there is no such identity
	 */
	public OwnIdentity getOwnIdentity(String id) {
		Set<OwnIdentity> allOwnIdentities = getAllOwnIdentities();
		for (OwnIdentity ownIdentity : allOwnIdentities) {
			if (ownIdentity.getId().equals(id)) {
				return ownIdentity;
			}
		}
		return null;
	}

	/**
	 * Returns all own identities.
	 *
	 * @return All own identities
	 */
	public Set<OwnIdentity> getAllOwnIdentities() {
		try {
			Set<OwnIdentity> ownIdentities = webOfTrustConnector.loadAllOwnIdentities();
			Map<String, OwnIdentity> newOwnIdentities = new HashMap<String, OwnIdentity>();
			for (OwnIdentity ownIdentity : ownIdentities) {
				newOwnIdentities.put(ownIdentity.getId(), ownIdentity);
			}
			checkOwnIdentities(newOwnIdentities);
			return ownIdentities;
		} catch (WebOfTrustException wote1) {
			logger.log(Level.WARNING, "Could not load all own identities!", wote1);
			return Collections.emptySet();
		}
	}

	public Set<Identity> getTrustedIdentities(OwnIdentity ownIdentity) {
		SetBuilder<Identity> identities = new SetBuilder<Identity>();
		if ((context == null) || ownIdentity.getContexts().contains(context)) {
			identities.add(ownIdentity);
		}
		synchronized (syncObject) {
			identities.addAll(Default.forNull(currentTrustedIdentities.get(ownIdentity), Collections.<Identity> emptySet()));
		}
		return identities.get();
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceRun() {
		while (!shouldStop()) {
			Map<OwnIdentity, Map<String, Identity>> currentIdentities = new HashMap<OwnIdentity, Map<String, Identity>>();
			@SuppressWarnings("hiding")
			Map<String, OwnIdentity> currentOwnIdentities = new HashMap<String, OwnIdentity>();

			Set<OwnIdentity> ownIdentities = null;
			boolean identitiesLoaded = false;
			try {
				/* get all identities with the wanted context from WoT. */
				ownIdentities = webOfTrustConnector.loadAllOwnIdentities();

				/* load trusted identities. */
				for (OwnIdentity ownIdentity : ownIdentities) {
					if ((context != null) && !ownIdentity.hasContext(context)) {
						continue;
					}
					currentOwnIdentities.put(ownIdentity.getId(), ownIdentity);

					Set<Identity> trustedIdentities = webOfTrustConnector.loadTrustedIdentities(ownIdentity, context);
					Map<String, Identity> identities = new HashMap<String, Identity>();
					currentIdentities.put(ownIdentity, identities);
					for (Identity identity : trustedIdentities) {
						identities.put(identity.getId(), identity);
					}

					/* add own identities, too, as long as the WoT doesn’t do that. */
					for (OwnIdentity additionalOwnIdentity : ownIdentities) {
						if (additionalOwnIdentity == ownIdentity) {
							continue;
						}
						if ((context != null) && !additionalOwnIdentity.hasContext(context)) {
							continue;
						}
						identities.put(additionalOwnIdentity.getId(), additionalOwnIdentity);
					}
				}
				identitiesLoaded = true;
			} catch (WebOfTrustException wote1) {
				logger.log(Level.WARNING, "WoT has disappeared!", wote1);
			}

			if (identitiesLoaded) {

				/* check for changes. */
				checkOwnIdentities(currentOwnIdentities);

				/* now check for changes in remote identities. */
				for (OwnIdentity ownIdentity : currentOwnIdentities.values()) {
					checkTrustedIdentities(ownIdentity, currentIdentities.get(ownIdentity));
				}
			}

			/* wait a minute before checking again. */
			sleep(60 * 1000);
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Checks the given new list of own identities for added or removed own
	 * identities, as compared to {@link #currentOwnIdentities}.
	 *
	 * @param newOwnIdentities
	 *            The new own identities
	 */
	private void checkOwnIdentities(Map<String, OwnIdentity> newOwnIdentities) {
		synchronized (syncObject) {

			/* find removed own identities: */
			for (OwnIdentity oldOwnIdentity : currentOwnIdentities.values()) {
				if (!newOwnIdentities.containsKey(oldOwnIdentity.getId())) {
					identityListenerManager.fireOwnIdentityRemoved(oldOwnIdentity);
				}
			}

			/* find added own identities. */
			for (OwnIdentity currentOwnIdentity : newOwnIdentities.values()) {
				if (!currentOwnIdentities.containsKey(currentOwnIdentity.getId())) {
					identityListenerManager.fireOwnIdentityAdded(currentOwnIdentity);
				}
			}

			currentOwnIdentities.clear();
			currentOwnIdentities.putAll(newOwnIdentities);
		}
	}

	/**
	 * Checks the given identities for changes since the last check.
	 *
	 * @param ownIdentity
	 *            The own identity trusting the given identities
	 * @param trustedIdentities
	 *            The trusted identities
	 */
	private void checkTrustedIdentities(OwnIdentity ownIdentity, Map<String, Identity> trustedIdentities) {

		@SuppressWarnings("hiding")
		Map<String, Identity> currentTrustedIdentities = new HashMap<String, Identity>();
		synchronized (syncObject) {
			if (this.currentTrustedIdentities.containsKey(ownIdentity)) {
				for (Identity identity : this.currentTrustedIdentities.get(ownIdentity)) {
					currentTrustedIdentities.put(identity.getId(), identity);
				}
			}
		}

		/* find new identities. */
		for (Identity currentIdentity : trustedIdentities.values()) {
			if (!currentTrustedIdentities.containsKey(currentIdentity.getId())) {
				identityListenerManager.fireIdentityAdded(ownIdentity, currentIdentity);
			}
		}

		/* find removed identities. */
		for (Identity oldIdentity : currentTrustedIdentities.values()) {
			if (!trustedIdentities.containsKey(oldIdentity.getId())) {
				identityListenerManager.fireIdentityRemoved(ownIdentity, oldIdentity);
			}
		}

		/* check for changes in the contexts. */
		for (Identity oldIdentity : currentTrustedIdentities.values()) {
			if (!trustedIdentities.containsKey(oldIdentity.getId())) {
				continue;
			}
			Identity newIdentity = trustedIdentities.get(oldIdentity.getId());
			Set<String> oldContexts = oldIdentity.getContexts();
			Set<String> newContexts = newIdentity.getContexts();
			if (oldContexts.size() != newContexts.size()) {
				identityListenerManager.fireIdentityUpdated(ownIdentity, newIdentity);
				continue;
			}
			boolean changed = false;
			for (String oldContext : oldContexts) {
				if (!newContexts.contains(oldContext)) {
					identityListenerManager.fireIdentityUpdated(ownIdentity, newIdentity);
					changed = true;
					break;
				}
			}
			if (changed) {
				continue;
			}
			Map<String, String> oldProperties = oldIdentity.getProperties();
			Map<String, String> newProperties = newIdentity.getProperties();
			if (oldProperties.size() != newProperties.size()) {
				identityListenerManager.fireIdentityUpdated(ownIdentity, newIdentity);
				continue;
			}
			for (Entry<String, String> oldProperty : oldProperties.entrySet()) {
				if (!newProperties.containsKey(oldProperty.getKey()) || !newProperties.get(oldProperty.getKey()).equals(oldProperty.getValue())) {
					identityListenerManager.fireIdentityUpdated(ownIdentity, newIdentity);
					break;
				}
			}
		}

		synchronized (syncObject) {
			this.currentTrustedIdentities.put(ownIdentity, trustedIdentities.values());
		}

	}

}
