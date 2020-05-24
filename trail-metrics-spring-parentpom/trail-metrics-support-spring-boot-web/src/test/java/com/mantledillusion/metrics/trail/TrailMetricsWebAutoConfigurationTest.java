package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class TrailMetricsWebAutoConfigurationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void testInterceptorConfigured() {
        Assertions.assertTrue(this.restTemplate.getInterceptors().stream().
                anyMatch(interceptor -> interceptor instanceof TrailMetricsHttpClientInterceptor));
    }
}
