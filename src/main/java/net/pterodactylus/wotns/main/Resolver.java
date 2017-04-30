/*
 * WoTNS - Resolver.java - Copyright © 2011–2017 David Roden
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

package net.pterodactylus.wotns.main;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.object.Default;
import net.pterodactylus.wotns.freenet.wot.Identity;
import net.pterodactylus.wotns.freenet.wot.IdentityManager;
import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
import net.pterodactylus.wotns.freenet.wot.Trust;
import freenet.keys.FreenetURI;

/**
 * Resolves short names as given by the user.
 * <p>
 * Short names generally have the syntax:
 *
 * <pre>
 * identity [ ‘@’ start-of-key ] ‘/’ target [ ‘/’ file-path ]
 * </pre>
 * <p>
 * Because resolving a short name is based on the <i>web</i> of trust, the ID of
 * an own identity must be given in order to find the entry point into the web
 * of trust. If no ID is specified, the ID of a random own identity is used. If
 * no own identity exists, short names can not be resolved.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Resolver {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(Resolver.class);

	/** The identity manager. */
	private final IdentityManager identityManager;

	/** The ID of the own identity to use for resolving. */
	private String ownIdentityId;

	/**
	 * Creates a new resolver.
	 *
	 * @param identityManager
	 *            The identity manager to use
	 */
	public Resolver(IdentityManager identityManager) {
		this.identityManager = identityManager;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the ID of the own identity used for resolving short names.
	 *
	 * @return The ID of the own identity used for resolving
	 */
	public String getOwnIdentityId() {
		return ownIdentityId;
	}

	/**
	 * Sets the ID of the own identity used for resolving short names.
	 *
	 * @param ownIdentityId
	 *            The ID of the own identity used for resolving
	 */
	public void setOwnIdentityId(String ownIdentityId) {
		this.ownIdentityId = ownIdentityId;
	}

	//
	// ACTIONS
	//

	/**
	 * Resolves a short name.
	 *
	 * @param shortUri
	 *            The short name to resolve
	 * @return The Freenet URI the short name resolves to, or {@code null} if
	 *         the short name can not be resolved
	 * @throws MalformedURLException
	 *             if the short name is malformed
	 */
	public FreenetURI resolveURI(String shortUri) throws MalformedURLException {
		int firstSlash = shortUri.indexOf('/');
		if (firstSlash == -1) {
			throw new MalformedURLException("At least one slash is required.");
		}
		String shortName = shortUri.substring(0, firstSlash);
		String target = shortUri.substring(firstSlash + 1);
		Identity identity = locateIdentity(shortName);
		System.out.println("located identity: " + identity);
		if (identity == null) {
			return null;
		}
		return new FreenetURI(identity.getProperty("tns." + target));
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Locates the identity specified by the given short name. If more than one
	 * identity matches the given pattern, the one with the highest trust is
	 * used. When calculating the trust, local and remote trust are treated
	 * equally, i.e. the higher value of either one is used.
	 *
	 * @param shortName
	 *            The short name to locate an identity for
	 * @return The located identity, or {@code null} if no identity can be
	 *         found, or if no own identity is found to use for locating an
	 *         identity
	 */
	private Identity locateIdentity(String shortName) {
		int atSign = shortName.indexOf('@');
		String identityName = shortName;
		String keyStart = "";
		if (atSign > -1) {
			identityName = shortName.substring(0, atSign);
			keyStart = shortName.substring(atSign + 1);
		}
		final OwnIdentity ownIdentity;
		if (this.ownIdentityId != null) {
			if (identityManager.getOwnIdentity(this.ownIdentityId) != null) {
				ownIdentity = identityManager.getOwnIdentity(this.ownIdentityId);
			} else {
				ownIdentity = getFirstOwnIdentity();
			}
		} else {
			ownIdentity = getFirstOwnIdentity();
		}
		if (ownIdentity == null) {
			logger.log(Level.SEVERE, "Can not resolve “" + shortName + "” without a Web of Trust Identity!");
			return null;
		}
		System.out.println("using own identity " + ownIdentity + " to resolve " + shortName);
		Set<Identity> trustedIdentities = Default.forNull(identityManager.getTrustedIdentities(ownIdentity), Collections.<Identity> emptySet());
		List<Identity> matchingIdentities = new ArrayList<Identity>();
		System.out.println("checking " + trustedIdentities);
		for (Identity identity : trustedIdentities) {
			if (identity.getNickname().equals(identityName) && identity.getId().startsWith(keyStart)) {
				matchingIdentities.add(identity);
			}
		}
		if (matchingIdentities.isEmpty()) {
			return null;
		}
		Collections.sort(matchingIdentities, new Comparator<Identity>() {

			@Override
			public int compare(Identity leftIdentity, Identity rightIdentity) {
				Trust leftTrust = leftIdentity.getTrust(ownIdentity);
				Trust rightTrust = rightIdentity.getTrust(ownIdentity);
				int leftTrustCombined = ((leftTrust.getExplicit() != null) ? leftTrust.getExplicit() : 0) + ((leftTrust.getImplicit() != null) ? leftTrust.getImplicit() : 0);
				int rightTrustCombined = ((rightTrust.getExplicit() != null) ? rightTrust.getExplicit() : 0) + ((rightTrust.getImplicit() != null) ? rightTrust.getImplicit() : 0);
				return leftTrustCombined - rightTrustCombined;
			}
		});
		return matchingIdentities.get(0);
	}

	/**
	 * Returns a random own identity from the web of trust.
	 *
	 * @return A random own identity from the web of trust, or {@code null} if
	 *         the web of trust does not have any own identities
	 */
	private OwnIdentity getFirstOwnIdentity() {
		Set<OwnIdentity> ownIdentities = identityManager.getAllOwnIdentities();
		if (!ownIdentities.isEmpty()) {
			return ownIdentities.iterator().next();
		}
		return null;
	}

}
