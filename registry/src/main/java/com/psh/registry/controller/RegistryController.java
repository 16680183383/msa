package com.psh.registry.controller;

import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.service.RegistrySyncService;
import com.psh.registry.service.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RegistryController {

    private final ServiceRegistry registry;
    
    @Autowired
    private RegistrySyncService syncService;

    public RegistryController(ServiceRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody ServiceInstance instance) {
        registry.register(instance);
        return Map.of("code", 200);
    }

    @PostMapping("/unregister")
    public Map<String, Object> unregister(@RequestBody ServiceInstance instance) {
        registry.unregister(instance);
        return Map.of("code", 200);
    }

    @PostMapping("/heartbeat")
    public Map<String, Object> heartbeat(@RequestBody Map<String, Object> payload) {
        String serviceId = (String) payload.get("serviceId");
        String ip = (String) payload.get("ipAddress");
        int port = (int) payload.get("port");
        registry.heartbeat(serviceId, ip, port);
        return Map.of("code", 200);
    }

    @GetMapping("/discovery")
    public Object discover(@RequestParam(required = false) String name) {
        if (name != null) {
            ServiceInstance instance = registry.discover(name);
            return instance == null ? List.of() : List.of(instance);
        } else {
            return registry.getAllServices();
        }
    }

    // 同步接口 - 处理来自其他registry实例的同步请求
    @PostMapping("/sync/register")
    public Map<String, Object> syncRegister(@RequestBody SyncOperation operation) {
        syncService.handleSyncRegister(operation);
        return Map.of("code", 200);
    }

    @PostMapping("/sync/unregister")
    public Map<String, Object> syncUnregister(@RequestBody SyncOperation operation) {
        syncService.handleSyncUnregister(operation);
        return Map.of("code", 200);
    }

    @PostMapping("/sync/heartbeat")
    public Map<String, Object> syncHeartbeat(@RequestBody SyncOperation operation) {
        syncService.handleSyncHeartbeat(operation);
        return Map.of("code", 200);
    }

    // 简化的同步接口 - 直接传递服务信息
    @PostMapping("/sync/simple-register")
    public Map<String, Object> simpleSyncRegister(@RequestBody Map<String, Object> payload) {
        String serviceName = (String) payload.get("serviceName");
        String serviceId = (String) payload.get("serviceId");
        String ipAddress = (String) payload.get("ipAddress");
        int port = (int) payload.get("port");
        
        ServiceInstance instance = new ServiceInstance();
        instance.setServiceName(serviceName);
        instance.setServiceId(serviceId);
        instance.setIpAddress(ipAddress);
        instance.setPort(port);
        instance.setLastHeartbeat(System.currentTimeMillis());
        
        registry.registerSync(instance);
        
        return Map.of("code", 200);
    }

    // 每10秒清理一次超时节点
    @Scheduled(fixedRate = 10000)
    public void cleanUp() {
        registry.removeExpiredInstances(60_000); // 60秒心跳超时
    }
}
