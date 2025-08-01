package com.psh.timeservice.model;

/**
 * 心跳请求模型类
 */
public class HeartbeatRequest {
    private String serviceId;
    private String ipAddress;
    private int port;

    public HeartbeatRequest() {
    }

    public HeartbeatRequest(String serviceId, String ipAddress, int port) {
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "HeartbeatRequest{" +
                "serviceId='" + serviceId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }
} 