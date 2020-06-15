package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Spring Boot auto configuration registering a {@link TrailMetricsHttpClientInterceptor} to every {@link RestTemplate}
 * available in the application's context.
 */
@Configuration
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
public class TrailMetricsWebAutoConfiguration {

    @Value("${"+TrailMetricsHttpClientInterceptor.PRTY_HEADER_NAME+ ":"+TrailMetricsHttpClientInterceptor.DEFAULT_HEADER_NAME+"}")
    private String headerName;
    @Autowired
    private List<RestTemplate> restTemplates;

    @PostConstruct
    public void interceptRestTemplates() {
        TrailMetricsHttpClientInterceptor clientInterceptor = new TrailMetricsHttpClientInterceptor(headerName);
        this.restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(clientInterceptor));
    }
}
