package com.psh.registry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SyncOperation {
    // Getters and Setters
    private String operationType; // REGISTER, UNREGISTER, HEARTBEAT
    private ServiceInstance serviceInstance;
    private String sourceInstanceId;
    private long timestamp;
    
    public SyncOperation(String operationType, ServiceInstance serviceInstance, String sourceInstanceId) {
        this.operationType = operationType;
        this.serviceInstance = serviceInstance;
        this.sourceInstanceId = sourceInstanceId;
        this.timestamp = System.currentTimeMillis();
    }

}