package com.frostwire.gnutella.gui.android;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;

import org.limewire.io.NetworkUtils;

import com.frostwire.HttpFetcher;
import com.frostwire.json.JsonEngine;
import com.limegroup.gnutella.settings.ConnectionSettings;

public class PeerDiscoveryClerk {
	
	private static final long STALE_DEVICE_TIMEOUT = 21000;
	
	private HashMap<String, Device> DEVICE_CACHE;
	private Map<Device, Long> DEVICE_TIMEOUTS;
	
	private JsonEngine _jsonEngine;
	
	public PeerDiscoveryClerk() {
		DEVICE_CACHE = new HashMap<String, Device>();
		DEVICE_TIMEOUTS = new HashMap<Device, Long>();
		_jsonEngine = new JsonEngine();
	}
	
	public void handleNewDevice(String key, final Device device) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleNewDevice(device);
			}
		});
	}
	
	private void handleDeviceAlive(String key, final Device device) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleDeviceAlive(device);
			}
		});
	}
	
	private void handleDeviceStale(String key, final Device device) {
		DEVICE_CACHE.remove(key);
		DEVICE_TIMEOUTS.remove(key);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleDeviceStale(device);
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
		
		new Thread(new CleanStaleDevices()).start();
	}

	private void startBroadcast() throws Exception {
		
		final DatagramSocket socket = new DatagramSocket(null);
		socket.setReuseAddress(true);
		socket.setBroadcast(true);
		socket.setSoTimeout(60000);

		socket.bind(new InetSocketAddress(DeviceConstants.PORT_BROADCAST));
		
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

		final MulticastSocket socket = new MulticastSocket(DeviceConstants.PORT_MULTICAST);
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
		boolean b = (data[0x43] & 0xFF) != 0;
		
		InetAddress address = packet.getAddress();
		
		handlePossibleNewDevice(address, p + 1, multicast, b);
	}

	private void handlePossibleNewDevice(InetAddress address, int p, boolean multicast, boolean b) {
		
		String key = address.getHostAddress() + ":" + p;
		
		if (DEVICE_CACHE.containsKey(key)) {
			Device device = DEVICE_CACHE.get(key);
			if (!b) {
				handleDeviceAlive(key, device);
			} else {
				handleDeviceStale(key, device);
			}
			return;
		}
		
		try {
			
			URI uri = new URI("http://" + key + "/finger");
			
			HttpFetcher fetcher = new HttpFetcher(uri);
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null) {
				System.out.println("Failed to connnect to " + uri);
				return;
			}
			
			String json = new String(jsonBytes);
			
			Finger finger = _jsonEngine.toObject(json, Finger.class);
			
			synchronized (DEVICE_CACHE) {			
				if (DEVICE_CACHE.containsKey(key)) { // best effort without lock
					Device device = DEVICE_CACHE.get(key);
					if (!b) {
						handleDeviceAlive(key, device);
					} else {
						handleDeviceStale(key, device);
					}
				} else {
					if (!b) {
						Device device = new Device(address, p, finger);
						DEVICE_CACHE.put(key, device);
						handleNewDevice(key, device);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to connnect http to " + key);
		}
	}
	
	private final class CleanStaleDevices implements Runnable {

		@Override
		public void run() {
			
			while (true) {
				
				long now = System.currentTimeMillis();
				
				for (Entry<Device, Long> entry : DEVICE_TIMEOUTS.entrySet()){
					if (entry.getValue() + STALE_DEVICE_TIMEOUT < now) {
						
						Device device = entry.getKey();
						String key = device.getAddress().getHostAddress() + ":" + device.getPort();
						
						handleDeviceStale(key, entry.getKey());
					}
				}
				
				for (int i = 0; i < STALE_DEVICE_TIMEOUT; i+= 1000) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
}
