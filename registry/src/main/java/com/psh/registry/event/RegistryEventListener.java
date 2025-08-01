package com.psh.registry.event;

import com.psh.registry.config.RegistryClusterConfig;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.model.ServiceInstance;
import com.psh.registry.service.RegistrySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RegistryEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistryEventListener.class);
    
    @Autowired
    private RegistryClusterConfig clusterConfig;
    
    @Autowired
    private RegistrySyncService syncService;
    
    @EventListener
    public void handleRegisterEvent(RegistryEvent event) {
        // 只处理注册事件
        if (!"REGISTER".equals(event.getEventType())) {
            return;
        }
        
        logger.info("收到注册事件: eventType={}, sourceInstanceId={}, serviceName={}, serviceId={}", 
                event.getEventType(), event.getSourceInstanceId(), 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
        
        // 只处理本地事件，避免循环同步
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            logger.info("跳过非本地事件: sourceInstanceId={}", event.getSourceInstanceId());
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            logger.info("同步功能已禁用，跳过事件处理: eventType={}, serviceName={}, serviceId={}", 
                    event.getEventType(), event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
            return;
        }
        
        logger.info("开始处理注册事件同步: serviceName={}, serviceId={}, ip={}, port={}", 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId(),
                event.getServiceInstance().getIpAddress(), event.getServiceInstance().getPort());
        
        // 使用简化的同步接口
        ServiceInstance instance = event.getServiceInstance();
        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceName", instance.getServiceName());
        payload.put("serviceId", instance.getServiceId());
        payload.put("ipAddress", instance.getIpAddress());
        payload.put("port", instance.getPort());
        payload.put("lastHeartbeat", System.currentTimeMillis()); // 添加心跳时间
        
        syncService.syncSimpleToOtherInstances("/api/sync/simple-register", payload);
        
        logger.info("注册事件同步处理完成: serviceName={}, serviceId={}", 
                instance.getServiceName(), instance.getServiceId());
    }
    
    @EventListener
    public void handleUnregisterEvent(RegistryEvent event) {
        // 只处理注销事件
        if (!"UNREGISTER".equals(event.getEventType())) {
            return;
        }
        
        logger.info("收到注销事件: eventType={}, sourceInstanceId={}, serviceName={}, serviceId={}", 
                event.getEventType(), event.getSourceInstanceId(), 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
        
        // 只处理本地事件，避免循环
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            logger.info("跳过非本地事件: sourceInstanceId={}", event.getSourceInstanceId());
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            logger.info("同步功能已禁用，跳过事件处理: eventType={}, serviceName={}, serviceId={}", 
                    event.getEventType(), event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
            return;
        }
        
        logger.info("开始处理注销事件同步: serviceName={}, serviceId={}, ip={}, port={}", 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId(),
                event.getServiceInstance().getIpAddress(), event.getServiceInstance().getPort());
        
        // 转换为同步操作并发送到其他实例
        SyncOperation operation = new SyncOperation("UNREGISTER", event.getServiceInstance(), 
                "registry-" + clusterConfig.getInstanceId());
        syncService.syncToOtherInstances("/api/sync/unregister", operation);
        
        logger.info("注销事件同步处理完成: serviceName={}, serviceId={}", 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
    }
    
    @EventListener
    public void handleHeartbeatEvent(RegistryEvent event) {
        // 只处理心跳事件
        if (!"HEARTBEAT".equals(event.getEventType())) {
            return;
        }
        
        logger.info("收到心跳事件: eventType={}, sourceInstanceId={}, serviceName={}, serviceId={}", 
                event.getEventType(), event.getSourceInstanceId(), 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
        
        // 只处理本地事件，避免循环
        if (!"registry-local".equals(event.getSourceInstanceId())) {
            logger.info("跳过非本地事件: sourceInstanceId={}", event.getSourceInstanceId());
            return;
        }
        
        if (!clusterConfig.isSyncEnabled()) {
            logger.info("同步功能已禁用，跳过事件处理: eventType={}, serviceName={}, serviceId={}", 
                    event.getEventType(), event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
            return;
        }
        
        logger.info("开始处理心跳事件同步: serviceName={}, serviceId={}, ip={}, port={}", 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId(),
                event.getServiceInstance().getIpAddress(), event.getServiceInstance().getPort());
        
        // 转换为同步操作并发送到其他实例
        SyncOperation operation = new SyncOperation("HEARTBEAT", event.getServiceInstance(), 
                "registry-" + clusterConfig.getInstanceId());
        syncService.syncToOtherInstances("/api/sync/heartbeat", operation);
        
        logger.info("心跳事件同步处理完成: serviceName={}, serviceId={}", 
                event.getServiceInstance().getServiceName(), event.getServiceInstance().getServiceId());
    }
} 