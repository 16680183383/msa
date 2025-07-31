package com.psh.registry.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class RegistryEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public RegistryEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
        public void publishRegisterEvent(RegistryEvent event) {
        eventPublisher.publishEvent(event);
    }

    public void publishUnregisterEvent(RegistryEvent event) {
        eventPublisher.publishEvent(event);
    }

    public void publishHeartbeatEvent(RegistryEvent event) {
        eventPublisher.publishEvent(event);
    }
} 