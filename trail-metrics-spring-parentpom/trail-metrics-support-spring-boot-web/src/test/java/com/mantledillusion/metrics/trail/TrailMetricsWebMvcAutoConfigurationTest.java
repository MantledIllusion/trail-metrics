package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.lang.reflect.Field;
import java.util.List;

@SpringBootTest
public class TrailMetricsWebMvcAutoConfigurationTest {

    @Autowired
    private WebMvcConfigurationSupport support;

    @Test
    public void testInterceptorConfigured() throws NoSuchFieldException, IllegalAccessException {
        Field interceptorField = WebMvcConfigurationSupport.class.getDeclaredField("interceptors");
        interceptorField.setAccessible(true);
        List<Object> interceptors = (List<Object>) interceptorField.get(this.support);
        Assertions.assertTrue(interceptors.stream().anyMatch(interceptor -> interceptor instanceof TrailMetricsHttpServerInterceptor));
    }
}
