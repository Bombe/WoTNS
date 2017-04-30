/*
 * WoTNS - EnableIdentityPage.java - Copyright © 2011–2017 David Roden
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
import net.pterodactylus.wotns.freenet.wot.WebOfTrustException;
import net.pterodactylus.wotns.web.FreenetRequest;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class EnableIdentityPage extends BasicPage {

	/**
	 * @param webInterface
	 * @param path
	 * @param template
	 */
	public EnableIdentityPage(Template template, WebInterface webInterface) {
		super(webInterface, "enableIdentity.html", null, template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			OwnIdentity ownIdentity = getOwnIdentity(request);
			if (ownIdentity == null) {
				/* TODO - show error. */
				return;
			}
			try {
				if (request.getHttpRequest().isPartSet("enable")) {
					ownIdentity.addContext("WoTNS");
				} else if (request.getHttpRequest().isPartSet("disable")) {
					ownIdentity.removeContext("WoTNS");
				}
				throw new RedirectException("index.html");
			} catch (WebOfTrustException wote1) {
				/* TODO - show error. */
			}
		}
	}

}
