/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
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
package com.frostwire.gui.library;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.HttpFetcher;
import com.frostwire.JsonEngine;

public class Device {

    public static int ACTION_BROWSE = 0;
    public static int ACTION_DOWNLOAD = 1;
    public static int ACTION_UPLOAD = 2;

    private static JsonEngine JSON_ENGINE = new JsonEngine();

    private InetAddress _address;
    private int _port;
    private Finger finger;
    private boolean _tokenAuthorized;
    private OnActionFailedListener _listener;
    private long timestamp;

    public Device(InetAddress address, int port, Finger finger) {
        this._address = address;
        this._port = port;
        this.finger = finger;
    }

    public InetAddress getAddress() {
        return _address;
    }

    public void setAddress(InetAddress address) {
        _address = address;
    }

    public int getPort() {
        return _port;
    }

    public void setPort(int port) {
        _port = port;
    }

    public Finger getFinger() {
        return finger;
    }

    public void setFinger(Finger finger) {
        this.finger = finger;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return finger.nickname;
    }

    public String getKey() {
        return _address.getHostAddress() + ":" + _port;
    }

    public int getTotalShared() {
        return finger.numSharedApplicationFiles + finger.numSharedDocumentFiles + finger.numSharedPictureFiles + finger.numSharedVideoFiles + finger.numSharedRingtoneFiles + finger.numSharedAudioFiles;
    }

    public boolean isTokenAuthorized() {
        return _tokenAuthorized;
    }

    public void setTokenAuthorized(boolean authorized) {
        _tokenAuthorized = authorized;
    }

    public OnActionFailedListener getOnActionFailedListener() {
        return _listener;
    }

    public void setOnActionFailedListener(OnActionFailedListener listener) {
        _listener = listener;
    }

    public List<FileDescriptor> browse(byte fileType) {

        try {

            URI uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/browse?type=" + fileType);

            HttpFetcher fetcher = new HttpFetcher(uri, 2000); // 2 seconds http timeout

            byte[] jsonBytes = (byte[]) fetcher.fetch(true)[0];

            if (jsonBytes == null) {
                notifyOnActionFailed(ACTION_BROWSE, null);
                return new ArrayList<FileDescriptor>();
            }

            setTimestamp(System.currentTimeMillis());

            String json = new String(jsonBytes);

            FileDescriptorList list = JSON_ENGINE.toObject(json, FileDescriptorList.class);

            return list.files;

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_BROWSE, e);
        }

        return new ArrayList<FileDescriptor>();
    }

    public URL getDownloadURL(int type, int id) {
        try {

            return new URL("http://" + _address.getHostAddress() + ":" + _port + "/download?type=" + type + "&id=" + id);

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_DOWNLOAD, e);
        }

        return null;
    }
    
    public String getDownloadURL(FileDescriptor fd) {
        return "http://" + _address.getHostAddress() + ":" + _port + "/download?type=" + fd.fileType + "&id=" + fd.id;
    }

    public byte[] download(int type, int id) {

        try {

            URI uri = getDownloadURL(type, id).toURI();

            HttpFetcher fetcher = new HttpFetcher(uri);

            byte[] data = fetcher.fetch();

            if (data == null) {
                notifyOnActionFailed(ACTION_DOWNLOAD, null);
                return null;
            }

            setTimestamp(System.currentTimeMillis());

            return data;

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_DOWNLOAD, e);
        }

        return null;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Device)) {
            return false;
        }

        return hashCode() == ((Device) obj).hashCode();
    }

    @Override
    public String toString() {
        return _address + ":" + _port + ", " + finger;
    }

    protected void notifyOnActionFailed(int action, Exception e) {
        if (_listener != null) {
            _listener.onActionFailed(this, action, e);
        }
    }

    public interface OnActionFailedListener {
        public void onActionFailed(Device device, int action, Exception e);
    }
}
