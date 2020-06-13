package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import java.util.List;

@SpringBootTest
public class TrailMetricsJmsTemplateAutoConfigurationTest {

    @Autowired
    private List<JmsTemplate> jmsTemplates;

    @Test
    public void testWrapperConfigured() {
        Assertions.assertTrue(this.jmsTemplates.stream().
                allMatch(jmsTemplate -> jmsTemplate.getMessageConverter() instanceof TrailMetricsJmsMessageConverterWrapper));
    }
}
