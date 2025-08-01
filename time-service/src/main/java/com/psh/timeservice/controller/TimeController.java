package com.psh.timeservice.controller;

import com.psh.timeservice.model.TimeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api")
public class TimeController {

    @Value("${server.port}")
    private int port;

    @Value("${timeserver.instance.id}")
    private int instanceId;

    @GetMapping("/getDateTime")
    public ResponseEntity<TimeResponse> getDateTime(@RequestParam(defaultValue = "full") String style) {
        try {
            Instant now = Instant.now(); // UTC 时间
            String serviceId = "time-service-" + instanceId;

            String result;
            switch (style) {
                case "date" -> result = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withZone(ZoneId.of("UTC"))
                        .format(now);
                case "time" -> result = DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(ZoneId.of("UTC"))
                        .format(now);
                case "unix" -> result = String.valueOf(now.toEpochMilli());
                case "full" -> result = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.of("UTC"))
                        .format(now);
                default -> {
                    TimeResponse errorResponse = new TimeResponse("不支持的样式参数: " + style, serviceId);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
            }

            TimeResponse successResponse = new TimeResponse(result, serviceId);
            return ResponseEntity.ok(successResponse);
            
        } catch (Exception e) {
            TimeResponse errorResponse = new TimeResponse("时间服务内部错误: " + e.getMessage(), "time-service-" + instanceId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

