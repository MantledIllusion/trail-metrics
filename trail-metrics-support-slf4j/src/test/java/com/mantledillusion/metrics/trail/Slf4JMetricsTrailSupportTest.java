package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;
import java.util.UUID;

public class Slf4JMetricsTrailSupportTest {

    private static final Logger LOG = LoggerFactory.getLogger(Slf4JMetricsTrailSupportTest.class);

    @Test
    public void testLogMDC() throws IOException {
        UUID correlationId = UUID.randomUUID();

        try (PipedOutputStream os = new PipedOutputStream();
             PipedInputStream is = new PipedInputStream();
             PrintStream ps = new PrintStream(os);
             Scanner in = new Scanner(is)) {
            os.connect(is);
            System.setOut(ps);

            Assertions.assertFalse(logAndSnatch(in, correlationId));

            Slf4JMetricsTrailSupport.activatePublishToMdc();
            Assertions.assertTrue(logAndSnatch(in, correlationId));

            Slf4JMetricsTrailSupport.deactivatePublishToMdc();
            Assertions.assertFalse(logAndSnatch(in, correlationId));
        }
    }

    private boolean logAndSnatch(Scanner in, UUID correlationId) {
        MetricsTrailSupport.begin(correlationId);
        LOG.warn("Warning");
        String log = in.nextLine();
        MetricsTrailSupport.end();
        return log.contains(correlationId.toString());
    }
}
