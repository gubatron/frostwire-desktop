/*
 * Copyright (C) 2011 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gudy.azureus2.core3.util.protocol.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;

import sun.net.www.protocol.http.Handler;

/**
 * NOTE: Source code and comments taken from cling source code.
 * 
 * The SUNW morons restrict the JDK handlers to GET/POST/etc for "security" reasons.
 * They do not understand HTTP. This is the hilarious comment in their source:
 * <p/>
 * "This restriction will prevent people from using this class to experiment w/ new
 * HTTP methods using java.  But it should be placed for security - the request String
 * could be arbitrarily long."
 * 
 * @author Christian Bauer
 */
class HTTPURLConnection extends sun.net.www.protocol.http.HttpURLConnection {

    private static final String[] methods = {
            "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE",
            "SUBSCRIBE", "UNSUBSCRIBE", "NOTIFY"
    };

    protected HTTPURLConnection(URL u, Handler handler) throws IOException {
        super(u, handler);
    }

    public HTTPURLConnection(URL u, String host, int port) throws IOException {
        super(u, host, port);
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        OutputStream os;
        String savedMethod = method;
        // see if the method supports output
        if (method.equals("PUT") || method.equals("POST") || method.equals("NOTIFY")) {
            // fake the method so the superclass method sets its instance variables
            method = "PUT";
        } else {
            // use any method that doesn't support output, an exception will be
            // raised by the superclass
            method = "GET";
        }
        os = super.getOutputStream();
        method = savedMethod;
        return os;
    }

    public void setRequestMethod(String method) throws ProtocolException {
        if (connected) {
            throw new ProtocolException("Cannot reset method once connected");
        }
        for (String m : methods) {
            if (m.equals(method)) {
                this.method = method;
                return;
            }
        }
        throw new ProtocolException("Invalid UPnP HTTP method: " + method);
    }
}