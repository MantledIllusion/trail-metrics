package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestRestController {

    public static final UUID TRAIL_ID = UUID.randomUUID();

    @GetMapping("/supported")
    public void supported() {
        Assertions.assertTrue(MetricsTrailSupport.has());
    }

    @GetMapping("/unsupported")
    public void unsupported() {
        Assertions.assertFalse(MetricsTrailSupport.has());
    }
}
