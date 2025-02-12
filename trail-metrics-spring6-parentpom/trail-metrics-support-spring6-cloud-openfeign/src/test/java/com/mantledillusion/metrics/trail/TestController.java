package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {

    @GetMapping("/")
    public String get(RequestEntity<Void> request) {
        return request.getHeaders().containsKey(TrailMetricsFeignRequestInterceptor.DEFAULT_HEADER_NAME) ?
                request.getHeaders().getFirst(TrailMetricsFeignRequestInterceptor.DEFAULT_HEADER_NAME) : null;
    }
}