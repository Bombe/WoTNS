/*
 * WoTNS - ManagePage.java - Copyright © 2011 David Roden
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

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
import net.pterodactylus.wotns.web.FreenetRequest;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ManagePage extends BasicPage {

	/**
	 * @param webInterface
	 * @param path
	 * @param template
	 */
	public ManagePage(Template template, WebInterface webInterface) {
		super(webInterface, "manage.html", "Manage - Web of Trust Name Service", template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
		}
		String ownIdentityId = request.getHttpRequest().getParam("ownIdentity");
		OwnIdentity ownIdentity = webInterface.getWoTNSPlugin().getIdentityManager().getOwnIdentity(ownIdentityId);
		templateContext.set("ownIdentity", ownIdentity);
	}

}
