package com.psh.client;


import com.psh.client.service.RegistryClient;
import com.psh.client.service.LogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Value("${server.port}")
    private int port;

    @Value("${client.instance.id:1}")
    private int instanceId;

    private String ip;
    private String serviceId;

    private final RegistryClient registryClient = new RegistryClient();
    private final LogService logService = new LogService();

    private final Timer heartbeatTimer = new Timer();
    private final Timer logTimer = new Timer();


    @Override
    public void run(String... args) throws Exception {
        ip = InetAddress.getLocalHost().getHostAddress();
        serviceId = "client-" + instanceId;

        // 注册
        registryClient.register("client", serviceId, ip, port);

        // 启动定时心跳
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                registryClient.heartbeat(serviceId, ip, port);
            }
        }, 0, 60_000); // 每60秒

        // 启动定时日志发送（可选功能）
        logTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logService.sendLog("client", serviceId, "info", "Client status is OK.");
            }
        }, 0, 1_000); // 每1秒
    }

    @PreDestroy
    public void shutdown() {
        registryClient.unregister("client", serviceId, ip, port);
        heartbeatTimer.cancel();
        logTimer.cancel();
    }
}
