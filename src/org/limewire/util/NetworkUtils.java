/*
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

package org.limewire.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Provides methods for network programming. 
 * <code>NetworkUtils</code>' methods check the validity of IP addresses, ports
 * and socket addresses. <code>NetworkUtils</code> includes both 
 * IPv4 and 
 * <a href="http://en.wikipedia.org/wiki/IPv6">IPv6</a> compliant methods.
 */
public final class NetworkUtils {
    
    /**
     * Ensure that this class cannot be constructed.
     */
    private NetworkUtils() {}

    /**
     * Returns whether or not the specified port is within the valid range of
     * ports.
     * 
     * @param port
     *            the port number to check
     */
    public static boolean isValidPort(int port) {
        return (port > 0 && port <= 0xFFFF);
    }

    /**
     * Returns whether or not the specified InetAddress is valid.
     */
    public static boolean isValidAddress(InetAddress address) {
        return !address.isAnyLocalAddress() 
            && !isInvalidAddress(address)
            && !isBroadcastAddress(address)
            && !isDocumentationAddress(address);
    }
    
    /**
     * Checks if the given address is a private address.
     * 
     * This method is IPv6 compliant
     * 
     * @param address the address to check
     */
    static boolean isPrivateAddress(byte[] address) {
        if (isAnyLocalAddress(address) 
                || isInvalidAddress(address)
                || isLoopbackAddress(address) 
                || isLinkLocalAddress(address) 
                || isSiteLocalAddress(address)
                || isUniqueLocalUnicastAddress(address)
                || isBroadcastAddress(address)
                || isDocumentationAddress(address)) {
            return true;
        }
        
        return false;
    }
    /**
     * @return A non-loopback IPv4 address of a network interface on the local
     *         host.
     * @throws UnknownHostException
     */
    public static InetAddress getLocalAddress() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        
        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
            return addr;
        }
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        addr = addresses.nextElement();
                        if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                            return addr;
                        }
                    }
                }
            }
        } catch (SocketException se) {
        }

        throw new UnknownHostException(
                "localhost has no interface with a non-loopback IPv4 address");
    }
    
    /**
     * Returns the IP:Port as byte array.
     * 
     * This method is IPv6 compliant
     */
    public static byte[] getBytes(SocketAddress addr) throws UnknownHostException {
        InetSocketAddress iaddr = (InetSocketAddress)addr;
        if (iaddr.isUnresolved()) {
            throw new UnknownHostException(iaddr.toString());
        }
        
        return getBytes(iaddr.getAddress(), iaddr.getPort());
    }
    
    /**
     * Returns the IP:Port as byte array.
     * 
     * This method is IPv6 compliant
     */
    public static byte[] getBytes(InetAddress addr, int port) {
        if (!isValidPort(port)) {
            throw new IllegalArgumentException("Port out of range: " + port);
        }
        
        byte[] address = addr.getAddress();

        byte[] dst = new byte[address.length + 2];
        System.arraycopy(address, 0, dst, 0, address.length);
        dst[dst.length-2] = (byte)((port >> 8) & 0xFF);
        dst[dst.length-1] = (byte)((port     ) & 0xFF);
        return dst;
    }
    
    /**
     * Returns true if the given byte-array is an IPv4 address
     */
    private static boolean isIPv4Address(byte[] address) {
        return address.length == 4;
    }
    
    /**
     * Returns true if the given byte-array is an IPv4 mapped address.
     * IPv4 mapped addresses indicate systems that do not support IPv6. 
     * They are limited to IPv4. An IPv6 host can communicate with an 
     * IPv4 only host using the IPv4 mapped IPv6 address.
     */
    static boolean isIPv4MappedAddress(byte[] address) {
        if (address.length == 16 
                && (address[ 0] == 0x00) && (address[ 1] == 0x00) 
                && (address[ 2] == 0x00) && (address[ 3] == 0x00) 
                && (address[ 4] == 0x00) && (address[ 5] == 0x00) 
                && (address[ 6] == 0x00) && (address[ 7] == 0x00) 
                && (address[ 8] == 0x00) && (address[ 9] == 0x00) 
                && (address[10] == (byte)0xFF) && (address[11] == (byte)0xFF)) {   
            return true;
        }
        
        return false;  
    }
    
    /**
     * Returns true if it's an IPv4 InetAddress and the first octed is 0x00.
     */
    private static boolean isInvalidAddress(InetAddress address) {
        return isInvalidAddress(address.getAddress());
    }
    
    /**
     * Returns true if it's an IPv4 InetAddress and the first octet is 0x00.
     */
    private static boolean isInvalidAddress(byte[] address) {
        if (isIPv4Address(address) || isIPv4MappedAddress(address)) {
            return address[/* 0 */ address.length - 4] == 0x00;
        }
        return false;
    }
    
    /**
     * Returns true if the given byte-array is an any local address.
     */
    static boolean isAnyLocalAddress(byte[] address) {
        if (address.length == 4 || address.length == 16) {
            byte test = 0;
            for (int i = 0; i < address.length; i++) {
                test |= address[i];
            }
            
            return (test == 0x00);
        }
        return false;
    }
    
    /**
     * Returns true if the given byte-array is a loopback address
     */
    static boolean isLoopbackAddress(byte[] address) {
        if (isIPv4Address(address) || isIPv4MappedAddress(address)) {
            return (address[/* 0 */ address.length - 4] & 0xFF) == 127;
        } else if (address.length == 16) {
            byte test = 0x00;
            for (int i = 0; i < 15; i++) {
                test |= address[i];
            }
            return (test == 0x00) && (address[15] == 0x01);
        }
        return false;
    }
    
    /**
     * Returns true if the given byte-array is a link-local address
     */
    static boolean isLinkLocalAddress(byte[] address) {
        if (isIPv4Address(address) || isIPv4MappedAddress(address)) {
            return (address[/* 0 */ address.length - 4] & 0xFF) == 169
                && (address[/* 1 */ address.length - 3] & 0xFF) == 254;
            
        // FE80::/64
        } else if (address.length == 16) {
            return (address[0] & 0xFF) == 0xFE
                && (address[1] & 0xC0) == 0x80;
        }
        return false;
    }
    
    /**
     * Returns true if the given byte-array is a site-local address.
     * IPv6 site-local addresses were deprecated in September 2004 
     * by RFC 3879 and replaced by RFC 4193 (Unique Local IPv6 Unicast
     * Addresses).
     */
    static boolean isSiteLocalAddress(byte[] address) {
        if (isIPv4Address(address) || isIPv4MappedAddress(address)) {
            return  (address[/* 0 */ address.length - 4] & 0xFF) == 10
                || ((address[/* 0 */ address.length - 4] & 0xFF) == 172
                &&  (address[/* 1 */ address.length - 3] & 0xF0) == 16)
                || ((address[/* 0 */ address.length - 4] & 0xFF) == 192
                &&  (address[/* 1 */ address.length - 3] & 0xFF) == 168);
            
        // Has been deprecated in September 2004 by RFC 3879 
        // FEC0::/10
        } else if (address.length == 16) {
            return (address[0] & 0xFF) == 0xFE
                && (address[1] & 0xC0) == 0xC0;
        }
        return false;
    }

    /**
     * Returns true if the given byte-array is an Unique Local IPv6
     * Unicast Address. See RFC 4193 for more info.
     */
    private static boolean isUniqueLocalUnicastAddress(byte[] address) {
        // FC00::/7
        if (address.length == 16) {
            return (address[0] & 0xFE) == 0xFC;
        }
        return false;
    }

    /**
     * Returns true if the given InetAddress is a broadcast address.
     */
    public static boolean isBroadcastAddress(InetAddress address) {
        return isBroadcastAddress(address.getAddress());
    }
    
    /**
     * Returns true if the given byte-array is a broadcast address
     * 
     * This method is IPv6 compliant but returns always false if
     * the given address is neither a true IPv4, nor an IPv4-mapped
     * address.
     */
    private static boolean isBroadcastAddress(byte[] address) {
        if (isIPv4Address(address) || isIPv4MappedAddress(address)) {
            return (address[/* 0 */ address.length - 4] & 0xFF) == 0xFF;
        }
        
        return false;
    }
    
    /**
     * Returns true if the given InetAddress has a prefix that's used in
     * Documentation. It's a non-routeable IPv6 address. See <a href=
     * "http://www.ietf.org/rfc/rfc3849.txt">RFC 3849</a>  
     * for more information.
     */
    public static boolean isDocumentationAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            return isDocumentationAddress(address.getAddress());
        }
        return false;
    }
    
    /**
     * Returns true if the given byte-array has a prefix that's used in
     * Documentation. It's a non-routeable IPv6 address. See <a href=
     * "http://www.ietf.org/rfc/rfc3849.txt">RFC 3849</a> 
     * for more information.
     */
    private static boolean isDocumentationAddress(byte[] address) {
        // 2001:0DB8::/32
        if (address.length == 16) {
            return (address[0] & 0xFF) == 0x20
                && (address[1] & 0xFF) == 0x01
                && (address[2] & 0xFF) == 0x0D
                && (address[3] & 0xFF) == 0xB8;
        }
        return false;
    }
}
