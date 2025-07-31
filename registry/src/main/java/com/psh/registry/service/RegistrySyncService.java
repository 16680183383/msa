package com.psh.registry.service;

import com.psh.registry.config.RegistryClusterConfig;
import com.psh.registry.model.ServiceInstance;
import com.psh.registry.model.SyncOperation;
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
    
    @Autowired
    private RegistryClusterConfig clusterConfig;
    
    @Autowired
    private ServiceRegistry serviceRegistry;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 向其他registry实例发送同步请求
     */
    public void syncToOtherInstances(String endpoint, SyncOperation operation) {
        List<String> otherUrls = clusterConfig.getOtherRegistryUrls();
        
        for (String baseUrl : otherUrls) {
            try {
                String url = baseUrl + endpoint;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                restTemplate.postForEntity(url, new HttpEntity<>(operation, headers), Map.class);
            } catch (Exception e) {
                System.err.println("Failed to sync to " + baseUrl + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * 简化的同步方法 - 直接传递Map数据
     */
    public void syncSimpleToOtherInstances(String endpoint, Map<String, Object> payload) {
        List<String> otherUrls = clusterConfig.getOtherRegistryUrls();
        
        for (String baseUrl : otherUrls) {
            try {
                String url = baseUrl + endpoint;
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Map.class);
            } catch (Exception e) {
                System.err.println("Failed simple sync to " + baseUrl + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * 处理来自其他实例的同步注册请求
     */
    public void handleSyncRegister(SyncOperation operation) {
        if (operation.getSourceInstanceId().equals("registry-" + clusterConfig.getInstanceId())) {
            return; // 避免循环同步
        }
        
        if (operation.getServiceInstance() == null) {
            return;
        }
        
        serviceRegistry.registerSync(operation.getServiceInstance());
    }
    
    /**
     * 处理来自其他实例的同步注销请求
     */
    public void handleSyncUnregister(SyncOperation operation) {
        if (operation.getSourceInstanceId().equals("registry-" + clusterConfig.getInstanceId())) {
            return; // 避免循环同步
        }
        
        serviceRegistry.unregisterSync(operation.getServiceInstance());
    }
    
    /**
     * 处理来自其他实例的同步心跳请求
     */
    public void handleSyncHeartbeat(SyncOperation operation) {
        if (operation.getSourceInstanceId().equals("registry-" + clusterConfig.getInstanceId())) {
            return; // 避免循环同步
        }
        
        ServiceInstance instance = operation.getServiceInstance();
        serviceRegistry.heartbeatSync(instance.getServiceId(), instance.getIpAddress(), instance.getPort());
    }
} 