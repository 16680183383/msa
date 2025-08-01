package com.psh.client.service;

import com.psh.client.model.ServiceInstance;
import com.psh.client.model.HeartbeatRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class RegistryClient {

    private final String registryUrl = "http://localhost:8180";
    private final RestTemplate restTemplate = new RestTemplate();

    public void register(String serviceName, String serviceId, String ip, int port) {
        ServiceInstance instance = new ServiceInstance(serviceName, serviceId, ip, port);
        sendPostRequest("/api/register", instance);
    }

    public void unregister(String serviceName, String serviceId, String ip, int port) {
        ServiceInstance instance = new ServiceInstance(serviceName, serviceId, ip, port);
        sendPostRequest("/api/unregister", instance);
    }


    public void heartbeat(String serviceId, String ip, int port) {
        HeartbeatRequest request = new HeartbeatRequest(serviceId, ip, port);
        sendPostRequest("/api/heartbeat", request);
    }


    public Object discover(String serviceName) {
        String url = registryUrl + "/api/discovery";
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            url += "?name=" + serviceName;
        }
        
        return restTemplate.getForObject(url, Object.class);
    }


    private <T> void sendPostRequest(String endpoint, T body) {
        String url = registryUrl + endpoint;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }
}
