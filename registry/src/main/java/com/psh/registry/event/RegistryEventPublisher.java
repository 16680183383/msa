package com.psh.registry.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RegistryEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(RegistryEventPublisher.class);
    
    private final ApplicationEventPublisher eventPublisher;

    public RegistryEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishRegisterEvent(RegistryEvent event) {
        logger.debug("发布注册事件: serviceName={}, serviceId={}, sourceInstanceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId(),
                event.getSourceInstanceId());
        
        eventPublisher.publishEvent(event);
        
        logger.debug("注册事件发布完成: serviceName={}, serviceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId());
    }

    public void publishUnregisterEvent(RegistryEvent event) {
        logger.debug("发布注销事件: serviceName={}, serviceId={}, sourceInstanceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId(),
                event.getSourceInstanceId());
        
        eventPublisher.publishEvent(event);
        
        logger.debug("注销事件发布完成: serviceName={}, serviceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId());
    }

    public void publishHeartbeatEvent(RegistryEvent event) {
        logger.debug("发布心跳事件: serviceName={}, serviceId={}, sourceInstanceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId(),
                event.getSourceInstanceId());
        
        eventPublisher.publishEvent(event);
        
        logger.debug("心跳事件发布完成: serviceName={}, serviceId={}", 
                event.getServiceInstance().getServiceName(), 
                event.getServiceInstance().getServiceId());
    }
} 