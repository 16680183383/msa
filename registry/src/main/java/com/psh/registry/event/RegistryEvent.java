package com.psh.registry.event;

import com.psh.registry.model.ServiceInstance;

public class RegistryEvent {
    private String eventType; // REGISTER, UNREGISTER, HEARTBEAT
    private ServiceInstance serviceInstance;
    private String serviceId;
    private String ipAddress;
    private int port;
    private String sourceInstanceId;
    
    public RegistryEvent() {}
    
    public RegistryEvent(String eventType, ServiceInstance serviceInstance, String sourceInstanceId) {
        this.eventType = eventType;
        this.serviceInstance = serviceInstance;
        this.sourceInstanceId = sourceInstanceId;
    }
    
    public RegistryEvent(String eventType, String serviceId, String ipAddress, int port, String sourceInstanceId) {
        this.eventType = eventType;
        this.serviceId = serviceId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.sourceInstanceId = sourceInstanceId;
    }
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }
    
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
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
    
    public String getSourceInstanceId() {
        return sourceInstanceId;
    }
    
    public void setSourceInstanceId(String sourceInstanceId) {
        this.sourceInstanceId = sourceInstanceId;
    }
} 