package com.frostwire.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.LinkedList;

import org.limewire.io.IP;

import com.limegroup.gnutella.filters.IPList;

/**
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * @author gubatron
 *
 */

public final class HostilesMerger {
	private IPList _oldIPList;
	private LinkedList<String> _diffList;
	private LinkedList<String> _newList;
	
	private OldHostileFileProcessor _oldListProcessor;
	private NewHostileFileProcessor _newListProcessor;
	
	private File _outputFile;
	private BufferedOutputStream _outputStream;
	
	private static final HostilesMerger _instance = new HostilesMerger();
	
	private HostilesMerger() {
		_oldIPList = new IPList();
		_diffList = new LinkedList<String>();
		_newList = new LinkedList<String>();
		
		_oldListProcessor = new OldHostileFileProcessor();
		_newListProcessor = new NewHostileFileProcessor();
	}
	
	public static final HostilesMerger getInstance() {
		return _instance;
	}
	
	private final void loadOldIPList(String file) throws Exception {
		System.out.println("Loading Old List");
		_oldListProcessor.loadDataFromFile(file);
		
	} //loadList
	
	private final void mergeNewIPs(String file) throws Exception {
		System.out.println("Merging New IPs");
		_newListProcessor.loadDataFromFile(file);
		Collections.sort(_newList);
		
		for (String ip : _newList) {
			getOutputStream().write((ip + System.getProperty("line.separator")).getBytes());
		}
		getOutputStream().flush();
		getOutputStream().close();
		resetOutputStream();
	}
	
	private final void setOutputFile(String file) throws Exception {
		_outputFile = new File(file);
		_outputFile.createNewFile();
	}
	
	public final BufferedOutputStream getOutputStream() throws FileNotFoundException {
		//new BufferedOutputStream(new FileOutputStream(_outputFile))
		//new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(_outputFile))).
		if (_outputStream != null)
			return _outputStream;
		
		_outputStream = new BufferedOutputStream(new FileOutputStream(_outputFile));
		return _outputStream;
	}
	
	public final void resetOutputStream() {
		_outputStream = null;
	}
	
	
	/**
	 * Given an old hosts.txt file and a new one, it'll create a new IPList
	 * with the ips of the new one that didn't fall in any of the ip ranges defined by the first file.
	 * 
	 * Outputs to stdout
	 */
	private static final void usage() {
		System.out.println("Hostiles IP List Merger");
		System.out.println("By FrostWire.com, Licensed under the GNU General Public License.\n\n");
		System.out.println("hostilesMerger usage:");
		System.out.println("./hostilesMerger <oldHostsFile> <newHostsFile> <outputFile>");
		System.out.println("");
	}
	
	public IPList getOldIPList() {
		return _oldIPList;
	}
		
	public LinkedList<String> getNewList() {
		return _newList;
	}
	
	public LinkedList<String> getDiffList() {
		return _diffList;
	}

	/**
	 * Implements the logic needed when we're parsing the old hosts.txt
	 * @author gubatron
	 *
	 */
	private class OldHostileFileProcessor extends HostilesFileProcessor {
		
		@Override
		public void doSomethingWithIP(String ip) {
			//add node to patricia trie
			HostilesMerger.getInstance().getOldIPList().add(ip);
			HostilesMerger.getInstance().getNewList().add(ip);//since we're not deleting anybody (as of now, all the old ips go to the new list)
			HostilesMerger.getInstance().resetOutputStream();
		}
	}
	
	/**
	 * Implements the logic needed when we're processing the new hosts file.
	 * @author gubatron
	 *
	 */
	private class NewHostileFileProcessor extends HostilesFileProcessor {
		@Override
		public void doSomethingWithIP(String ip) {
			IP ipObj = new IP(ip);
			//Ask if the IP or IP range exist on the patricia trie
			if (!getInstance().getOldIPList().contains(ipObj)) {
				HostilesMerger.getInstance().getNewList().add(ip);
				HostilesMerger.getInstance().getDiffList().add(ip);
				System.out.println("+" + ip);
			} else {
				System.out.println("x " + ip);
			}
		}
	}
	
	public final static void main(String[] args) {
		if (args.length < 3) {
			usage();
			return;
		}
		
		HostilesMerger merger = HostilesMerger.getInstance();
		try {
			merger.setOutputFile(args[2]);
			merger.loadOldIPList(args[0]);
			merger.mergeNewIPs(args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	
}