/*
 * WoTNS - BasicPage.java - Copyright © 2011 David Roden
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

package net.pterodactylus.wotns.ui.web;

import java.util.Arrays;
import java.util.Collection;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.wotns.freenet.wot.IdentityManager;
import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
import net.pterodactylus.wotns.web.FreenetRequest;
import net.pterodactylus.wotns.web.FreenetTemplatePage;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class BasicPage extends FreenetTemplatePage {

	protected final WebInterface webInterface;

	protected final IdentityManager identityManager;

	private final String title;

	public BasicPage(WebInterface webInterface, String path, String title, Template template) {
		super(path, webInterface.getTemplateContextFactory(), template, "noPermission.html");
		this.webInterface = webInterface;
		this.identityManager = webInterface.getWoTNSPlugin().getIdentityManager();
		this.title = title;
	}

	//
	// PROTECTED METHODS
	//

	protected OwnIdentity getOwnIdentity(FreenetRequest request) {
		if (request.getMethod() == Method.POST) {
			String ownIdentityId = request.getHttpRequest().getPartAsStringFailsafe("ownIdentity", 43);
			return identityManager.getOwnIdentity(ownIdentityId);
		} else if (request.getMethod() == Method.GET) {
			String ownIdentityId = request.getHttpRequest().getParam("ownIdentity");
			return identityManager.getOwnIdentity(ownIdentityId);
		}
		return null;
	}

	//
	// FREENETTEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPageTitle(FreenetRequest request) {
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<String> getStyleSheets() {
		return Arrays.asList("css/main.css");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		templateContext.set("request", request.getHttpRequest());
		templateContext.set("ownIdentities", identityManager.getAllOwnIdentities());
		templateContext.set("formPassword", webInterface.getWoTNSPlugin().getToadletContainer().getFormPassword());
	}

}
