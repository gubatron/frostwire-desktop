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

package com.frostwire.vuze;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This class is public until the a refactor is done.
 * 
 * @author gubatron
 * @author aldenml
 *
 */
public final class EmptyResourceBundle extends ResourceBundle {

    @Override
    public Enumeration<String> getKeys() {
        return new Vector<String>().elements();
    }

    @Override
    protected Object handleGetObject(String key) {
        return null;
    }
}
