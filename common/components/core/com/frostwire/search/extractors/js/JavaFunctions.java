/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
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

package com.frostwire.search.extractors.js;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 *
 */
final class JavaFunctions {

    private JavaFunctions() {
    }

    public static boolean isdigit(String str) {
        if (str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isalpha(String str) {
        if (str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetter(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static Object[] list(String str) {
        Object[] r = new Object[str.length()];

        for (int i = 0; i < str.length(); i++) {
            r[i] = str.charAt(i);
        }

        return r;
    }

    public static String join(Object[] arr) {
        StringBuilder sb = new StringBuilder();

        for (Object obj : arr) {
            sb.append(obj.toString());
        }

        return sb.toString();
    }

    public static Integer len(Object obj) {

        if (obj instanceof Object[]) {
            return ((Object[]) obj).length;
        }

        if (obj instanceof String) {
            return ((String) obj).length();
        }

        throw new IllegalArgumentException("Not supported type");
    }

    public static Object reverse(Object obj) {

        if (obj instanceof Object[]) {
            List<Object> list = new ArrayList<Object>();
            list.addAll(Arrays.asList((Object[]) obj));
            Collections.reverse(list);
            return list.toArray();
        }

        if (obj instanceof String) {
            return new StringBuilder((String) obj).reverse().toString();
        }

        throw new IllegalArgumentException("Not supported type");
    }

    public static Object splice(Object obj, int fromIndex) {

        if (obj instanceof Object[]) {
            return Arrays.asList((Object[]) obj).subList(fromIndex, ((Object[]) obj).length).toArray();
        }

        if (obj instanceof String) {
            return ((String) obj).substring(fromIndex);
        }

        throw new IllegalArgumentException("Not supported type");
    }
}
