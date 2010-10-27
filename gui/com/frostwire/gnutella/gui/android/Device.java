package com.frostwire.gnutella.gui.android;

import java.net.InetAddress;


public class Device {

	private InetAddress _address;
	private int _port;
	private Finger _finger;
	
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
}
