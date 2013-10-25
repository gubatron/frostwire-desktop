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

package com.frostwire.gui.library;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.limewire.concurrent.ExecutorsHelper;
import org.limewire.util.NetworkUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.HttpFetcherListener;
import com.frostwire.JsonEngine;
import com.frostwire.core.FileDescriptor;
import com.frostwire.gui.library.ProgressFileEntity.ProgressFileEntityListener;
import com.frostwire.gui.upnp.PingInfo;
import com.frostwire.gui.upnp.UPnPManager;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.util.EncodingUtils;

/**
 * @author gubatron
 * @author aldenml
 * 
 */
public class Device {

    private static final Log LOG = LogFactory.getLog(Device.class);

    private static final ExecutorService executor;

    static {
        //upload files to 3 different devices at the same time.
        executor = ExecutorsHelper.newFixedSizeThreadPool(3, "UploadToDeviceExecutor");
    }

    public static int ACTION_BROWSE = 0;
    public static int ACTION_DOWNLOAD = 1;
    public static int ACTION_UPLOAD = 2;

    private static JsonEngine JSON_ENGINE = new JsonEngine();

    private final String udn;
    private InetAddress _address;
    private int _port;
    private Finger finger;
    private boolean _tokenAuthorized;
    private OnActionFailedListener _listener;
    private long timestamp;

    private PingInfo pingInfo;

    private boolean local;

    public Device(String udn, InetAddress address, int port, Finger finger, PingInfo pinfo) {
        this.udn = udn;
        this._address = address;
        this._port = port;
        this.finger = finger;
        this.pingInfo = pinfo;
        this.local = udn.equals(UPnPManager.instance().getLocalDevice().getIdentity().getUdn().getIdentifierString());
    }
    
    /**
     * aka isMe()
     */
    public boolean isLocal() {
        return local;
    }
    
    public String getUdn() {
        return udn;
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

            HttpFetcher fetcher = new HttpFetcher(uri, 10000); // 10 seconds http timeout

            byte[] jsonBytes = (byte[]) fetcher.fetch(true)[0];

            if (jsonBytes == null) {
                notifyOnActionFailed(ACTION_BROWSE, null);
                return new ArrayList<FileDescriptor>();
            }

            setTimestamp(System.currentTimeMillis());

            String json = new String(jsonBytes, "UTF-8");

            FileDescriptorList list = JSON_ENGINE.toObject(json, FileDescriptorList.class);

            return list.files;

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_BROWSE, e);
        }

        return new ArrayList<FileDescriptor>();
    }

    public URL getDownloadURL(int type, int id) {
        try {

            String ip = _address.getHostAddress();

            if (ip.equals("0.0.0.0")) {
                // we need to replace with a real nic address
                ip = NetworkUtils.getLocalAddress().getHostAddress();
            }

            return new URL("http://" + ip + ":" + _port + "/download?type=" + type + "&id=" + id);

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_DOWNLOAD, e);
        }

        return null;
    }

    public String getDownloadURL(FileDescriptor fd) {
        return getDownloadURL(fd.fileType, fd.id).toString();
    }
    
    public int getDeviceType() {
        return  pingInfo.deviceMajorType;
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

    public void upload(File[] files) {
        try {
            final DesktopUploadRequest dur = new DesktopUploadRequest();

            dur.address = NetworkUtils.getLocalAddress().getHostAddress();
            dur.computerName = NetworkUtils.getLocalAddress().getHostName();
            dur.files = new ArrayList<FileDescriptor>();

            for (File f : flatFiles(files)) {
                for (File cf : getFiles(f, 3)) {
                    FileDescriptor fd = new FileDescriptor();
                    fd.filePath = cf.getAbsolutePath();
                    fd.fileSize = cf.length();

                    dur.files.add(fd);
                }
            }

            HttpFetcher fetcher = new HttpFetcher("http://" + _address.getHostAddress() + ":" + _port + "/dekstop-upload-request", 60000);

            String json = new JsonEngine().toJson(dur);

            final DeviceUploadDialog dlg = new DeviceUploadDialog(GUIMediator.getAppFrame());

            fetcher.asyncPostJSON(json, new HttpFetcherListener() {

                @Override
                public void onSuccess(final byte[] body) {
                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            if (dlg.isVisible()) {
                                dlg.setVisible(false);
                                try {
                                    String token = new String(body, "UTF-8");

                                    executor.execute(new DeviceUploadTask(Device.this, dur.files.toArray(new FileDescriptor[0]), token));
                                } catch (Throwable e) {
                                    LOG.error("Error uploading files to device", e);
                                }
                            }
                        }
                    });
                }

                @Override
                public void onError(Throwable e) {
                    GUIMediator.safeInvokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (dlg.isVisible()) {
                                dlg.setVisible(false);
                                JOptionPane.showMessageDialog(GUIMediator.getAppFrame(), I18n.tr("The device is busy with another transfer or did not authorize your request"), I18n.tr("Transfer failed"), JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    });
                }
            });

            dlg.setVisible(true);

        } catch (Throwable e) {
            LOG.error("Error uploading files to device", e);
        }
    }

    public void upload(File file, String token, ProgressFileEntityListener listener) {

        URI uri = null;

        try {
            uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/desktop-upload?filePath=" + EncodingUtils.encode(file.getAbsolutePath()) + "&token=" + EncodingUtils.encode(token));

            HttpFetcher fetcher = new HttpFetcher(uri);

            ProgressFileEntity fileEntity = new ProgressFileEntity(file);
            fileEntity.setProgressFileEntityListener(listener);

            fetcher.post(fileEntity);

        } catch (Exception e) {
            notifyOnActionFailed(ACTION_UPLOAD, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        return udn.hashCode();
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

    private static List<File> flatFiles(File[] files) {
        Set<File> set = new HashSet<File>();
        for (File f : files) {
            for (File cf : getFiles(f, 3)) {
                if (!set.contains(cf)) {
                    set.add(cf);
                }
            }
        }

        return new ArrayList<File>(set);
    }

    private static List<File> getFiles(File file, int depth) {
        List<File> files = new ArrayList<File>();

        if (file == null) {
            return files;
        }

        if (!file.isDirectory()) {
            files.add(file);
            return files;
        }

        for (File childFile : file.listFiles()) {
            if (!childFile.isDirectory()) {
                files.add(childFile);
            } else {
                if (depth > 0) {
                    files.addAll(getFiles(childFile, depth - 1));
                }
            }
        }

        return files;
    }
}
