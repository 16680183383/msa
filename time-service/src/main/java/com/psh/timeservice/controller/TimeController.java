package com.psh.timeservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TimeController {

    @Value("${server.port}")
    private int port;

    @Value("${timeserver.instance.id}")
    private int instanceId;
    @GetMapping("/getDateTime")
    public Map<String, Object> getDateTime(@RequestParam(defaultValue = "full") String style){
        Map<String, Object> res = new HashMap<>();

        Instant now = Instant.now(); // UTC 时间

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
                res.put("result", null);
                res.put("serviceId", "time-service-" + instanceId);
                return res;
            }
        }

        res.put("result", result);
        res.put("serviceId", "time-service-" + instanceId);
        return res;
    }
}

