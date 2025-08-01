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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class RegistrySyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrySyncService.class);
    
    @Autowired
    private RegistryClusterConfig clusterConfig;
    
    @Autowired
    private ServiceRegistry serviceRegistry;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public void syncToOtherInstances(String endpoint, SyncOperation operation) {
        List<String> otherUrls = clusterConfig.getOtherRegistryUrls();
        
        logger.info("开始向其他实例同步操作: endpoint={}, operationType={}, serviceName={}, serviceId={}, 目标实例数={}", 
                endpoint, operation.getOperationType(), 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId(),
                otherUrls.size());
        
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
    }

    public void syncSimpleToOtherInstances(String endpoint, Map<String, Object> payload) {
        List<String> otherUrls = clusterConfig.getOtherRegistryUrls();
        
        String serviceName = (String) payload.get("serviceName");
        String serviceId = (String) payload.get("serviceId");
        
        logger.info("开始简化同步操作: endpoint={}, serviceName={}, serviceId={}, 目标实例数={}", 
                endpoint, serviceName, serviceId, otherUrls.size());
        
        for (String baseUrl : otherUrls) {
            try {
                String url = baseUrl + endpoint;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                logger.info("向实例发送简化同步请求: url={}, serviceName={}, serviceId={}", 
                        url, serviceName, serviceId);
                
                restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Map.class);
                
                logger.info("简化同步请求发送成功: url={}, serviceName={}, serviceId={}", 
                        url, serviceName, serviceId);
                
            } catch (Exception e) {
                logger.error("简化同步请求发送失败: url={}, serviceName={}, serviceId={}, error={}", 
                        baseUrl + endpoint, serviceName, serviceId, e.getMessage(), e);
            }
        }
        
        logger.info("简化同步操作完成: endpoint={}, serviceName={}, serviceId={}", 
                endpoint, serviceName, serviceId);
    }

    public void handleSyncRegister(SyncOperation operation) {
        String sourceInstanceId = operation.getSourceInstanceId();
        String currentInstanceId = "registry-" + clusterConfig.getInstanceId();
        
        logger.debug("处理同步注册请求: sourceInstanceId={}, currentInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, currentInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        if (sourceInstanceId.equals(currentInstanceId)) {
            logger.debug("跳过循环同步: sourceInstanceId={}, currentInstanceId={}", sourceInstanceId, currentInstanceId);
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
        
        logger.debug("处理同步注销请求: sourceInstanceId={}, currentInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, currentInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        if (sourceInstanceId.equals(currentInstanceId)) {
            logger.debug("跳过循环同步: sourceInstanceId={}, currentInstanceId={}", sourceInstanceId, currentInstanceId);
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
        
        logger.debug("处理同步心跳请求: sourceInstanceId={}, currentInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, currentInstanceId, 
                operation.getServiceInstance().getServiceName(),
                operation.getServiceInstance().getServiceId());
        
        if (sourceInstanceId.equals(currentInstanceId)) {
            logger.debug("跳过循环同步: sourceInstanceId={}, currentInstanceId={}", sourceInstanceId, currentInstanceId);
            return; // 避免循环同步
        }
        
        if (operation.getServiceInstance() == null) {
            logger.warn("同步心跳请求失败: serviceInstance为null, sourceInstanceId={}", sourceInstanceId);
            return;
        }
        
        ServiceInstance instance = operation.getServiceInstance();
        
        logger.debug("开始处理同步心跳: sourceInstanceId={}, serviceName={}, serviceId={}, ip={}, port={}", 
                sourceInstanceId, 
                instance.getServiceName(),
                instance.getServiceId(),
                instance.getIpAddress(),
                instance.getPort());
        
        serviceRegistry.heartbeatSync(instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        logger.debug("同步心跳处理完成: sourceInstanceId={}, serviceName={}, serviceId={}", 
                sourceInstanceId, 
                instance.getServiceName(),
                instance.getServiceId());
    }
} 