package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Configuration
@AutoConfigureAfter(JmsAutoConfiguration.class)
public class TrailMetricsJmsTemplateAutoConfiguration {

    @Value("${"+TrailMetricsJmsMessageConverterWrapper.PRTY_INCOMING_MODE+":"+TrailMetricsJmsMessageConverterWrapper.DEFAULT_INCOMING_MODE+"}")
    private String incomingMode;
    @Value("${"+TrailMetricsJmsMessageConverterWrapper.PRTY_OUTGOING_MODE+":"+TrailMetricsJmsMessageConverterWrapper.DEFAULT_OUTGOING_MODE+"}")
    private String outgoingMode;
    @Value("${"+TrailMetricsJmsMessageConverterWrapper.PRTY_DISPATCH_RECEIVE+":"+TrailMetricsJmsMessageConverterWrapper.DEFAULT_DISPATCH_RECEIVE+"}")
    private boolean dispatchReceiveMessage;

    @Autowired(required = false)
    private List<JmsTemplate> jmsTemplates = Collections.emptyList();

    @PostConstruct
    public void interceptJmsTemplates() {
        this.jmsTemplates.forEach(jmsTemplate -> jmsTemplate.setMessageConverter(
                new TrailMetricsJmsMessageConverterWrapper(jmsTemplate.getMessageConverter() == null ? new SimpleMessageConverter() : jmsTemplate.getMessageConverter(),
                        TrailBehaviourMode.valueOf(this.incomingMode), TrailBehaviourMode.valueOf(this.outgoingMode), this.dispatchReceiveMessage)));
    }
}
