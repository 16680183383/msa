package com.psh.registry.controller;

import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.service.RegistrySyncService;
import com.psh.registry.service.ServiceRegistry;
import com.psh.registry.config.RegistryClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class RegistryController {

    private static final Logger logger = LoggerFactory.getLogger(RegistryController.class);
    
    private final ServiceRegistry registry;
    
    @Autowired
    private RegistrySyncService syncService;
    
    @Autowired
    private RegistryClusterConfig clusterConfig;

    public RegistryController(ServiceRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody ServiceInstance instance) {
        logger.info("收到服务注册请求: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        // 确保注册时设置心跳时间为当前时间
        instance.setLastHeartbeat(System.currentTimeMillis());
        registry.register(instance);
        
        logger.info("服务注册成功: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        return Map.of("code", 200);
    }

    @PostMapping("/unregister")
    public Map<String, Object> unregister(@RequestBody ServiceInstance instance) {
        logger.info("收到服务注销请求: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        registry.unregister(instance);
        
        logger.info("服务注销成功: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        return Map.of("code", 200);
    }

    @PostMapping("/heartbeat")
    public Map<String, Object> heartbeat(@RequestBody Map<String, Object> payload) {
        String serviceId = (String) payload.get("serviceId");
        String ip = (String) payload.get("ipAddress");
        int port = (int) payload.get("port");
        
        logger.debug("收到心跳请求: serviceId={}, ip={}, port={}", serviceId, ip, port);
        
        registry.heartbeat(serviceId, ip, port);
        
        logger.debug("心跳处理成功: serviceId={}, ip={}, port={}", serviceId, ip, port);
        
        return Map.of("code", 200);
    }

    @GetMapping("/discovery")
    public Object discover(@RequestParam(required = false) String name) {
        try {
            if (name != null) {
                logger.debug("收到服务发现请求: serviceName={}", name);
                ServiceInstance instance = registry.discover(name);
                if (instance != null) {
                    logger.debug("服务发现成功: serviceName={}, found={}", name, instance.getServiceId());
                } else {
                    logger.debug("服务发现失败: serviceName={}, 未找到服务", name);
                }
                return instance == null ? List.of() : List.of(instance);
            } else {
                logger.debug("收到所有服务查询请求");
                logger.debug("开始调用registry.getAllServices()");
                List<ServiceInstance> allServices = registry.getAllServices();
                logger.debug("registry.getAllServices()调用完成，返回 {} 个服务实例", allServices.size());
                logger.debug("准备返回结果");
                return allServices;
            }
        } catch (Exception e) {
            logger.error("服务发现过程中发生异常: name={}, error={}", name, e.getMessage(), e);
            return List.of();
        }
    }

    // 同步接口 - 处理来自其他registry实例的同步请求
    @PostMapping("/sync/register")
    public Map<String, Object> syncRegister(@RequestBody SyncOperation operation) {
        logger.info("收到同步注册请求: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        syncService.handleSyncRegister(operation);
        
        logger.info("同步注册处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        return Map.of("code", 200);
    }

    @PostMapping("/sync/unregister")
    public Map<String, Object> syncUnregister(@RequestBody SyncOperation operation) {
        logger.info("收到同步注销请求: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        syncService.handleSyncUnregister(operation);
        
        logger.info("同步注销处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        return Map.of("code", 200);
    }

    @PostMapping("/sync/heartbeat")
    public Map<String, Object> syncHeartbeat(@RequestBody SyncOperation operation) {
        logger.debug("收到同步心跳请求: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        syncService.handleSyncHeartbeat(operation);
        
        logger.debug("同步心跳处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                operation.getSourceInstanceId(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        return Map.of("code", 200);
    }

    // 简化的同步接口 - 直接传递服务信息
    @PostMapping("/sync/simple-register")
    public Map<String, Object> simpleSyncRegister(@RequestBody Map<String, Object> payload) {
        String serviceName = (String) payload.get("serviceName");
        String serviceId = (String) payload.get("serviceId");
        String ipAddress = (String) payload.get("ipAddress");
        int port = (int) payload.get("port");
        
        logger.info("收到简化同步注册请求: serviceName={}, serviceId={}, ip={}, port={}", 
                serviceName, serviceId, ipAddress, port);
        
        ServiceInstance instance = new ServiceInstance();
        instance.setServiceName(serviceName);
        instance.setServiceId(serviceId);
        instance.setIpAddress(ipAddress);
        instance.setPort(port);
        
        // 优先使用传入的心跳时间，如果没有则使用当前时间
        Object lastHeartbeatObj = payload.get("lastHeartbeat");
        if (lastHeartbeatObj != null) {
            instance.setLastHeartbeat((Long) lastHeartbeatObj);
        } else {
            instance.setLastHeartbeat(System.currentTimeMillis());
        }
        
        registry.registerSync(instance);
        
        logger.info("简化同步注册处理完成: serviceName={}, serviceId={}, ip={}, port={}", 
                serviceName, serviceId, ipAddress, port);
        
        return Map.of("code", 200);
    }

    // 每10秒清理一次超时节点
    @Scheduled(fixedRate = 10000)
    public void cleanUp() {
        logger.debug("开始清理超时服务实例");
        int beforeCount = registry.getAllServices().size();
        
        registry.removeExpiredInstances(60_000); // 60秒心跳超时
        
        int afterCount = registry.getAllServices().size();
        int removedCount = beforeCount - afterCount;
        
        if (removedCount > 0) {
            logger.info("清理完成: 移除了 {} 个超时服务实例", removedCount);
        } else {
            logger.debug("清理完成: 没有超时的服务实例");
        }
    }

    // 测试同步功能的端点
    @GetMapping("/test-sync")
    public Map<String, Object> testSync() {
        logger.info("收到同步功能测试请求");
        
        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", clusterConfig.getInstanceId());
        result.put("syncEnabled", clusterConfig.isSyncEnabled());
        result.put("clusterUrls", clusterConfig.getClusterUrls());
        result.put("otherUrls", clusterConfig.getOtherRegistryUrls());
        result.put("currentServices", registry.getAllServices().size());
        
        logger.info("同步功能测试结果: {}", result);
        
        return result;
    }
}
