/******************************************************************
*
*	CyberUPnP for Java
*
*	Copyright (C) Satoshi Konno 2002-2003
*
*	File: SSDPSearchSocket.java
*
*	Revision;
*
*	12/30/02
*		- first revision.
*	05/13/03
*		- Added support for IPv6.
*	05/28/03
*		- Moved post() for SSDPSearchRequest to SSDPResponseSocketList.
*	
******************************************************************/

package org.cybergarage.upnp.ssdp;

import org.cybergarage.net.*;
import org.cybergarage.util.*;

import org.cybergarage.upnp.device.*;

public class SSDPSearchSocket extends HTTPMUSocket implements Runnable
{
	private boolean useIPv6Address;
	
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPSearchSocket()
	{
	}
	
	public SSDPSearchSocket(String bindAddr)
	{
		open(bindAddr);
	}

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public boolean open(String bindAddr)
	{
		String addr = SSDP.ADDRESS;
		useIPv6Address = false;
		if (HostInterface.isIPv6Address(bindAddr) == true) {
			addr = SSDP.getIPv6Address();
			useIPv6Address = true;
		}
		return open(addr, SSDP.PORT, bindAddr);
	}
	
	////////////////////////////////////////////////
	//	deviceSearch
	////////////////////////////////////////////////

	private ListenerList deviceSearchListenerList = new ListenerList();
	 	
	public void addSearchListener(SearchListener listener)
	{
		deviceSearchListenerList.add(listener);
	}		

	public void removeSearchListener(SearchListener listener)
	{
		deviceSearchListenerList.remove(listener);
	}		

	public void performSearchListener(SSDPPacket ssdpPacket)
	{
		int listenerSize = deviceSearchListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			SearchListener listener = (SearchListener)deviceSearchListenerList.get(n);
			listener.deviceSearchReceived(ssdpPacket);
		}
	}		
	
	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread deviceSearchThread = null;
		
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		
		while (deviceSearchThread == thisThread) {
			Thread.yield();
			SSDPPacket packet = receive();
			if (packet.isDiscover() == true)
				performSearchListener(packet);
		}
	}
	
	public void start()
	{
		deviceSearchThread = new Thread(this);
		deviceSearchThread.setName("device search thread");
		deviceSearchThread.start();
	}
	
	public void stop()
	{
		deviceSearchThread = null;
	}
}

