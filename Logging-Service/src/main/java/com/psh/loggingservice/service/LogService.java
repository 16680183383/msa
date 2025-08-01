package com.psh.loggingservice.service;

import com.psh.loggingservice.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LogService {
    
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final List<LogEntry> logs = Collections.synchronizedList(new ArrayList<>());

    public void addLog(LogEntry logEntry) {
        logs.add(logEntry);
        
        // 使用SLF4J记录业务日志到文件
        logger.info("收到业务日志: serviceName={}, serviceId={}, datetime={}, level={}, message={}", 
                logEntry.getServiceName(), 
                logEntry.getServiceId(), 
                logEntry.getDatetime(), 
                logEntry.getLevel(), 
                logEntry.getMessage());
    }

    public List<LogEntry> getAllLogs() {
        return new ArrayList<>(logs);
    }
} 