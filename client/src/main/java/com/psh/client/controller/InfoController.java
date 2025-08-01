package com.psh.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psh.client.model.ClientResponse;
import com.psh.client.service.RegistryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class InfoController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final RegistryClient registryClient = new RegistryClient();

    @GetMapping("/getInfo")
    public ResponseEntity<ClientResponse> getInfo() {
        try {
            // 使用RegistryClient进行服务发现
            Object serviceResult = registryClient.discover("time-service");
            
            if (serviceResult == null) {
                ClientResponse errorResponse = new ClientResponse("time-service 不可用", null);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

            // 将Object转换为JsonNode
            JsonNode serviceNode = mapper.valueToTree(serviceResult);

            if (!serviceNode.isArray() || serviceNode.size() == 0) {
                ClientResponse errorResponse = new ClientResponse("time-service 不可用", null);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

            JsonNode service = serviceNode.get(0);
            String ip = service.get("ipAddress").asText();
            int port = service.get("port").asInt();
            String serviceId = service.get("serviceId").asText();

            String url = String.format("http://%s:%d/api/getDateTime?style=full", ip, port);

            JsonNode timeNode = restTemplate.getForObject(url, JsonNode.class);
            
            if (timeNode == null || !timeNode.has("result")) {
                ClientResponse errorResponse = new ClientResponse("time-service 响应格式错误", null);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }
            
            String timeStr = timeNode.get("result").asText();
            
            // 检查时间字符串是否包含错误信息
            if (timeStr.startsWith("不支持的样式参数:") || timeStr.startsWith("时间服务内部错误:")) {
                ClientResponse errorResponse = new ClientResponse("time-service 错误: " + timeStr, null);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
            }

            // 将GMT时间转换为北京时间（+8）
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date gmtDate = gmtFormat.parse(timeStr);

            SimpleDateFormat bjFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            bjFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            String result = String.format("Hello Kingsoft Cloud Star Camp - %s - %s",
                    serviceId,
                    bjFormat.format(gmtDate));

            ClientResponse successResponse = new ClientResponse(result);
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            ClientResponse errorResponse = new ClientResponse("服务调用失败: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}