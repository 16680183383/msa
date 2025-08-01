package com.psh.client.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class RegistryClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String registryUrl = "http://localhost:8180"; // 如有多个注册中心需扩展

    public void register(String serviceName, String serviceId, String ip, int port) {
        String url = registryUrl + "/api/register";

        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", serviceName);
        body.put("serviceId", serviceId);
        body.put("ipAddress", ip);
        body.put("port", port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public void unregister(String serviceName, String serviceId, String ip, int port) {
        String url = registryUrl + "/api/unregister";

        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", serviceName);
        body.put("serviceId", serviceId);
        body.put("ipAddress", ip);
        body.put("port", port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public void heartbeat(String serviceId, String ip, int port) {
        String url = registryUrl + "/api/heartbeat";

        Map<String, Object> body = new HashMap<>();
        body.put("serviceId", serviceId);
        body.put("ipAddress", ip);
        body.put("port", port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public Object discover(String serviceName) {
        String url = registryUrl + "/api/discovery";
        if (serviceName != null && !serviceName.trim().isEmpty()) {
            url += "?name=" + serviceName;
        }
        
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getAllServices() {
        return discover(null);
    }
}
