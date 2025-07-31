package com.psh.loggingservice.service;

import com.psh.loggingservice.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LogService {
    
    // 使用内存存储日志
    private final List<LogEntry> logs = Collections.synchronizedList(new ArrayList<>());

    /**
     * 添加日志
     * @param logEntry 日志条目
     */
    public void addLog(LogEntry logEntry) {
        logs.add(logEntry);
        
        // 打印日志到控制台
        System.out.println("Received log: " + logEntry);
    }

    /**
     * 获取所有日志
     * @return 所有日志列表
     */
    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(logs);
    }
} 