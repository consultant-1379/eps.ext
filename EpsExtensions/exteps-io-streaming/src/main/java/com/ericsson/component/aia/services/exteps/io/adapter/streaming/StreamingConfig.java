/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.component.aia.services.exteps.io.adapter.streaming;

public class StreamingConfig {

    public static final String[] NO_COMPONENT_ARRAY_DEFINED_BY_USER = new String[] {};

    private String host;
    private int port;
    private int userId;
    private int filterId;
    private int groupId;
    private boolean monitorOn;
    private long monitorPeriod;
    private boolean statisticsOn;
    private String[] componentArray;
    private String transportName;
    private int connectionRetries;

    /*
     * When this option is enabled by way of flow file (when attribute has value true), the operating system may use a
     * <em>keep-alive</em> mechanism to periodically probe the other end of the TCP connection when the connection is
     * otherwise idle, thereby determining if a connection is dead and notifying the application layer. The exact
     * semantics of the keep-alive mechanism is system dependent.
     *
     * TCP keep-alive under Linux: https://www.tldp.org/HOWTO/html_single/TCP-Keepalive-HOWTO/#usingkeepalive
     * Description of Redhat solution for keep-alive https://access.redhat.com/solutions/19029
     *
     * Default Redhat keep-alive settings:
     * tcp_keepalive_time: 7200 sec
     * tcp_keepalive_intvl: 75 sec
     * tcp_keepalive_probes: 9
     *
     * If the value of this option is false, then a keep-alive mechanism will not be used by the operating system, and
     * there will be the possibility that the TCP Socket will persist indefinitely when the remote side of the
     * connection terminates ungracefully.
     */
    private boolean connectionKeepAlive;

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int myUserIdParam) {
        userId = myUserIdParam;
    }

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(final int myFilterIdParam) {
        filterId = myFilterIdParam;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(final int myGroupIdParam) {
        groupId = myGroupIdParam;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String myHostParam) {
        host = myHostParam;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int myPortParam) {
        port = myPortParam;
    }

    public boolean isMonitorOn() {
        return monitorOn;
    }

    public void setMonitorOn(final boolean myMonitorOnParam) {
        monitorOn = myMonitorOnParam;
    }

    public long getMonitorPeriod() {
        return monitorPeriod;
    }

    public void setMonitorPeriod(final long myMonitorPeriodParam) {
        monitorPeriod = myMonitorPeriodParam;
    }

    public boolean isStatisticsOn() {
        return statisticsOn;
    }

    public void setStatisticsOn(final boolean myStatisticsOn) {
        statisticsOn = myStatisticsOn;
    }

    public String[] getComponentArray() {
        if (componentArray == null) {
            componentArray = NO_COMPONENT_ARRAY_DEFINED_BY_USER;
        }
        return componentArray;
    }

    public void setComponentArray(final String[] componentArray) {
        this.componentArray = componentArray;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(final String transportName) {
        this.transportName = transportName;
    }

    public int getConnectionRetries() {
        return connectionRetries;
    }

    public void setConnectionRetries(final int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    public boolean getConnectionKeepAlive() {
        return connectionKeepAlive;
    }

    public void setConnectionKeepAlive(final boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
    }
}
