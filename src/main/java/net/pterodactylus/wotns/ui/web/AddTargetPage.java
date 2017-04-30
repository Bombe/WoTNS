/*
 * WoTNS - AddTargetPage.java - Copyright © 2011–2017 David Roden
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

import java.net.MalformedURLException;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
import net.pterodactylus.wotns.freenet.wot.WebOfTrustException;
import net.pterodactylus.wotns.web.FreenetRequest;
import freenet.keys.FreenetURI;


/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AddTargetPage extends BasicPage {

	/**
	 * @param webInterface
	 * @param path
	 * @param template
	 */
	public AddTargetPage(Template template, WebInterface webInterface) {
		super(webInterface, "addTarget.html", null, template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			String ownIdentityId = request.getHttpRequest().getPartAsStringFailsafe("ownIdentity", 43);
			OwnIdentity ownIdentity = identityManager.getOwnIdentity(ownIdentityId);
			if (ownIdentity == null) {
				/* TODO - show error. */
				return;
			}
			String name = request.getHttpRequest().getPartAsStringFailsafe("name", 64).trim();
			if (name.length() == 0) {
				/* TODO - show error. */
				return;
			}
			String target = request.getHttpRequest().getPartAsStringFailsafe("target", 256).trim();
			if (name.length() == 0) {
				/* TODO - show error. */
				return;
			}
			try {
				new FreenetURI(target);
				ownIdentity.setProperty("tns." + name, target);
				throw new RedirectException("manage.html?ownIdentity=" + ownIdentityId);
			} catch (MalformedURLException mue1) {
				/* TODO - show error. */
			} catch (WebOfTrustException wote1) {
				/* TODO - show error. */
			}
		}
	}

}
