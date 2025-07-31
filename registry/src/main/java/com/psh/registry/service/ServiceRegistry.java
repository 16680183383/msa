package com.psh.registry.service;

import com.psh.registry.event.RegistryEvent;
import com.psh.registry.event.RegistryEventPublisher;
import com.psh.registry.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ServiceRegistry {

    private final Map<String, Map<String, ServiceInstance>> registry = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> roundRobinIndex = new ConcurrentHashMap<>();
    
    @Autowired
    private RegistryEventPublisher eventPublisher;

    public synchronized void register(ServiceInstance instance) {
        registry.putIfAbsent(instance.getServiceName(), new ConcurrentHashMap<>());
        registry.get(instance.getServiceName()).put(instance.getKey(), instance);
        
        // 发布注册事件，由事件监听器处理同步
        RegistryEvent event = new RegistryEvent("REGISTER", instance, "registry-local");
        eventPublisher.publishRegisterEvent(event);
    }

    public synchronized void unregister(ServiceInstance instance) {
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        if (serviceMap != null) {
            String key = instance.getKey();
            ServiceInstance existing = serviceMap.get(key);
            if (existing != null &&
                    existing.getServiceId().equals(instance.getServiceId()) &&
                    existing.getIpAddress().equals(instance.getIpAddress()) &&
                    existing.getPort() == instance.getPort()) {
                serviceMap.remove(key);
                
                // 发布注销事件，由事件监听器处理同步
                RegistryEvent event = new RegistryEvent("UNREGISTER", instance, "registry-local");
                eventPublisher.publishUnregisterEvent(event);
            }
        }
    }

    public synchronized void heartbeat(String serviceId, String ip, int port) {
        for (Map<String, ServiceInstance> services : registry.values()) {
            for (ServiceInstance instance : services.values()) {
                if (instance.getServiceId().equals(serviceId)
                        && instance.getIpAddress().equals(ip)
                        && instance.getPort() == port) {
                    instance.setLastHeartbeat(System.currentTimeMillis());
                    
                    // 发布心跳事件，由事件监听器处理同步
                    RegistryEvent event = new RegistryEvent("HEARTBEAT", instance, "registry-local");
                    eventPublisher.publishHeartbeatEvent(event);
                    return;
                }
            }
        }
    }

    // 同步方法 - 不发布事件，避免循环同步
    public synchronized void registerSync(ServiceInstance instance) {
        registry.putIfAbsent(instance.getServiceName(), new ConcurrentHashMap<>());
        registry.get(instance.getServiceName()).put(instance.getKey(), instance);
    }

    public synchronized void unregisterSync(ServiceInstance instance) {
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        if (serviceMap != null) {
            String key = instance.getKey();
            ServiceInstance existing = serviceMap.get(key);
            if (existing != null &&
                    existing.getServiceId().equals(instance.getServiceId()) &&
                    existing.getIpAddress().equals(instance.getIpAddress()) &&
                    existing.getPort() == instance.getPort()) {
                serviceMap.remove(key);
            }
        }
    }

    public synchronized void heartbeatSync(String serviceId, String ip, int port) {
        for (Map<String, ServiceInstance> services : registry.values()) {
            for (ServiceInstance instance : services.values()) {
                if (instance.getServiceId().equals(serviceId)
                        && instance.getIpAddress().equals(ip)
                        && instance.getPort() == port) {
                    instance.setLastHeartbeat(System.currentTimeMillis());
                    return;
                }
            }
        }
    }

    public synchronized List<ServiceInstance> getAllServices() {
        List<ServiceInstance> list = new ArrayList<>();
        for (Map<String, ServiceInstance> serviceMap : registry.values()) {
            list.addAll(serviceMap.values());
        }
        return list;
    }

    public synchronized ServiceInstance discover(String serviceName) {
        Map<String, ServiceInstance> services = registry.get(serviceName);
        if (services == null || services.isEmpty()) return null;

        List<ServiceInstance> list = new ArrayList<>(services.values());
        AtomicInteger index = roundRobinIndex.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int i = index.getAndIncrement() % list.size();
        return list.get(i);
    }

    public synchronized void removeExpiredInstances(long timeoutMillis) {
        long now = System.currentTimeMillis();
        for (Map<String, ServiceInstance> serviceMap : registry.values()) {
            serviceMap.values().removeIf(instance -> {
                boolean expired = now - instance.getLastHeartbeat() > timeoutMillis;
                if (expired) {
                    System.out.println("Removing expired instance: " + instance.getServiceId() + 
                            " (last heartbeat: " + (now - instance.getLastHeartbeat()) + "ms ago)");
                }
                return expired;
            });
        }
    }
}
