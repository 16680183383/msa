package com.psh.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class LogService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String logCollectorUrl = "http://localhost:8480/api/logs"; // 日志收集服务端口为8480
    
    public void sendLog(String serviceName, String serviceId, String level, String message) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("serviceName", serviceName);
            logData.put("serviceId", serviceId);
            
            // 生成GMT时间，带毫秒
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            logData.put("datetime", gmtFormat.format(new Date()));
            
            logData.put("level", level);
            logData.put("message", message);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            restTemplate.postForEntity(logCollectorUrl, new HttpEntity<>(logData, headers), String.class);
        } catch (Exception e) {
            // 日志发送失败时不抛出异常，避免影响主业务
            System.err.println("Failed to send log: " + e.getMessage());
        }
    }
} 