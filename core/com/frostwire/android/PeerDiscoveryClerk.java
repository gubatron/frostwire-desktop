package com.frostwire.android;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import com.frostwire.HttpFileFetcher;
import com.frostwire.gnutella.gui.chat.AndroidMediator;

public class PeerDiscoveryClerk {
	
	private HashSet<String> DEVICE_CACHE;
	
	public PeerDiscoveryClerk() {
		DEVICE_CACHE = new HashSet<String>();
	}
	
	public void handleNewDevice(final String json) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			    AndroidMediator.handleNewDevice(json);
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

		InetAddress loopback = InetAddress.getByName("10.10.10.109");

		socket.bind(new InetSocketAddress(loopback, 0xffb0));
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {

					byte[] data = new byte[65535];

					while (true) {

						try {

							DatagramPacket packet = new DatagramPacket(data, data.length);

							socket.receive(packet);

							handleDatagramPacket(packet);

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

		final MulticastSocket socket = new MulticastSocket(0xffa0);
		socket.setSoTimeout(60000);
		socket.setTimeToLive(254);
		socket.setReuseAddress(true);
		socket.setNetworkInterface(NetworkInterface.getByInetAddress(InetAddress.getByName("10.10.10.109")));
		
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

							handleDatagramPacket(packet);

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
	
	private void handleDatagramPacket(DatagramPacket packet) {
		
		System.out.println("-----------DATAGRAM-----------------");
		
		byte[] data = packet.getData();
		
		int p = ((data[0x1e] & 0xFF) << 8) + (data[0x1f] & 0xFF);
		
		InetAddress address = packet.getAddress();
		
		handlePossibleNewDevice(address, p);
	}

	private void handlePossibleNewDevice(InetAddress address, int p) {
		String key = address + ":" + (p + 1);
		if (DEVICE_CACHE.contains(key)) {
			return;
		}
		
		try {
			HttpFileFetcher fetcher = new HttpFileFetcher(new URI(key + "/finger"));
			
			fetcher.fetch();
			
			byte[] jsonBytes = fetcher.fetch();
			
			if (jsonBytes == null)
				return;
			
			String json = new String(jsonBytes);
			
			DEVICE_CACHE.add(key);
			
			handleNewDevice(json);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
