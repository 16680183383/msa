package com.psh.registry.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegistryClusterConfig {

    private static final Logger logger = LoggerFactory.getLogger(RegistryClusterConfig.class);

    @Value("${registry.instance.id:1}")
    private String instanceId;

    @Value("${registry.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${registry.cluster.urls:}")
    private String clusterUrls;

    public String getInstanceId() {
        return instanceId;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public String getClusterUrls() {
        return clusterUrls;
    }

    public List<String> getOtherRegistryUrls() {
        List<String> urls = new ArrayList<>();
        
        if (clusterUrls != null && !clusterUrls.trim().isEmpty()) {
            String[] urlArray = clusterUrls.split(",");
            for (String url : urlArray) {
                String trimmedUrl = url.trim();
                if (!trimmedUrl.isEmpty()) {
                    // 排除当前实例的URL
                    if (!isCurrentInstanceUrl(trimmedUrl)) {
                        urls.add(trimmedUrl);
                    }
                }
            }
        }
        
        logger.debug("获取其他注册中心实例URL: instanceId={}, syncEnabled={}, clusterUrls={}, otherUrls={}", 
                instanceId, syncEnabled, clusterUrls, urls);
        
        return urls;
    }

    private boolean isCurrentInstanceUrl(String url) {
        // 获取当前实例的端口
        String currentPort = getServerPort();
        
        // 检查URL是否包含当前端口
        if (url.contains(":" + currentPort)) {
            logger.debug("排除当前实例URL: url={}, currentPort={}", url, currentPort);
            return true;
        }
        
        return false;
    }

    private String getServerPort() {
        // 从系统属性或环境变量获取端口
        String port = System.getProperty("server.port");
        if (port == null) {
            port = System.getenv("SERVER_PORT");
        }
        if (port == null) {
            // 根据instanceId推断端口
            switch (instanceId) {
                case "1":
                    port = "8180";
                    break;
                case "2":
                    port = "8181";
                    break;
                case "3":
                    port = "8182";
                    break;
                default:
                    port = "8180";
            }
        }
        return port;
    }
} 