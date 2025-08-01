package com.psh.timeservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TimeResponse {
    private String result;
    private String serviceId;

    @Override
    public String toString() {
        return "TimeResponse{" +
                "result='" + result + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
} 