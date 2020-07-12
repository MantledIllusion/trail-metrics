package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;

public class TrailMetricsHttpClientInterceptorTest {

    private static RestTemplate REST_TEMPLATE;
    private static MockRestServiceServer MOCK_REST_SERVER;

    @BeforeAll
    public static void beforeAll() {
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.setInterceptors(Collections.singletonList(new TrailMetricsHttpClientInterceptor()));
        MOCK_REST_SERVER = MockRestServiceServer.bindTo(REST_TEMPLATE).build();
    }

    @Test
    public void testSendCorrelationId() {
        UUID correlationId = UUID.randomUUID();

        MetricsTrailSupport.begin(correlationId);
        MOCK_REST_SERVER.
                expect(MockRestRequestMatchers.header(TrailMetricsHttpClientInterceptor.DEFAULT_HEADER_NAME, correlationId.toString())).
                andRespond(MockRestResponseCreators.withSuccess());
        REST_TEMPLATE.getForObject("/", Void.class);

        MOCK_REST_SERVER.reset();

        MetricsTrailSupport.end();
        MOCK_REST_SERVER.
                expect(MockRestRequestMatchers.headerDoesNotExist(TrailMetricsHttpClientInterceptor.DEFAULT_HEADER_NAME)).
                andRespond(MockRestResponseCreators.withSuccess());
        REST_TEMPLATE.getForObject("/", Void.class);
    }
}