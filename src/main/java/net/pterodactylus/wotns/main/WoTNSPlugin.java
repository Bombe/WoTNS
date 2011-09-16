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

import net.pterodactylus.util.logging.Logging;
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
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WoTNSPlugin implements FredPlugin, FredPluginL10n, FredPluginBaseL10n, FredPluginVersioned, FredPluginThreadless {

	static {
		Logging.setup("WoTNS");
		Logging.setupConsoleLogging();
	}

	private static final Version VERSION = new Version(0, 0, 3);

	private PluginRespirator pluginRespirator;

	private PluginL10n l10n;

	private WebInterface webInterface;

	private Resolver resolver;

	private WebOfTrustConnector webOfTrustConnector;

	private IdentityManager identityManager;

	//
	// ACCESSORS
	//

	public HighLevelSimpleClient getHighLevelSimpleClient() {
		return pluginRespirator.getHLSimpleClient();
	}

	public ToadletContainer getToadletContainer() {
		return pluginRespirator.getToadletContainer();
	}

	public IdentityManager getIdentityManager() {
		return identityManager;
	}

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
		resolver.setOwnIdentityId("e3myoFyp5avg6WYN16ImHri6J7Nj8980Fm~aQe4EX1U");

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
