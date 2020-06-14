package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TrailMetricsSchedulingAutoConfigurationTest {

    @Autowired
    private TestTask task;

    @Test
    public void test() {
        Assertions.assertNotNull(this.task.waitForTask());
    }
}
