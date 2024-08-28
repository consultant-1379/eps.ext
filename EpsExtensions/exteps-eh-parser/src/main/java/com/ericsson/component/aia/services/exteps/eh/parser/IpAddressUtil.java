package com.ericsson.component.aia.services.exteps.eh.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpAddressUtil {
    protected static final Logger log = LoggerFactory.getLogger(IpAddressUtil.class);

    /**
     * Return a String representation of a byte array(16 bytes) IP Address. Supports IPv4 & IPv6 format.
     *
     * @param ipAddress
     * @return
     */
    protected static String getReadableIPAddress(final byte[] ipAddress) {
        String readableaddress = "";
        if (ipAddress != null && ipAddress.length == 16) {
            if (ipAddress[0] != 0) {
                readableaddress = getIpAddress(ipAddress);
            } else {
                final byte[] ipv4Address = new byte[4];
                System.arraycopy(ipAddress, 12, ipv4Address, 0, 4);
                readableaddress = getIpAddress(ipv4Address);
            }
        }
        return readableaddress;
    }

    /**
     * Gets the host name for this IP address.
     *
     * @param ipAddress
     * @return
     */
    private static String getIpAddress(final byte[] ipAddress) {
        String address = "";
        try {
            address = InetAddress.getByAddress(ipAddress).getHostName();
        } catch (UnknownHostException e) {
            log.error("Exception resolving address [{}] :: {}", ipAddress, e.getMessage());
        }
        return address;
    }
}