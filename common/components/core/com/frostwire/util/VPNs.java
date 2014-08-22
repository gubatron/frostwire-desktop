/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2014, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.limewire.util.OSUtils;

import com.limegroup.gnutella.gui.util.BackgroundExecutorService;

/**
 * 
 * @author gubatron
 * @author aldenml
 */
public final class VPNs {
    public static boolean isVPNActive() {
        boolean result = false;
        
        if (OSUtils.isAnyMac() || OSUtils.isLinux()) {
            result = isPosixVPNActive();
        } else if (OSUtils.isWindows()) {
            result = isWindowsVPNActive();
        }
        
        return result;
    }
    
    private static boolean isPosixVPNActive() {
        boolean result = false;
        try {
            result = isAnyNetworkInterfaceATunnel();
        } catch (Throwable t) {
            result = false;
            try {
                result = readProcessOutput("netstat","-nr").indexOf(" tun") != -1;                
            } catch (Throwable t2) {
                result = false;
            }
        }
        
        return result;
    }
    
    private static boolean isAnyNetworkInterfaceATunnel() {
        boolean result = false;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                if (iface.getDisplayName().contains("tun")) {
                    result = true;
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private static boolean isWindowsVPNActive() {
        // we might need some native magic here, some how tap into the 
        // starting points for research might be:
        //
        // Routing Table Manager Version 2
        // http://msdn.microsoft.com/en-us/library/aa373798%28v=VS.85%29.aspx
        //
        // GetAdaptersInfo function
        // http://msdn.microsoft.com/en-us/library/aa365917.aspx
        //
        // GetAdaptersAddresses function
        // http://msdn.microsoft.com/en-us/library/aa365915.aspx
        //
        // IP_ADAPTER_INFO structure
        // MIB_IF_TYPE_OTHER ?
        // http://msdn.microsoft.com/en-us/library/windows/desktop/aa366062(v=vs.85).aspx
        return false;
    }
    
    private static String readProcessOutput(String command, String arguments) {
        String result ="";
        
        ProcessBuilder pb = new ProcessBuilder(command, arguments);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            InputStream stdout = process.getInputStream();
            
            final BufferedReader brstdout = new BufferedReader(new InputStreamReader(stdout));
            
            Callable<String> outputReader = new Callable<String>() {

                @Override
                public String call() throws Exception {
                    String line="";
                    try {
                        
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((line = brstdout.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        
                        line = stringBuilder.toString();
                    } catch (Exception e) {
                    }
                    return line;
                }
            };
            
            Future<String> futureOutput = BackgroundExecutorService.submit(outputReader);
            result = futureOutput.get();
            
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public static void printNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                System.out.println(iface.getIndex() + ":" + iface.getDisplayName() + ":" +
                "virtual=" + iface.isVirtual() + ":" + "mtu=" + iface.getMTU() + ":mac=" + (iface.getHardwareAddress()!=null ? "0x"+ByteUtils.encodeHex(iface.getHardwareAddress()) : "n/a"));
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("GOT VPN? " + isVPNActive());
        printNetworkInterfaces();
    }
}