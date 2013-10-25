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

package org.gudy.azureus2.core3.util.protocol.http;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class Handler extends sun.net.www.protocol.http.Handler {
    protected URLConnection openConnection(URL u) throws IOException {
        return openConnection(u, null);
    }

    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        return new HTTPURLConnection(u, this);
    }
}
