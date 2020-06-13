package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.config.AbstractJmsListenerContainerFactory;

import java.util.List;

@SpringBootTest
public class TrailMetricsJmsListenerContainerFactoryAutoConfigurationTest {

    @Autowired
    private List<AbstractJmsListenerContainerFactory<?>> containerFactories;

    @Test
    public void testWrapperConfigured() throws NoSuchFieldException {
        Assertions.assertTrue(this.containerFactories.stream().
                        map(TrailMetricsJmsListenerContainerFactoryAutoConfiguration::extract).
                allMatch(messageConverter -> messageConverter instanceof TrailMetricsJmsMessageConverterWrapper));
    }
}
