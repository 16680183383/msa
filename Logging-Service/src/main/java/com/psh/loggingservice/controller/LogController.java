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

    @PostMapping("/logs")
    public ResponseEntity<Map<String, String>> receiveLog(@RequestBody LogEntry logEntry) {
        logService.addLog(logEntry);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Log received"));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        List<LogEntry> logs = logService.getAllLogs();
        return ResponseEntity.ok(logs);
    }
} 