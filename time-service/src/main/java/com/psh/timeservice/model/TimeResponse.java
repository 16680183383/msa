package com.psh.timeservice.model;

/**
 * 时间服务响应类
 */
public class TimeResponse {
    private String result;
    private String serviceId;

    public TimeResponse() {
    }

    public TimeResponse(String result, String serviceId) {
        this.result = result;
        this.serviceId = serviceId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "TimeResponse{" +
                "result='" + result + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
} 