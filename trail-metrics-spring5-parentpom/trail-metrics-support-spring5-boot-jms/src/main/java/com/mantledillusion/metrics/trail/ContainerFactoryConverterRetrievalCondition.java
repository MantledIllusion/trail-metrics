package com.mantledillusion.metrics.trail;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;

class ContainerFactoryConverterRetrievalCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            AbstractJmsListenerContainerFactory.class.getDeclaredField("messageConverter").setAccessible(true);
            return true;
        } catch (NoSuchFieldException | SecurityException e) {
            return false;
        }
    }
}
