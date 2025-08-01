package com.psh.registry.service;

import com.psh.registry.event.RegistryEvent;
import com.psh.registry.event.RegistryEventPublisher;
import com.psh.registry.model.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private final Map<String, Map<String, ServiceInstance>> registry = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> roundRobinIndex = new ConcurrentHashMap<>();
    
    @Autowired
    private RegistryEventPublisher eventPublisher;

    public synchronized void register(ServiceInstance instance) {
        logger.info("开始注册服务: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        // 设置注册时的心跳时间为当前时间
        instance.setLastHeartbeat(System.currentTimeMillis());
        
        registry.putIfAbsent(instance.getServiceName(), new ConcurrentHashMap<>());
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        
        // 检查是否已存在相同的服务实例
        ServiceInstance existing = serviceMap.get(instance.getKey());
        if (existing != null) {
            logger.warn("服务实例已存在，将进行更新: serviceName={}, serviceId={}, ip={}, port={}", 
                    instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        }
        
        serviceMap.put(instance.getKey(), instance);
        
        logger.info("服务注册完成: serviceName={}, serviceId={}, ip={}, port={}, 当前该服务共有 {} 个实例", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort(),
                serviceMap.size());
        
        // 发布注册事件，由事件监听器处理同步
        RegistryEvent event = new RegistryEvent("REGISTER", instance, "registry-local");
        eventPublisher.publishRegisterEvent(event);
        
        logger.debug("已发布注册事件: serviceName={}, serviceId={}", 
                instance.getServiceName(), instance.getServiceId());
    }

    public synchronized void unregister(ServiceInstance instance) {
        logger.info("开始注销服务: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        if (serviceMap != null) {
            String key = instance.getKey();
            ServiceInstance existing = serviceMap.get(key);
            if (existing != null &&
                    existing.getServiceId().equals(instance.getServiceId()) &&
                    existing.getIpAddress().equals(instance.getIpAddress()) &&
                    existing.getPort() == instance.getPort()) {
                serviceMap.remove(key);
                
                logger.info("服务注销完成: serviceName={}, serviceId={}, ip={}, port={}, 当前该服务剩余 {} 个实例", 
                        instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort(),
                        serviceMap.size());
                
                // 发布注销事件，由事件监听器处理同步
                RegistryEvent event = new RegistryEvent("UNREGISTER", instance, "registry-local");
                eventPublisher.publishUnregisterEvent(event);
                
                logger.debug("已发布注销事件: serviceName={}, serviceId={}", 
                        instance.getServiceName(), instance.getServiceId());
            } else {
                logger.warn("服务注销失败，未找到匹配的服务实例: serviceName={}, serviceId={}, ip={}, port={}", 
                        instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            }
        } else {
            logger.warn("服务注销失败，服务不存在: serviceName={}", instance.getServiceName());
        }
    }

    public synchronized void heartbeat(String serviceId, String ip, int port) {
        logger.debug("处理心跳请求: serviceId={}, ip={}, port={}", serviceId, ip, port);
        
        boolean found = false;
        for (Map<String, ServiceInstance> services : registry.values()) {
            for (ServiceInstance instance : services.values()) {
                if (instance.getServiceId().equals(serviceId)
                        && instance.getIpAddress().equals(ip)
                        && instance.getPort() == port) {
                    long oldHeartbeat = instance.getLastHeartbeat();
                    instance.setLastHeartbeat(System.currentTimeMillis());
                    found = true;
                    
                    logger.debug("心跳更新成功: serviceName={}, serviceId={}, ip={}, port={}, 上次心跳={}, 当前心跳={}", 
                            instance.getServiceName(), serviceId, ip, port, 
                            new Date(oldHeartbeat), new Date(instance.getLastHeartbeat()));
                    
                    // 发布心跳事件，由事件监听器处理同步
                    RegistryEvent event = new RegistryEvent("HEARTBEAT", instance, "registry-local");
                    eventPublisher.publishHeartbeatEvent(event);
                    
                    logger.debug("已发布心跳事件: serviceName={}, serviceId={}", 
                            instance.getServiceName(), instance.getServiceId());
                    return;
                }
            }
        }
        
        if (!found) {
            logger.warn("心跳处理失败，未找到服务实例: serviceId={}, ip={}, port={}", serviceId, ip, port);
        }
    }

    // 同步方法 - 不发布事件，避免循环同步
    public synchronized void registerSync(ServiceInstance instance) {
        logger.info("开始同步注册服务: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        // 确保同步注册时也设置心跳时间
        if (instance.getLastHeartbeat() == 0) {
            instance.setLastHeartbeat(System.currentTimeMillis());
        }
        
        registry.putIfAbsent(instance.getServiceName(), new ConcurrentHashMap<>());
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        
        ServiceInstance existing = serviceMap.get(instance.getKey());
        if (existing != null) {
            logger.debug("同步注册时发现已存在的服务实例，将进行更新: serviceName={}, serviceId={}", 
                    instance.getServiceName(), instance.getServiceId());
        }
        
        serviceMap.put(instance.getKey(), instance);
        
        logger.info("同步注册完成: serviceName={}, serviceId={}, ip={}, port={}, 当前该服务共有 {} 个实例", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort(),
                serviceMap.size());
    }

    public synchronized void unregisterSync(ServiceInstance instance) {
        logger.info("开始同步注销服务: serviceName={}, serviceId={}, ip={}, port={}", 
                instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
        
        Map<String, ServiceInstance> serviceMap = registry.get(instance.getServiceName());
        if (serviceMap != null) {
            String key = instance.getKey();
            ServiceInstance existing = serviceMap.get(key);
            if (existing != null &&
                    existing.getServiceId().equals(instance.getServiceId()) &&
                    existing.getIpAddress().equals(instance.getIpAddress()) &&
                    existing.getPort() == instance.getPort()) {
                serviceMap.remove(key);
                
                logger.info("同步注销完成: serviceName={}, serviceId={}, ip={}, port={}, 当前该服务剩余 {} 个实例", 
                        instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort(),
                        serviceMap.size());
            } else {
                logger.warn("同步注销失败，未找到匹配的服务实例: serviceName={}, serviceId={}, ip={}, port={}", 
                        instance.getServiceName(), instance.getServiceId(), instance.getIpAddress(), instance.getPort());
            }
        } else {
            logger.warn("同步注销失败，服务不存在: serviceName={}", instance.getServiceName());
        }
    }

    public synchronized void heartbeatSync(String serviceId, String ip, int port) {
        logger.debug("处理同步心跳请求: serviceId={}, ip={}, port={}", serviceId, ip, port);
        
        boolean found = false;
        for (Map<String, ServiceInstance> services : registry.values()) {
            for (ServiceInstance instance : services.values()) {
                if (instance.getServiceId().equals(serviceId)
                        && instance.getIpAddress().equals(ip)
                        && instance.getPort() == port) {
                    long oldHeartbeat = instance.getLastHeartbeat();
                    instance.setLastHeartbeat(System.currentTimeMillis());
                    found = true;
                    
                    logger.debug("同步心跳更新成功: serviceName={}, serviceId={}, ip={}, port={}, 上次心跳={}, 当前心跳={}", 
                            instance.getServiceName(), serviceId, ip, port, 
                            new Date(oldHeartbeat), new Date(instance.getLastHeartbeat()));
                    return;
                }
            }
        }
        
        if (!found) {
            logger.warn("同步心跳处理失败，未找到服务实例: serviceId={}, ip={}, port={}", serviceId, ip, port);
        }
    }

    public synchronized List<ServiceInstance> getAllServices() {
        logger.info("获取所有服务实例");
        List<ServiceInstance> list = new ArrayList<>();
        
        try {
            for (Map<String, ServiceInstance> serviceMap : registry.values()) {
                list.addAll(serviceMap.values());
            }
            logger.info("获取所有服务实例，共 {} 个", list.size());
            return list;
        } catch (Exception e) {
            logger.error("getAllServices()方法执行异常: {}", e.getMessage(), e);
            return list;
        }
    }

    public synchronized ServiceInstance discover(String serviceName) {
        logger.info("服务发现请求: serviceName={}", serviceName);
        
        Map<String, ServiceInstance> serviceMap = registry.get(serviceName);
        if (serviceMap == null || serviceMap.isEmpty()) {
            logger.info("服务发现失败: serviceName={}, 服务不存在或没有实例", serviceName);
            return null;
        }
        
        // 获取所有可用的服务实例
        List<ServiceInstance> availableInstances = new ArrayList<>(serviceMap.values());
        
        // 使用轮询算法选择实例
        AtomicInteger index = roundRobinIndex.computeIfAbsent(serviceName, k -> new AtomicInteger(0));
        int currentIndex = index.getAndIncrement() % availableInstances.size();
        ServiceInstance selectedInstance = availableInstances.get(currentIndex);
        
        logger.info("服务发现成功: serviceName={}, serviceId={}, 选择实例={}, 总实例数={}", 
                serviceName, selectedInstance.getServiceId(), currentIndex, availableInstances.size());
        
        return selectedInstance;
    }

    public synchronized void removeExpiredInstances(long timeout) {
        logger.info("开始清理超时服务实例，超时时间: {}ms", timeout);
        
        long currentTime = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map<String, ServiceInstance> serviceMap : registry.values()) {
            Iterator<Map.Entry<String, ServiceInstance>> iterator = serviceMap.entrySet().iterator();
            while (iterator.hasNext()) {
                ServiceInstance instance = iterator.next().getValue();
                if (currentTime - instance.getLastHeartbeat() > timeout) {
                    iterator.remove();
                    removedCount++;
                    logger.info("移除超时服务实例: serviceName={}, serviceId={}", 
                            instance.getServiceName(), instance.getServiceId());
                }
            }
        }
        
        logger.info("超时服务实例清理完成: 移除了 {} 个实例", removedCount);
    }
}
