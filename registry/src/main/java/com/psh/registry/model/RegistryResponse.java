package com.psh.registry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistryResponse {
    private int code;
    private String message;
    private Object data;
    private List<ServiceInstance> services;

    public RegistryResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public RegistryResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RegistryResponse(int code, String message, List<ServiceInstance> services) {
        this.code = code;
        this.message = message;
        this.services = services;
    }

    @Override
    public String toString() {
        return "RegistryResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", services=" + services +
                '}';
    }
} 