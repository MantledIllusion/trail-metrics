package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class TestJmsReceiver {

    static final String QUEUE = "queue";

    private final Map<String, CompletableFuture<UUID>> futures = new HashMap<>();

    @JmsListener(destination = QUEUE)
    public void receive(String msg) {
        recreate(msg).complete(MetricsTrailSupport.has() ? MetricsTrailSupport.get() : null);
        if (MetricsTrailSupport.has()) {
            MetricsTrailSupport.end();
        }
    }

    public UUID await(String msg) {
        try {
            return recreate(msg).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return Assertions.fail(e);
        } catch (TimeoutException e) {
            return Assertions.fail("Did not receive a message containing '" + msg + "'");
        }
    }

    private CompletableFuture<UUID> recreate(String msg) {
        synchronized (this.futures) {
            return this.futures.computeIfAbsent(msg, m -> new CompletableFuture<>());
        }
    }
}
