/*
 * $HeadURL: https://svn.apache.org/repos/asf/httpcomponents/httpcore/tags/4.0.1/httpcore-nio/src/main/java/org/apache/http/impl/nio/reactor/SessionHandle.java $
 * $Revision: 744539 $
 * $Date: 2009-02-14 18:23:26 +0100 (Sat, 14 Feb 2009) $
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.nio.reactor;

import org.apache.http.nio.reactor.IOSession;

/**
 * Session handle class used by I/O reactor implementations to keep a reference 
 * to a {@link IOSession} along with information about time of last I/O 
 * operations on that session.
 *
 * @since 4.0
 */
public class SessionHandle {

    private final IOSession session;
    private final long startedTime;

    private long lastReadTime;
    private long lastWriteTime;
    private long lastAccessTime;

    public SessionHandle(final IOSession session) {
        super();
        if (session == null) {
            throw new IllegalArgumentException("Session may not be null");
        }
        this.session = session;
        long now = System.currentTimeMillis();
        this.startedTime = now;
        this.lastReadTime = now;
        this.lastWriteTime = now;
        this.lastAccessTime = now;
    }

    public IOSession getSession() {
        return this.session;
    }

    public long getStartedTime() {
        return this.startedTime;
    }

    public long getLastReadTime() {
        return this.lastReadTime;
    }

    public long getLastWriteTime() {
        return this.lastWriteTime;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public void resetLastRead() {
        long now = System.currentTimeMillis();
        this.lastReadTime = now;
        this.lastAccessTime = now;
    }

    public void resetLastWrite() {
        long now = System.currentTimeMillis();
        this.lastWriteTime = now;
        this.lastAccessTime = now;
    }

}
