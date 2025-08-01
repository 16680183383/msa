package com.psh.timeservice.model;

/**
 * 服务实例模型类
 */
public class ServiceInstance {
    private String serviceName;
    private String serviceId;
    private String ipAddress;
    private int port;

    public ServiceInstance() {
    }

    public ServiceInstance(String serviceName, String serviceId, String ipAddress, int port) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
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

    @Override
    public String toString() {
        return "ServiceInstance{" +
                "serviceName='" + serviceName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }
} 