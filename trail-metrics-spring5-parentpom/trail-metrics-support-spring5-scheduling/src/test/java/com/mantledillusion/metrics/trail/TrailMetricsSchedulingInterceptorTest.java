package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestSchedulingConfig.class)
public class TrailMetricsSchedulingInterceptorTest {

    @Autowired
    private TestTask task;

    @Test
    public void test() {
        UUID execution0 = this.task.waitForTask(0);
        Assertions.assertNotNull(execution0);

        UUID execution1 = this.task.waitForTask(1);
        Assertions.assertNotNull(execution1);
        Assertions.assertNotEquals(execution0, execution1);
    }
}
