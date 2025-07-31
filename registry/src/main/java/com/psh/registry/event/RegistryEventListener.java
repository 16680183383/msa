package com.psh.registry.event;

import com.psh.registry.config.RegistryClusterConfig;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.model.ServiceInstance;
import com.psh.registry.service.RegistrySyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RegistryEventListener {
    
    @Autowired
    private RegistryClusterConfig clusterConfig;
    
    @Autowired
    private RegistrySyncService syncService;
    
    @EventListener
    public void handleRegisterEvent(RegistryEvent event) {
        // 只处理本地事件，避免循环同步
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            return;
        }
        
        // 使用简化的同步接口
        ServiceInstance instance = event.getServiceInstance();
        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceName", instance.getServiceName());
        payload.put("serviceId", instance.getServiceId());
        payload.put("ipAddress", instance.getIpAddress());
        payload.put("port", instance.getPort());
        
        syncService.syncSimpleToOtherInstances("/api/sync/simple-register", payload);
    }
    
    @EventListener
    public void handleUnregisterEvent(RegistryEvent event) {
        // 只处理本地事件，避免循环
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            return;
        }
        
        // 转换为同步操作并发送到其他实例
        SyncOperation operation = new SyncOperation("UNREGISTER", event.getServiceInstance(), 
                "registry-" + clusterConfig.getInstanceId());
        syncService.syncToOtherInstances("/api/sync/unregister", operation);
    }
    
    @EventListener
    public void handleHeartbeatEvent(RegistryEvent event) {
        // 只处理本地事件，避免循环
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            return;
        }
        
        // 转换为同步操作并发送到其他实例
        SyncOperation operation = new SyncOperation("HEARTBEAT", event.getServiceInstance(), 
                "registry-" + clusterConfig.getInstanceId());
        syncService.syncToOtherInstances("/api/sync/heartbeat", operation);
    }
} 