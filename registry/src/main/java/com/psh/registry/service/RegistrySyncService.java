package com.psh.registry.service;

import com.psh.registry.config.RegistryClusterConfig;
import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RegistrySyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrySyncService.class);
    
    @Autowired
    private RegistryClusterConfig clusterConfig;
    
    @Autowired
    private ServiceRegistry serviceRegistry;
    
    private final RestTemplate restTemplate;

    public RegistrySyncService() {
        // 创建带超时设置的RestTemplate
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 连接超时3秒
        factory.setReadTimeout(5000);    // 读取超时5秒
        this.restTemplate = new RestTemplate(factory);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> syncToOtherInstances(String endpoint, SyncOperation operation) {
        List<String> otherUrls = clusterConfig.getOtherRegistryUrls();
        
        for (String baseUrl : otherUrls) {
            try {
                String url = baseUrl + endpoint;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                logger.info("向实例发送同步请求: url={}, operationType={}, serviceName={}, serviceId={}", 
                        url, operation.getOperationType(), 
                        operation.getServiceInstance().getServiceName(),
                        operation.getServiceInstance().getServiceId());
                
                restTemplate.postForEntity(url, new HttpEntity<>(operation, headers), Map.class);
                
                logger.info("同步请求发送成功: url={}, operationType={}, serviceName={}, serviceId={}", 
                        url, operation.getOperationType(), 
                        operation.getServiceInstance().getServiceName(),
                        operation.getServiceInstance().getServiceId());
                
            } catch (Exception e) {
                logger.error("同步请求发送失败: url={}, operationType={}, serviceName={}, serviceId={}, error={}", 
                        baseUrl + endpoint, operation.getOperationType(), 
                        operation.getServiceInstance().getServiceName(),
                        operation.getServiceInstance().getServiceId(),
                        e.getMessage(), e);
            }
        }
        
        logger.info("向其他实例同步操作完成: endpoint={}, operationType={}, serviceName={}, serviceId={}", 
                endpoint, operation.getOperationType(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        return CompletableFuture.completedFuture(null);
    }



    public void handleSyncRegister(SyncOperation operation) {
        String sourceInstanceId = operation.getSourceInstanceId();
        String currentInstanceId = "registry-" + clusterConfig.getInstanceId();

        if (sourceInstanceId.equals(currentInstanceId)) {
            return; // 避免循环同步
        }
        
        if (operation.getServiceInstance() == null) {
            logger.warn("同步注册请求失败: serviceInstance为null, sourceInstanceId={}", sourceInstanceId);
            return;
        }
        
        logger.info("开始处理同步注册: sourceInstanceId={}, serviceName={}, serviceId={}, ip={}, port={}", 
                sourceInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId(),
                operation.getServiceInstance().getIpAddress(),
                operation.getServiceInstance().getPort());
        
        serviceRegistry.registerSync(operation.getServiceInstance());
        
        logger.info("同步注册处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
    }

    public void handleSyncUnregister(SyncOperation operation) {
        String sourceInstanceId = operation.getSourceInstanceId();
        String currentInstanceId = "registry-" + clusterConfig.getInstanceId();

        if (sourceInstanceId.equals(currentInstanceId)) {
            return; // 避免循环同步
        }
        
        if (operation.getServiceInstance() == null) {
            logger.warn("同步注销请求失败: serviceInstance为null, sourceInstanceId={}", sourceInstanceId);
            return;
        }
        
        logger.info("开始处理同步注销: sourceInstanceId={}, serviceName={}, serviceId={}, ip={}, port={}", 
                sourceInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId(),
                operation.getServiceInstance().getIpAddress(),
                operation.getServiceInstance().getPort());
        
        serviceRegistry.unregisterSync(operation.getServiceInstance());
        
        logger.info("同步注销处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
    }

    public void handleSyncHeartbeat(SyncOperation operation) {
        String sourceInstanceId = operation.getSourceInstanceId();
        String currentInstanceId = "registry-" + clusterConfig.getInstanceId();
        
        logger.info("处理同步心跳请求: sourceInstanceId={}, currentInstanceId={}, serviceName={}, serviceId={}",
                sourceInstanceId, currentInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        if (sourceInstanceId.equals(currentInstanceId)) {
            return; // 避免循环同步
        }
        
        if (operation.getServiceInstance() == null) {
            logger.warn("同步心跳请求失败: serviceInstance为null, sourceInstanceId={}", sourceInstanceId);
            return;
        }
        
        ServiceInstance instance = operation.getServiceInstance();

        serviceRegistry.heartbeatSync(instance.getServiceId(), instance.getIpAddress(), instance.getPort());

    }
} 