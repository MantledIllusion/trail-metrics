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
        UUID trailId = UUID.randomUUID();

        try (PipedOutputStream os = new PipedOutputStream();
             PipedInputStream is = new PipedInputStream();
             PrintStream ps = new PrintStream(os);
             Scanner in = new Scanner(is)) {
            os.connect(is);
            System.setOut(ps);

            Assertions.assertFalse(logAndSnatch(in, trailId));

            Slf4JMetricsTrailSupport.activatePublishToMdc();
            Assertions.assertTrue(logAndSnatch(in, trailId));

            Slf4JMetricsTrailSupport.deactivatePublishToMdc();
            Assertions.assertFalse(logAndSnatch(in, trailId));
        }
    }

    private boolean logAndSnatch(Scanner in, UUID trailId) {
        MetricsTrailSupport.begin(trailId);
        LOG.warn("Warning");
        String log = in.nextLine();
        MetricsTrailSupport.end();
        return log.contains(trailId.toString());
    }
}
