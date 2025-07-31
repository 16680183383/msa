package com.psh.registry.model;

public class ServiceInstance {
    private String serviceName;
    private String serviceId;
    private String ipAddress;
    private int port;
    private long lastHeartbeat;

    public ServiceInstance(String serviceName, String serviceId, String ipAddress, int port, long lastHeartbeat) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.lastHeartbeat = lastHeartbeat;
    }

    public ServiceInstance() {
    }

    public String getKey() {
        return serviceId + "@" + ipAddress + ":" + port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }
    
    @Override
    public String toString() {
        return "ServiceInstance{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
