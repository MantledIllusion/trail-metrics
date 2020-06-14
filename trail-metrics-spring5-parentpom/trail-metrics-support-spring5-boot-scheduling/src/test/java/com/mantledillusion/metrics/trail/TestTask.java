package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class TestTask {

    private final CompletableFuture<UUID> future = new CompletableFuture<>();

    @Scheduled(fixedDelay = Long.MAX_VALUE)
    public void execute() {
        this.future.complete(MetricsTrailSupport.has() ? MetricsTrailSupport.id() : null);
    }

    public UUID waitForTask() {
        try {
            return this.future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Assertions.fail(e);
        }
    }
}
