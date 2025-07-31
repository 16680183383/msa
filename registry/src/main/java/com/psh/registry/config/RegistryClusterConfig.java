package com.psh.registry.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RegistryClusterConfig {
    
    @Value("${registry.instance.id:1}")
    private int instanceId;
    
    @Value("${registry.sync.enabled:true}")
    private boolean syncEnabled;
    
    // 定义所有registry实例的端口
    private static final List<Integer> REGISTRY_PORTS = Arrays.asList(8180, 8181, 8182);
    
    public int getInstanceId() {
        return instanceId;
    }
    
    public boolean isSyncEnabled() {
        return syncEnabled;
    }
    
    public List<Integer> getRegistryPorts() {
        return REGISTRY_PORTS;
    }
    
    public List<String> getOtherRegistryUrls() {
        return REGISTRY_PORTS.stream()
                .filter(port -> port != getCurrentPort())
                .map(port -> "http://localhost:" + port)
                .toList();
    }
    
    private int getCurrentPort() {
        return 8180 + instanceId - 1;
    }
} 