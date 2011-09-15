/*
 * WoTNS - EditTargetPage.java - Copyright © 2011 David Roden
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
public class EditTargetPage extends BasicPage {

	/**
	 * @param webInterface
	 * @param path
	 * @param template
	 */
	public EditTargetPage(Template template, WebInterface webInterface) {
		super(webInterface, "editTarget.html", null, template);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		if (request.getMethod() == Method.POST) {
			OwnIdentity ownIdentity = getOwnIdentity(request);
			String name = request.getHttpRequest().getPartAsStringFailsafe("name", 64).trim();
			String target = request.getHttpRequest().getPartAsStringFailsafe("target", 256).trim();
			if ((name.length() == 0) || (target.length() == 0)) {
				/* TODO - show error. */
				return;
			}
			try {
				new FreenetURI(target);
			} catch (MalformedURLException mue1) {
				/* TODO - show error. */
				return;
			}
			try {
				if (request.getHttpRequest().getPartAsStringFailsafe("edit", 4).equals("true")) {
					System.out.println("edit");
					ownIdentity.setProperty("tns." + name, target);
				} else if (request.getHttpRequest().getPartAsStringFailsafe("delete", 4).equals("true")) {
					System.out.println("delete");
					ownIdentity.removeProperty("tns." + name);
				}
				throw new RedirectException("manage.html?ownIdentity=" + ownIdentity.getId());
			} catch (WebOfTrustException wote1) {
				/* TODO - show error. */
			}
		}
	}

}
