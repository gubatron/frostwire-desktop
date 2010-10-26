package com.frostwire.android;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.limewire.io.NetworkUtils;

import com.frostwire.HttpFileFetcher;
import com.frostwire.gnutella.gui.chat.AndroidMediator;
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.settings.ConnectionSettings;

public class PeerDiscoveryClerk {
	
	public static final int PORT_MULTICAST = 0xffa0; // 65440
	public static final int PORT_BROADCAST = 0xffb0; // 65456
	
	private HashMap<String, Finger> DEVICE_CACHE;
	
	private JsonEngine _jsonEngine;
	
	public PeerDiscoveryClerk() {
		DEVICE_CACHE = new HashMap<String, Finger>();
		_jsonEngine = new JsonEngine();
	}
	
	public void handleNewDevice(final Finger finger) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleNewDevice(finger);
			}
		});
	}
	
	private void handleDeviceAlive(final Finger finger) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleDeviceAlive(finger);
			}
		});
	}
	
	public void start() {
		try {
			startBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			startMulticast();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void startBroadcast() throws Exception {
		
		final DatagramSocket socket = new DatagramSocket(null);
		socket.setReuseAddress(true);
		socket.setBroadcast(true);
		socket.setSoTimeout(60000);

		socket.bind(new InetSocketAddress(PORT_BROADCAST));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {

					byte[] data = new byte[65535];

					while (true) {

						try {

							DatagramPacket packet = new DatagramPacket(data, data.length);

							socket.receive(packet);

							handleDatagramPacket(packet, false);

						} catch (InterruptedIOException e) {
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();
					socket.disconnect();
				}
			}
		}, "BroadcastClerk").start();
	}

	private void startMulticast() throws Exception {
		
		final InetAddress groupInetAddress = InetAddress.getByAddress(new byte[] { (byte) 224, 0, 1, 16 });

		final MulticastSocket socket = new MulticastSocket(PORT_MULTICAST);
		socket.setSoTimeout(60000);
		socket.setTimeToLive(254);
		socket.setReuseAddress(true);
		
		InetAddress address = null;
		
		if (!ConnectionSettings.CUSTOM_INETADRESS.isDefault()) {
			address = InetAddress.getByName(ConnectionSettings.CUSTOM_INETADRESS.getValue());
		} else {
			address = NetworkUtils.getLocalAddress();
		}
		socket.setNetworkInterface(NetworkInterface.getByInetAddress(address));
		
		socket.joinGroup(groupInetAddress);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {

					byte[] data = new byte[65535];

					while (true) {

						try {

							DatagramPacket packet = new DatagramPacket(data, data.length);

							socket.receive(packet);

							handleDatagramPacket(packet, true);

						} catch (InterruptedIOException e) {
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					socket.close();
					socket.disconnect();
				}
			}
		}, "MulticastClerk").start();
	}
	
	private void handleDatagramPacket(DatagramPacket packet, boolean multicast) {
		
		byte[] data = packet.getData();
		
		int p = ((data[0x1e] & 0xFF) << 8) + (data[0x1f] & 0xFF);
		
		InetAddress address = packet.getAddress();
		
		handlePossibleNewDevice(address, p, multicast);
	}

	private void handlePossibleNewDevice(InetAddress address, int p, boolean multicast) {
		
		String key = address.getHostAddress() + ":" + (p + 1);
		
		if (DEVICE_CACHE.containsKey(key)) {
			handleDeviceAlive(DEVICE_CACHE.get(key));
			return;
		}
		
		try {
			
			URI uri = new URI("http://" + key + "/finger");
			
			HttpFileFetcher fetcher = new HttpFileFetcher(uri);
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null) {
				System.out.println("Failed to connnect to " + uri);
				return;
			}
			
			String json = new String(jsonBytes);
			
			Finger finger = _jsonEngine.toObject(json, Finger.class);
			
			if (DEVICE_CACHE.containsKey(key)) { // best effort without lock
				handleDeviceAlive(DEVICE_CACHE.get(key));
			} else {
				DEVICE_CACHE.put(key, finger);
				handleNewDevice(finger);
			}
			
		} catch (Exception e) {
			System.out.println("Failed to connnect http to " + key);
		}
	}
}
