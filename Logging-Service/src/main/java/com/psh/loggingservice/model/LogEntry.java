package com.psh.loggingservice.model;

import java.time.LocalDateTime;

public class LogEntry {
    private String serviceName;    // 服务名
    private String serviceId;      // 服务ID
    private String datetime;       // 日期，带有毫秒部分 (GMT格式)
    private String level;          // 级别，目前只有 info
    private String message;        // 消息内容
    private LocalDateTime receivedAt; // 接收时间

    public LogEntry() {
        this.receivedAt = LocalDateTime.now();
    }

    public LogEntry(String serviceName, String serviceId, String datetime, String level, String message) {
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.datetime = datetime;
        this.level = level;
        this.message = message;
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s: %s", 
            receivedAt, serviceName, serviceId, level, message);
    }
} 