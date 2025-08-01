package com.psh.timeservice.service;

import com.psh.timeservice.model.ServiceInstance;
import com.psh.timeservice.model.HeartbeatRequest;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RegistryClient {

    private final RestTemplate restTemplate;
    private final String registryUrl;

    public RegistryClient(String registryUrl) {
        this.registryUrl = registryUrl;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(2000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 注册服务
     */
    public void register(String serviceName, String serviceId, String ip, int port) {
        ServiceInstance instance = new ServiceInstance(serviceName, serviceId, ip, port);
        sendPostRequest("/api/register", instance);
    }

    /**
     * 注销服务
     */
    public void unregister(String serviceName, String serviceId, String ip, int port) {
        ServiceInstance instance = new ServiceInstance(serviceName, serviceId, ip, port);
        sendPostRequest("/api/unregister", instance);
    }

    /**
     * 发送心跳
     */
    public void heartbeat(String serviceId, String ip, int port) {
        HeartbeatRequest request = new HeartbeatRequest(serviceId, ip, port);
        sendPostRequest("/api/heartbeat", request);
    }

    /**
     * 发送POST请求的通用方法
     */
    private <T> void sendPostRequest(String endpoint, T body) {
        String url = registryUrl + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}
