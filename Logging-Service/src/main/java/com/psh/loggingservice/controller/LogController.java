package com.psh.loggingservice.controller;

import com.psh.loggingservice.model.LogEntry;
import com.psh.loggingservice.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 接收日志数据
     * POST /api/logs
     * 
     * 请求格式：
     * {
     *     "serviceName": "client",
     *     "serviceId": "client-1",
     *     "datetime": "2025-07-25 12:34:56.235",
     *     "level": "info",
     *     "message": "Client status is OK."
     * }
     */
    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> receiveLog(@RequestBody LogEntry logEntry) {
        logService.addLog(logEntry);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Log received"));
    }

    /**
     * 获取所有日志（用于验证）
     * GET /api/logs
     */
    @GetMapping("/logs")
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        List<LogEntry> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
} 