package com.psh.registry.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EasyResponse {
    private String error;
    private String result;

    public EasyResponse(String result) {
        this.error = null;
        this.result = result;
    }

    @Override
    public String toString() {
        return "ClientResponse{" +
                "error='" + error + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
} 