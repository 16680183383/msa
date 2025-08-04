package com.psh.registry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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
        return serviceName + "@" + ipAddress + ":" + port;
    }

    /**
     * 返回用于API响应的Map，不包含内部字段
     */
    public Map<String, Object> toResponseMap() {
        Map<String, Object> response = new HashMap<>();
        response.put("serviceName", serviceName);
        response.put("serviceId", serviceId);
        response.put("ipAddress", ipAddress);
        response.put("port", port);
        return response;
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
