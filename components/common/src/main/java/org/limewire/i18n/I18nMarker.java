/*
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

package org.limewire.i18n;

/**
 * Class to mark messages for translation.
 * 
 * The external xgettext tool picks up occurrences of {@link #marktr(String)}
 * and extracts the string literal arguments into a template file for 
 * translation.
 */
public class I18nMarker {

    /**
     * Marks the string <code>text</code> to be translated but does not translate it.
     * @return the argument.
     */
    public static String marktr(String text) {
        return text;
    }

}
