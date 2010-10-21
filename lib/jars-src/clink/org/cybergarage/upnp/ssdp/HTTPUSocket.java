/******************************************************************
*
*	CyberLink for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: HTTPMU.java
*
*	Revision;
*
*	11/20/02
*		- first revision.
*	12/12/03
*		- Inma Mar?n <inma@DIF.UM.ES>
*		- Changed open(addr, port) to send IPv6 SSDP packets.
*		- The socket binds only the port without the interface address.
*		- The full binding socket can send SSDP IPv4 packets. Is it a bug of J2SE v.1.4.2-b28 ?.
*	01/06/04
*		- Oliver Newell <olivern@users.sourceforge.net>
*		- Added to set a current timestamp when the packet are received.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

import org.cybergarage.util.*;

public class HTTPUSocket
{
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private DatagramSocket ssdpUniSock = null;
	//private MulticastSocket ssdpUniSock = null;

	public DatagramSocket getDatagramSocket()
	{
		return ssdpUniSock;
	}
		
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPUSocket()
	{
		open();
	}
	
	public HTTPUSocket(String bindAddr, int bindPort) throws IOException
	{
		open(bindAddr, bindPort);
	}

	public HTTPUSocket(int bindPort)
	{
		open(bindPort);
	}

	protected void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	bindAddr
	////////////////////////////////////////////////

	private String localAddr = "";

	public void setLocalAddress(String addr)
	{
		localAddr = addr;
	}
	
	public String getLocalAddress()
	{
	    if (localAddr == null)
	        return null;
		if (0 < localAddr.length())
			return localAddr;
		if (ssdpUniSock != null && ssdpUniSock.getLocalAddress() != null)
			return ssdpUniSock.getLocalAddress().getHostAddress();
		else
			return null;
	}

	////////////////////////////////////////////////
	//	open
	////////////////////////////////////////////////
	
	public synchronized boolean open()
	{
		close();
		
		try {
			ssdpUniSock = new DatagramSocket();
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		
		return true;
	}
	
	public synchronized boolean open(String bindAddr, int bindPort) throws IOException
	{
		close();
		// use the same workaround as daap
		InetAddress addr = InetAddress.getLocalHost();
		if (addr.isLoopbackAddress() || !(addr instanceof Inet4Address)) {
		    addr = null;
		    Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
		    if (interfaces != null) {
		        while(addr == null && interfaces.hasMoreElements()) {
		            NetworkInterface nif = (NetworkInterface)interfaces.nextElement();
		            Enumeration addresses = nif.getInetAddresses();
		            while(addresses.hasMoreElements()) {
		                InetAddress address = (InetAddress)addresses.nextElement();
		                if (!address.isLoopbackAddress() 
		                        && address instanceof Inet4Address) {
		                    addr = address;
		                    break;
		                }
		            }
		        }
		    }
		}
		InetSocketAddress bindSock = new InetSocketAddress(addr, bindPort);
		ssdpUniSock = new DatagramSocket(null);
		ssdpUniSock.setReuseAddress(true);
		ssdpUniSock.bind(bindSock);
		
		
		setLocalAddress(bindAddr);
		
		return true;
	}

	public synchronized boolean open(int bindPort)
	{
		close();
		
		try {
			InetSocketAddress bindSock = new InetSocketAddress(bindPort);
			ssdpUniSock = new DatagramSocket(null);
			ssdpUniSock.setReuseAddress(true);
			ssdpUniSock.bind(bindSock);
		}
		catch (Exception e) {
			//Debug.warning(e);
			return false;
		}
		
		return true;
	}
		
	////////////////////////////////////////////////
	//	close
	////////////////////////////////////////////////

	public synchronized boolean close()
	{
		if (ssdpUniSock == null)
			return true;
			
		try {
			ssdpUniSock.close();
			ssdpUniSock = null;
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		
		return true;
	}

	////////////////////////////////////////////////
	//	send
	////////////////////////////////////////////////

	public synchronized boolean post(String addr, int port, String msg)
	{
		 try {
			InetAddress inetAddr = InetAddress.getByName(addr);
			DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(), msg.length(), inetAddr, port);
			ssdpUniSock.send(dgmPacket);
		}
		catch (Exception e) {
			Debug.warning("addr = " +ssdpUniSock.getLocalAddress().getHostName());
			Debug.warning("port = " + ssdpUniSock.getLocalPort());
			Debug.warning(e);
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////
	//	reveive
	////////////////////////////////////////////////

	public SSDPPacket receive()
	{
		byte ssdvRecvBuf[] = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
 		SSDPPacket recvPacket = new SSDPPacket(ssdvRecvBuf, ssdvRecvBuf.length);
		recvPacket.setLocalAddress(getLocalAddress());
		DatagramSocket sock;
		synchronized(this) {
			sock = ssdpUniSock;
		}
		if (sock==null)
			return null;
		try {
	 		sock.receive(recvPacket.getDatagramPacket());
			recvPacket.setTimeStamp(System.currentTimeMillis());
		}
		catch (Exception e) {
			return null;
		}
 		return recvPacket;
	}

	////////////////////////////////////////////////
	//	join/leave
	////////////////////////////////////////////////

/*
	boolean joinGroup(String mcastAddr, int mcastPort, String bindAddr)
	{
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.joinGroup(mcastGroup, mcastIf);
		}
		catch (Exception e) {
			Debug.warning(e);
			return false;
		}
		return true;
	}

	boolean leaveGroup(String mcastAddr, int mcastPort, String bindAddr)
	 {
		try {	 	
			InetSocketAddress mcastGroup = new InetSocketAddress(InetAddress.getByName(mcastAddr), mcastPort);
			NetworkInterface mcastIf = NetworkInterface.getByInetAddress(InetAddress.getByName(bindAddr));
			ssdpUniSock.leaveGroup(mcastGroup, mcastIf);
		 }
		 catch (Exception e) {
			 Debug.warning(e);
			 return false;
		 }
		 return true;
	 }
*/
}

