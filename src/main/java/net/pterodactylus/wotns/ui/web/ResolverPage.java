/*
 * WoTNS - ResolverPage.java - Copyright © 2011 David Roden
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
import net.pterodactylus.wotns.main.Resolver;
import net.pterodactylus.wotns.web.FreenetRequest;
import freenet.keys.FreenetURI;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ResolverPage extends BasicPage {

	private final Resolver resolver;

	public ResolverPage(Template unknownNameTemplate, WebInterface webInterface, Resolver resolver) {
		super(webInterface, "", unknownNameTemplate);
		this.resolver = resolver;
	}

	//
	// PAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrefixPage() {
		return true;
	}

	//
	// FREENETTEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		String uri = request.getUri().getPath();
		String path = uri.substring(uri.indexOf('/', 1) + 1);
		FreenetURI targetUri;
		try {
			targetUri = resolver.resolveURI(path);
			if (targetUri != null) {
				throw new RedirectException("/" + targetUri.toString());
			}
		} catch (MalformedURLException mue1) {
			/* TODO - do something. */
		}
		templateContext.set("shortName", path);
	}

}
