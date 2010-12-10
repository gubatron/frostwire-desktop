package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
	private long _timeout;
	
	public Device(InetAddress address, int port, Finger finger) {
		_address = address;
		_port = port;
		_finger = finger;
		_token = UUID.randomUUID().toString();
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
        return _finger;
    }

	public void setFinger(Finger finger) {
		_finger = finger;
	}
	
	public long getTimeout() {
	    return _timeout;
	}
	
	public void setTimeout(long timeout) {
	    _timeout = timeout;
	}

	public String getName() {
		return _finger.nickname;
	}
	
	public String getKey() {
	    return _address.getHostAddress() + ":" + _port;
	}
	
	public int getTotalShared() {
	    return _finger.numSharedApplicationFiles +
	           _finger.numSharedDocumentFiles +
	           _finger.numSharedPictureFiles +
	           _finger.numSharedVideoFiles +
	           _finger.numSharedRingtoneFiles +
	           _finger.numSharedAudioFiles;
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
			
			byte[] jsonBytes = (byte[]) fetcher.fetch(true)[0];
			
			if (jsonBytes == null) {
				notifyOnActionFailed(ACTION_BROWSE, null);
				return new ArrayList<FileDescriptor>();
			}
			
			setTimeout(System.currentTimeMillis());
			
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
			
			setTimeout(System.currentTimeMillis());
			
			return data; 
			
		} catch (Exception e) {
			notifyOnActionFailed(ACTION_DOWNLOAD, e);
		}
		
		return null;
	}
	
	public void upload(int type, File file, ProgressFileEntityListener listener) {
		
		URI uri = null;
		
		try {
		    
		    uri = new URI("http://" + _address.getHostAddress() + ":" + _port +
                    "/authorize?token=" + EncodingUtils.encode(_token) +
                    "&from=" + EncodingUtils.encode(System.getProperty("user.name")));

		    HttpFetcher fetcher = new HttpFetcher(uri);
		    
		    byte[] fetch = fetcher.fetch();
		    
		    if (!(fetch!=null &&
		    	Arrays.equals(_token.getBytes(), fetch))) {
		    	throw new Exception("Not authorized or invalid token for upload to " + _finger.nickname);
		    }
		    
		    setTokenAuthorized(true);
		    
		    
		    setTimeout(System.currentTimeMillis());
			
			uri = new URI("http://" + _address.getHostAddress() + ":" + _port +
			        "/upload?type=" + type +
			        "&fileName=" + EncodingUtils.encode(file.getName()) +
			        "&token=" + EncodingUtils.encode(_token));
			
			fetcher = new HttpFetcher(uri);
			
			ProgressFileEntity fileEntity = new ProgressFileEntity(file);
			fileEntity.setProgressFileEntityListener(listener);
			
			fetcher.post(fileEntity);
			
			setTimeout(System.currentTimeMillis());
						
		} catch (Exception e) {
			notifyOnActionFailed(ACTION_UPLOAD, e);
			throw new RuntimeException(e);
		}
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
	
	protected void notifyOnActionFailed(int action, Exception e) {
		if (_listener != null) {
			_listener.onActionFailed(this, action, e);
		}
	}
	
	public interface OnActionFailedListener {
		public void onActionFailed(Device device, int action, Exception e);
	}
}
