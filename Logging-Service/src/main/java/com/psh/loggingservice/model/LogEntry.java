package com.psh.loggingservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    // Getters and Setters
    private String serviceName;    // 服务名
    private String serviceId;      // 服务ID
    private String datetime;       // 日期，带有毫秒部分 (GMT格式)
    private String level;          // 级别，目前只有 info
    private String message;        // 消息内容
    private LocalDateTime receivedAt; // 接收时间

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s: %s", 
            receivedAt, serviceName, serviceId, level, message);
    }
} 