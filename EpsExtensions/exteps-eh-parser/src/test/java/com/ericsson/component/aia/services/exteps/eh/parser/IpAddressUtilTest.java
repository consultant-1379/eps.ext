package com.ericsson.component.aia.services.exteps.eh.parser;

import static com.ericsson.component.aia.services.exteps.eh.parser.IpAddressUtil.getReadableIPAddress;
import static org.junit.Assert.assertEquals;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;

public class IpAddressUtilTest {

    @Test
    public void getReadableIPAddress_IpV4ByteArray_readableIpV4Address() throws UnknownHostException {
        final String EXPECTED_IPV4ADDRESS = "10.45.90.1";
        final InetAddress inetAddress = InetAddress.getByName(EXPECTED_IPV4ADDRESS);
        final byte[] ipAddress = convertIpAddress(inetAddress);
        final String ipV4Address = getReadableIPAddress(ipAddress);
        assertEquals(EXPECTED_IPV4ADDRESS, ipV4Address);
    }

    @Ignore
    @Test(expected = UnknownHostException.class)
    public void getInvalidIPAddress_IpV4ByteArray_wrongLengthIpV4Address() throws UnknownHostException {
        final String EXPECTED_IPV4ADDRESS = "invalidIpAddress";
        final InetAddress inetAddress = InetAddress.getByName(EXPECTED_IPV4ADDRESS);
        final byte[] ipAddress = convertIpAddress(inetAddress);
        getReadableIPAddress(ipAddress);
    }

    @Test
    @Ignore("Expected IPV6 address is resolve to 'atrcxb3050-v6.athtem.eei.ericsson.se' if machine have access to DNS"
            + "[eikrwaq@atrcxb2330 tracefiledecodercli]$ nslookup 2001:1b70:82a1:17:0:2023:f:1                           "
            + "Server:         159.107.173.3                                                                             "
            + "Address:        159.107.173.3#53"
            + "1.0.0.0.f.0.0.0.3.2.0.2.0.0.0.0.7.1.0.0.1.a.2.8.0.7.b.1.1.0.0.2.ip6.arpa        name = atrcxb3050-v6.athtem.eei.ericsson.se.")

    public void getReadableIPAddress_IpV6ByteArray_readableIpV6Address() throws UnknownHostException {
        final String EXPECTED_IPV6ADDRESS = "2001:1b70:82a1:17:0:2023:f:1";
        final InetAddress inetAddress = InetAddress.getByName(EXPECTED_IPV6ADDRESS);
        final byte[] ipAddress = convertIpAddress(inetAddress);
        final String ipV6Address = getReadableIPAddress(ipAddress);
        assertEquals(EXPECTED_IPV6ADDRESS, ipV6Address);
    }

    /**
     * Method for obtain a 16 bytes representation of the IP address (In the case of Ipv4 it is filled with 0)
     * 
     * @param address
     *            address to be converted
     * @return byte array representation of address
     * @throws UnknownHostException
     */
    private byte[] convertIpAddress(final InetAddress address) throws UnknownHostException {
        if (address instanceof Inet4Address) {
            final byte[] result = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            System.arraycopy(address.getAddress(), 0, result, 12, 4); // 4 bytes (32 bits) address
            return result;
        }
        if (address instanceof Inet6Address) {
            return address.getAddress(); // 128-bit (16 bytes) IPv6 address.
        }
        return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }

}
