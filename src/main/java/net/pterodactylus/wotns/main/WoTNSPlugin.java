/*
 * WoTNS - WoTNSPlugin.java - Copyright © 2011 David Roden
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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.logging.LoggingListener;
import net.pterodactylus.util.version.Version;
import net.pterodactylus.wotns.freenet.plugin.PluginConnector;
import net.pterodactylus.wotns.freenet.wot.IdentityManager;
import net.pterodactylus.wotns.freenet.wot.WebOfTrustConnector;
import net.pterodactylus.wotns.ui.web.WebInterface;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.ToadletContainer;
import freenet.l10n.PluginL10n;
import freenet.l10n.BaseL10n.LANGUAGE;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.FredPluginBaseL10n;
import freenet.pluginmanager.FredPluginL10n;
import freenet.pluginmanager.FredPluginThreadless;
import freenet.pluginmanager.FredPluginVersioned;
import freenet.pluginmanager.PluginRespirator;

/**
 * Main plugin class for Web of Trust Name Service.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WoTNSPlugin implements FredPlugin, FredPluginL10n, FredPluginBaseL10n, FredPluginVersioned, FredPluginThreadless {

	static {
		Logging.setup("WoTNS");
		Logging.addLoggingListener(new LoggingListener() {

			@Override
			public void logged(LogRecord logRecord) {
				Class<?> loggerClass = Logging.getLoggerClass(logRecord.getLoggerName());
				int recordLevel = logRecord.getLevel().intValue();
				if (recordLevel < Level.FINE.intValue()) {
					freenet.support.Logger.debug(loggerClass, String.format(logRecord.getMessage(), logRecord.getParameters()), logRecord.getThrown());
				} else if (recordLevel < Level.INFO.intValue()) {
					freenet.support.Logger.minor(loggerClass, String.format(logRecord.getMessage(), logRecord.getParameters()), logRecord.getThrown());
				} else if (recordLevel < Level.WARNING.intValue()) {
					freenet.support.Logger.normal(loggerClass, String.format(logRecord.getMessage(), logRecord.getParameters()), logRecord.getThrown());
				} else if (recordLevel < Level.SEVERE.intValue()) {
					freenet.support.Logger.warning(loggerClass, String.format(logRecord.getMessage(), logRecord.getParameters()), logRecord.getThrown());
				} else {
					freenet.support.Logger.error(loggerClass, String.format(logRecord.getMessage(), logRecord.getParameters()), logRecord.getThrown());
				}
			}
		});
	}

	/** The current version of the plugin. */
	private static final Version VERSION = new Version(0, 0, 8);

	/** The plugin respirator. */
	private PluginRespirator pluginRespirator;

	/** The l10n handler. */
	private PluginL10n l10n;

	/** The web interface. */
	private WebInterface webInterface;

	/** The resolver. */
	private Resolver resolver;

	/** The web of trust connector. */
	private WebOfTrustConnector webOfTrustConnector;

	/** The identity manager. */
	private IdentityManager identityManager;

	//
	// ACCESSORS
	//

	/**
	 * Returns the high-level simple client for the node.
	 *
	 * @return The high-level simple client
	 */
	public HighLevelSimpleClient getHighLevelSimpleClient() {
		return pluginRespirator.getHLSimpleClient();
	}

	/**
	 * Returns the toadlet container of the node.
	 *
	 * @return The toadlet container of the node
	 */
	public ToadletContainer getToadletContainer() {
		return pluginRespirator.getToadletContainer();
	}

	/**
	 * Returns the identity manager.
	 *
	 * @return The identity manager
	 */
	public IdentityManager getIdentityManager() {
		return identityManager;
	}

	/**
	 * Returns the resolver.
	 *
	 * @return The resolver
	 */
	public Resolver getResolver() {
		return resolver;
	}

	//
	// FREDPLUGIN METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runPlugin(PluginRespirator pluginRespirator) {
		this.pluginRespirator = pluginRespirator;

		PluginConnector pluginConnector = new PluginConnector(pluginRespirator);
		webOfTrustConnector = new WebOfTrustConnector(pluginConnector);
		identityManager = new IdentityManager(webOfTrustConnector);
		identityManager.setContext("WoTNS");
		identityManager.start();

		resolver = new Resolver(identityManager);

		webInterface = new WebInterface(this);

		webInterface.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		identityManager.stop();
		webOfTrustConnector.stop();
		webInterface.stop();
		Logging.shutdown();
	}

	//
	// FREDPLUGINL10N METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString(String key) {
		return l10n.getBase().getString(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLanguage(LANGUAGE newLanguage) {
		l10n = new PluginL10n(this, newLanguage);
	}

	//
	// FREDPLUGINBASEL10N METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nFilesBasePath() {
		return "i18n";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nFilesMask() {
		return "WoTNS.${lang}.properties";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nOverrideFilesMask() {
		return "WoTNS.${lang}.override.properties";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassLoader getPluginClassLoader() {
		return WoTNSPlugin.class.getClassLoader();
	}

	//
	// FREDPLUGINVERSIONED METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVersion() {
		return VERSION.toString();
	}

}
