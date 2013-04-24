/*
 * Created on Feb 28, 2012
 * Created by Paul Gardner
 * 
 * Copyright 2012 Vuze, Inc.  All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License only.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package com.frostwire.torrent;

import java.util.AbstractMap;
import java.util.HashMap;

public class HashMapEx extends HashMap<String, Object> {

    public static final byte FL_MAP_ORDER_INCORRECT = 0x01;

    private byte flags;

    public HashMapEx(AbstractMap<String, Object> m) {
        super(m);
    }

    public void setFlag(byte flag, boolean set) {
        if (set) {

            flags |= flag;

        } else {

            flags &= ~flag;
        }
    }

    public boolean getFlag(byte flag) {
        return ((flags & flag) != 0);
    }
}
