package com.psh.registry.controller;

import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.service.RegistrySyncService;
import com.psh.registry.service.ServiceRegistry;
import com.psh.registry.config.RegistryClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> register(@RequestBody ServiceInstance instance) {
        try {
            logger.info("收到服务注册请求: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            
            // 确保注册时设置心跳时间为当前时间
            instance.setLastHeartbeat(System.currentTimeMillis());
            registry.register(instance);
            
            logger.info("服务注册成功: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("服务注册失败: serviceName={}, error={}", instance.getServiceName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "服务注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/unregister")
    public ResponseEntity<Map<String, Object>> unregister(@RequestBody ServiceInstance instance) {
        try {
            logger.info("收到服务注销请求: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            
            registry.unregister(instance);
            
            logger.info("服务注销成功: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("服务注销失败: serviceName={}, error={}", instance.getServiceName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "服务注销失败: " + e.getMessage()));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> payload) {
        try {
            String serviceId = (String) payload.get("serviceId");
            String ip = (String) payload.get("ipAddress");
            int port = (int) payload.get("port");
            
            logger.info("收到心跳请求: serviceId={}, ip={}, port={}", serviceId, ip, port);
            
            registry.heartbeat(serviceId, ip, port);
            
            logger.info("心跳处理成功: serviceId={}, ip={}, port={}", serviceId, ip, port);
            
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("心跳处理失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "心跳处理失败: " + e.getMessage()));
        }
    }

    @GetMapping("/discovery")
    public ResponseEntity<List<ServiceInstance>> discover(@RequestParam(required = false) String name) {
        try {
            if (name != null) {
                logger.info("收到服务发现请求: serviceName={}", name);
                ServiceInstance instance = registry.discover(name);
                if (instance != null) {
                    logger.info("服务发现成功: serviceName={}, found={}", name, instance.getServiceId());
                    return ResponseEntity.ok(List.of(instance));
                } else {
                    logger.info("服务发现失败: serviceName={}, 未找到服务", name);
                    return ResponseEntity.ok(List.of());
                }
            } else {
                logger.info("收到所有服务查询请求");
                List<ServiceInstance> allServices = registry.getAllServices();
                logger.info("返回 {} 个服务实例", allServices.size());
                return ResponseEntity.ok(allServices);
            }
        } catch (Exception e) {
            logger.error("服务发现过程中发生异常: name={}, error={}", name, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 同步接口 - 处理来自其他registry实例的同步请求
    @PostMapping("/sync/register")
    public ResponseEntity<Map<String, Object>> syncRegister(@RequestBody SyncOperation operation) {
        try {
            logger.info("收到同步注册请求: sourceInstanceId={}, serviceName={}, serviceId={}", 
                    operation.getSourceInstanceId(), 
                    operation.getServiceInstance().getServiceName(),
                    operation.getServiceInstance().getServiceId());
            
            syncService.handleSyncRegister(operation);
            
            logger.info("同步注册处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                    operation.getSourceInstanceId(), 
                    operation.getServiceInstance().getServiceName(),
                    operation.getServiceInstance().getServiceId());
            
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("同步注册失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "同步注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/sync/unregister")
    public ResponseEntity<Map<String, Object>> syncUnregister(@RequestBody SyncOperation operation) {
        try {
            syncService.handleSyncUnregister(operation);
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("同步注销失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "同步注销失败: " + e.getMessage()));
        }
    }

    @PostMapping("/sync/heartbeat")
    public ResponseEntity<Map<String, Object>> syncHeartbeat(@RequestBody SyncOperation operation) {
        try {
            syncService.handleSyncHeartbeat(operation);

            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("同步心跳失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "同步心跳失败: " + e.getMessage()));
        }
    }

    // 简化的同步接口 - 直接传递服务信息
    @PostMapping("/sync/simple-register")
    public ResponseEntity<Map<String, Object>> simpleSyncRegister(@RequestBody Map<String, Object> payload) {
        try {
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
            
            return ResponseEntity.ok(Map.of("code", 200));
        } catch (Exception e) {
            logger.error("简化同步注册失败: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "error", "简化同步注册失败: " + e.getMessage()));
        }
    }

    // 每10秒清理一次超时节点
    @Scheduled(fixedRate = 10000)
    public void cleanUp() {
        logger.info("开始清理超时服务实例");
        int beforeCount = registry.getAllServices().size();
        
        registry.removeExpiredInstances(60_000); // 60秒心跳超时
        
        int afterCount = registry.getAllServices().size();
        int removedCount = beforeCount - afterCount;
        
        if (removedCount > 0) {
            logger.info("清理完成: 移除了 {} 个超时服务实例", removedCount);
        } else {
            logger.info("清理完成: 没有超时的服务实例");
        }
    }

}
