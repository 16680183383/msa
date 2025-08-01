package com.psh.registry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInstance {
    private String serviceName;
    private String serviceId;
    private String ipAddress;
    private int port;
    private long lastHeartbeat;

    public String getKey() {
        return serviceId + "@" + ipAddress + ":" + port;
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
