package com.mantledillusion.metrics.trail;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "TestApi", url = "127.0.0.1:8080")
public interface TestApi {

    @GetMapping("/")
    String get();
}
