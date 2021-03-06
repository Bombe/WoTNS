/*
 * WoTNS - IndexPage.java - Copyright © 2011 David Roden
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.wotns.freenet.wot.OwnIdentity;
import net.pterodactylus.wotns.web.FreenetRequest;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IndexPage extends BasicPage {

	/**
	 * @param path
	 * @param contentType
	 * @param templateContextFactory
	 * @param template
	 */
	public IndexPage(Template template, WebInterface webInterface) {
		super(webInterface, "index.html", "Web of Trust Name Service", template);
	}

	//
	// FREENETTEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);

		Set<OwnIdentity> ownIdentities = identityManager.getAllOwnIdentities();
		List<OwnIdentity> enabledIdentities = new ArrayList<OwnIdentity>();
		List<OwnIdentity> disabledIdentities = new ArrayList<OwnIdentity>();
		for (OwnIdentity ownIdentity : ownIdentities) {
			if (ownIdentity.hasContext("WoTNS")) {
				enabledIdentities.add(ownIdentity);
			} else {
				disabledIdentities.add(ownIdentity);
			}
		}
		templateContext.set("enabledIdentities", enabledIdentities);
		templateContext.set("disabledIdentities", disabledIdentities);

	}

}
