/*
 * WoTNS - IdentityComparator.java - Copyright © 2011 David Roden
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

import java.util.Comparator;

import net.pterodactylus.wotns.freenet.wot.Identity;

/**
 * Contains several comparators that can be used with {@link Identity}s. At the
 * moment only a single {@link Comparator} is defined; it sorts identities
 * case-insensitively by name.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityComparator {

	/** Comparator for sorting by name. */
	@SuppressWarnings("synthetic-access")
	public static final Comparator<Identity> NAME = new IdentityNameComparator();

	/**
	 * {@link Comparator} for {@link Identity}s that sorts case-insensitively by
	 * name.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class IdentityNameComparator implements Comparator<Identity> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Identity leftIdentity, Identity rightIdentity) {
			return leftIdentity.getNickname().compareToIgnoreCase(rightIdentity.getNickname());
		}

	}

}
