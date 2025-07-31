package com.psh.timeservice;

import com.psh.timeservice.service.RegistryClient;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class TimeServiceApplication implements CommandLineRunner {

    @Value("${server.port}")
    private int port;

    @Value("${registry.base-url}")
    private String registryBaseUrl;

    @Value("${timeserver.instance.id}")
    private int instanceId;
    private String ip;
    private String serviceId;
    private RegistryClient registryClient;
    private final Timer timer = new Timer(true);

    public static void main(String[] args) {
        SpringApplication.run(TimeServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ip = InetAddress.getLocalHost().getHostAddress();
        serviceId = "time-service-" + instanceId;

        registryClient = new RegistryClient(registryBaseUrl);
        registryClient.register("time-service", serviceId, ip, port);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                registryClient.heartbeat(serviceId, ip, port);
            }
        }, 0, 60_000);
    }

    @PreDestroy
    public void shutdown() {
        try {
            registryClient.unregister("time-service", serviceId, ip, port);
        } catch (Exception ignored) {}
        timer.cancel();
    }
}

