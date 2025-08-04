package com.psh.registry.controller;

import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
import com.psh.registry.service.RegistrySyncService;
import com.psh.registry.service.ServiceRegistry;
import com.psh.registry.model.EasyResponse;

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
            EasyResponse response = registry.register(instance);
            
            if (response.getError() != null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", response.getError()));
            } else {
                String result = response.getResult();
                return ResponseEntity.ok(Map.of("result", result));
            }
        } catch (Exception e) {
            logger.error("服务注册失败: serviceName={}, error={}", instance.getServiceName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "服务注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/unregister")
    public ResponseEntity<Map<String, Object>> unregister(@RequestBody ServiceInstance instance) {
        try {
            logger.info("收到服务注销请求: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            
            EasyResponse response = registry.unregister(instance);
            
            if (response.getError() != null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", response.getError()));
            } else {
                return ResponseEntity.ok(Map.of("result", response.getResult()));
            }
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> payload) {
        try {
            String serviceId = (String) payload.get("serviceId");
            String ip = (String) payload.get("ipAddress");
            int port = (int) payload.get("port");
            
            logger.info("收到心跳请求: serviceId={}, ip={}, port={}", serviceId, ip, port);
            
            EasyResponse response = registry.heartbeat(serviceId, ip, port);
            
            if (response.getError() != null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", response.getError()));
            } else {
                return ResponseEntity.ok(Map.of("result", response.getResult()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/discovery")
    public ResponseEntity<List<Map<String, Object>>> discover(@RequestParam(required = false) String name) {
        try {
            if (name != null) {
                logger.info("收到服务发现请求: serviceName={}", name);
                ServiceInstance instance = registry.discover(name);
                if (instance != null) {
                    logger.info("服务发现成功: serviceName={}, found={}", name, instance.getServiceId());
                    return ResponseEntity.ok(List.of(instance.toResponseMap()));
                } else {
                    logger.info("服务发现失败: serviceName={}, 未找到服务", name);
                    return ResponseEntity.ok(List.of());
                }
            } else {
                logger.info("收到所有服务查询请求");
                List<ServiceInstance> allServices = registry.getAllServices();
                List<Map<String, Object>> responseList = allServices.stream()
                        .map(ServiceInstance::toResponseMap)
                        .toList();
                logger.info("返回 {} 个服务实例", responseList.size());
                return ResponseEntity.ok(responseList);
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
            syncService.handleSyncRegister(operation);

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

    @Scheduled(fixedRate = 10000)
    public void cleanUp() {
        registry.removeExpiredInstances(60_000); // 60秒心跳超时
    }

}
