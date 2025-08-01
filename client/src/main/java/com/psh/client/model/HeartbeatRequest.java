package com.psh.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatRequest {
    private String serviceId;
    private String ipAddress;
    private int port;

    @Override
    public String toString() {
        return "HeartbeatRequest{" +
                "serviceId='" + serviceId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }
} 