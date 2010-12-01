package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.frostwire.HttpFetcher;
import com.frostwire.gnutella.gui.android.ProgressFileEntity.ProgressFileEntityListener;
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.util.EncodingUtils;

public class Device {
	
	public static int ACTION_BROWSE = 0;	
	public static int ACTION_DOWNLOAD = 1;	
	public static int ACTION_UPLOAD = 2;
	
	private static JsonEngine JSON_ENGINE = new JsonEngine();

	private InetAddress _address;
	private int _port;
	private Finger _finger;
	private String _token;
	private boolean _tokenAuthorized;
	private OnActionFailedListener _listener;
	
	public Device(InetAddress address, int port, Finger finger) {
		_address = address;
		_port = port;
		_finger = finger;
		_token = UUID.randomUUID().toString();
	}

	public void setAddress(InetAddress address) {
		_address = address;
	}

	public InetAddress getAddress() {
		return _address;
	}

	public void setPort(int port) {
		_port = port;
	}

	public int getPort() {
		return _port;
	}

	public void setFinger(Finger finger) {
		_finger = finger;
	}

	public Finger getFinger() {
		return _finger;
	}

	public String getName() {
		return _finger.nickname;
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
	
	public List<FileDescriptor> browse(int type) {
		
		try {
			
			URI uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/browse?type=" + type);
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null) {
				notifyOnActionFailed(ACTION_BROWSE, null);
				return new ArrayList<FileDescriptor>();
			}
			
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
	
	public byte[] download(int type, int id) {
		
		try {
			
			URI uri = getDownloadURL(type, id).toURI();
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] data = fetcher.fetch();
			
			if (data == null) {
				notifyOnActionFailed(ACTION_DOWNLOAD, null);
				return null;
			}
			
			return data; 
			
		} catch (Exception e) {
			notifyOnActionFailed(ACTION_DOWNLOAD, e);
		}
		
		return null;
	}
	
	public void upload(int type, File file) {
		try {
			
			URI uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/upload?type=" + type + "&fileName=" + EncodingUtils.encode(file.getName()) + "&token=" + EncodingUtils.encode(_token));
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			fetcher.post(file);
						
		} catch (Exception e) {
			notifyOnActionFailed(ACTION_UPLOAD, e);
		}
	}
	
	public void upload(int type, File file, ProgressFileEntityListener listener) {
		
		URI uri = null;
		
		try {
			
			uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/upload?type=" + type + "&fileName=" + EncodingUtils.encode(file.getName()) + "&token=" + EncodingUtils.encode(_token));
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			ProgressFileEntity fileEntity = new ProgressFileEntity(file);
			fileEntity.setProgressFileEntityListener(listener);
			
			fetcher.post(fileEntity);
						
		} catch (Exception e) {
			notifyOnActionFailed(ACTION_UPLOAD, e);
		}
	}
	
	@Override
	public int hashCode() {
		return _finger.uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Device)) {
			return false;
		}
		
		return _finger.uuid.equals(((Device) obj)._finger.uuid);
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
