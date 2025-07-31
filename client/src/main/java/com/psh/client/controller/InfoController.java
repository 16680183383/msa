package com.psh.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class InfoController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String registryUrl = "http://localhost:8180/api/discovery?name=time-service";

    @GetMapping("/getInfo")
    public Map<String, Object> getInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode serviceNode = restTemplate.getForObject(registryUrl, JsonNode.class);

            if (serviceNode == null || !serviceNode.isArray() || serviceNode.size() == 0) {
                response.put("error", "time-service 不可用");
                response.put("result", null);
                return response;
            }

            JsonNode service = serviceNode.get(0);
            String ip = service.get("ipAddress").asText();
            int port = service.get("port").asInt();
            String serviceId = service.get("serviceId").asText();

            String url = String.format("http://%s:%d/api/getDateTime?style=full", ip, port);

            JsonNode timeNode = restTemplate.getForObject(url, JsonNode.class);
            String timeStr = timeNode.get("result").asText();

            // 将GMT时间转换为北京时间（+8）
            SimpleDateFormat gmtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date gmtDate = gmtFormat.parse(timeStr);

            SimpleDateFormat bjFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            bjFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

            String result = String.format("Hello Kingsoft Cloud Star Camp - %s - %s",
                    serviceId,
                    bjFormat.format(gmtDate));

            response.put("error", null);
            response.put("result", result);
        } catch (Exception e) {
            response.put("error", "服务调用失败: " + e.getMessage());
            response.put("result", null);
        }

        return response;
    }
}