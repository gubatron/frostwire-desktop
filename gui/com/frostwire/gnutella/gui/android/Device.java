package com.frostwire.gnutella.gui.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.utils.URLEncodedUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.util.EncodingUtils;


public class Device {
	
	private JsonEngine JSON_ENGINE = new JsonEngine();

	private InetAddress _address;
	private int _port;
	private Finger _finger;
	private String _token;
	private DeviceListener _listener;
	
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
	

	public DeviceListener getListener() {
		return _listener;
	}
	
	public void setListener(DeviceListener listener) {
		_listener = listener;
	}
	
	public List<FileDescriptor> browse(int type) {
		
		try {
			
			URI uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/browse?type=" + type);
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null) {
				System.out.println("Failed to connnect to " + uri);
				actionFailed(null);
				return new ArrayList<FileDescriptor>();
			}
			
			String json = new String(jsonBytes);
			
			FileDescriptorList list = JSON_ENGINE.toObject(json, FileDescriptorList.class);
			
			return list.files; 
			
		} catch (Exception e) {
			actionFailed(e);
		}
		
		return new ArrayList<FileDescriptor>();
	}
	
	public URL getDownloadURL(int type, int id) {
		try {
			
			return new URL("http://" + _address.getHostAddress() + ":" + _port + "/download?type=" + type + "&id=" + id);
			
		} catch (Exception e) {
			actionFailed(e);
		}
		
		return null;
	}
	
	public byte[] download(int type, int id) {
		
		try {
			
			URI uri = getDownloadURL(type, id).toURI();
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] data = fetcher.fetch();
			
			if (data == null) {
				System.out.println("Failed to connnect to " + uri);
				actionFailed(null);
				return null;
			}
			
			return data; 
			
		} catch (Exception e) {
			actionFailed(e);
		}
		
		return null;
	}
	
	public void upload(int type, File file) {
		try {
			
			URI uri = new URI("http://" + _address.getHostAddress() + ":" + _port + "/upload?type=" + type + "&fileName=" + EncodingUtils.encode(file.getName()) + "&token=" + _token);
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] data = fetcher.post(file);
			
			if (data == null) {
				System.out.println("Failed to connnect to " + uri);
				actionFailed(null);
			}
			
		} catch (Exception e) {
			actionFailed(e);
		}
	}
	
	private void actionFailed(Exception e) {
		if (_listener != null) {
			_listener.onActionFailed(this, e);
		}
	}
	
	public interface DeviceListener {
		public void onActionFailed(Device device, Exception e);
	}
}
