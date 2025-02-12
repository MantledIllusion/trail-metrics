package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.UUID;

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TrailMetricsFeignRequestInterceptorTest {

    @Autowired
    private TestApi apiClient;

    @Test
    public void testSendCorrelationId() {
        UUID correlationId = UUID.randomUUID();

        MetricsTrailSupport.begin(correlationId);
        Assertions.assertEquals(correlationId.toString(), this.apiClient.get());

        MetricsTrailSupport.end();
        Assertions.assertNull(this.apiClient.get());
    }
}