package com.psh.registry;

import com.psh.registry.config.RegistryClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RegistryApplication {

    private static final Logger logger = LoggerFactory.getLogger(RegistryApplication.class);

    public static void main(String[] args) {
        logger.info("正在启动注册中心应用...");
        
        SpringApplication.run(RegistryApplication.class, args);
        
        logger.info("注册中心应用启动完成");
    }
}
