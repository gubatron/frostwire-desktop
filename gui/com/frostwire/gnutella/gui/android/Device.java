package com.frostwire.gnutella.gui.android;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.frostwire.HttpFetcher;
import com.frostwire.json.JsonEngine;


public class Device {
	
	private JsonEngine JSON_ENGINE = new JsonEngine();

	private InetAddress _address;
	private int _port;
	private Finger _finger;
	private DeviceListener _listener;
	
	public Device(InetAddress address, int port, Finger finger) {
		_address = address;
		_port = port;
		_finger = finger;
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
	
	private void actionFailed(Exception e) {
		if (_listener != null) {
			_listener.onActionFailed(this, e);
		}
	}
	
	public interface DeviceListener {
		public void onActionFailed(Device device, Exception e);
	}
}
