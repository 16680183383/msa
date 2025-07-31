package com.psh.timeservice.service;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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

    private HttpEntity<Map<String, Object>> buildRequest(String serviceName, String serviceId, String ip, int port) {
        Map<String, Object> body = new HashMap<>();
        body.put("serviceName", serviceName);
        body.put("serviceId", serviceId);
        body.put("ipAddress", ip);
        body.put("port", port);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public void register(String serviceName, String serviceId, String ip, int port) {
        String url = registryUrl + "/api/register";
        restTemplate.postForEntity(url, buildRequest(serviceName, serviceId, ip, port), String.class);
    }

    public void unregister(String serviceName, String serviceId, String ip, int port) {
        String url = registryUrl + "/api/unregister";
        restTemplate.postForEntity(url, buildRequest(serviceName, serviceId, ip, port), String.class);
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
}
