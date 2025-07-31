package com.psh.registry.model;

public class SyncOperation {
    private String operationType; // REGISTER, UNREGISTER, HEARTBEAT
    private ServiceInstance serviceInstance;
    private String sourceInstanceId;
    private long timestamp;
    
    public SyncOperation() {}
    
    public SyncOperation(String operationType, ServiceInstance serviceInstance, String sourceInstanceId) {
        this.operationType = operationType;
        this.serviceInstance = serviceInstance;
        this.sourceInstanceId = sourceInstanceId;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }
    
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
    
    public String getSourceInstanceId() {
        return sourceInstanceId;
    }
    
    public void setSourceInstanceId(String sourceInstanceId) {
        this.sourceInstanceId = sourceInstanceId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 