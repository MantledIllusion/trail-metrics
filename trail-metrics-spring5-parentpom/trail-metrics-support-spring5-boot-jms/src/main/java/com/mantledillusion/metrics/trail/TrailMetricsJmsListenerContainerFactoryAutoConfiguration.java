package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

@Configuration
@Conditional(ContainerFactoryConverterRetrievalCondition.class)
@AutoConfigureAfter(JmsAutoConfiguration.class)
public class TrailMetricsJmsListenerContainerFactoryAutoConfiguration {

    @Value("${"+TrailMetricsJmsMessageConverterWrapper.PRTY_INCOMING_MODE+":"+TrailMetricsJmsMessageConverterWrapper.DEFAULT_INCOMING_MODE+"}")
    private String incomingMode;
    @Value("${"+TrailMetricsJmsMessageConverterWrapper.PRTY_OUTGOING_MODE+":"+TrailMetricsJmsMessageConverterWrapper.DEFAULT_OUTGOING_MODE+"}")
    private String outgoingMode;

    @Autowired
    private List<AbstractJmsListenerContainerFactory<?>> containerFactories;

    @PostConstruct
    public void interceptContainerFactories() {
        this.containerFactories.forEach(factory -> factory.setMessageConverter(
                new TrailMetricsJmsMessageConverterWrapper(extract(factory),
                        TrailBehaviourMode.valueOf(this.incomingMode), TrailBehaviourMode.valueOf(this.outgoingMode))));
    }

    static MessageConverter extract(AbstractJmsListenerContainerFactory<?> factory) {
        try {
            Field messageConverterField = AbstractJmsListenerContainerFactory.class.getDeclaredField("messageConverter");
            messageConverterField.setAccessible(true);
            MessageConverter messageConverter = (MessageConverter) messageConverterField.get(factory);
            return messageConverter == null ? new SimpleMessageConverter() : messageConverter;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to extract message converter", e);
        }
    }
}