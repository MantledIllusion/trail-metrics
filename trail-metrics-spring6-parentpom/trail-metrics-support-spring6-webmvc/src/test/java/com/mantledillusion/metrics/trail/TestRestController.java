package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/parametered/{first}/subpath/{second}")
    public void parametered(@PathVariable("first") Long first, @PathVariable("second") Long second) {
        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
    }
}
