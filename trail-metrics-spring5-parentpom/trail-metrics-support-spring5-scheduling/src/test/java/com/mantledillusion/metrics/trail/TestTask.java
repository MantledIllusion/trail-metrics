package com.mantledillusion.metrics.trail;

import org.junit.jupiter.api.Assertions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Component
public class TestTask {

    private final List<CompletableFuture<UUID>> futures = new ArrayList<>();

    @Scheduled(fixedDelay = 1000)
    public void execute() {
        synchronized (this.futures) {
            Optional<CompletableFuture<UUID>> future = this.futures.stream().filter(f -> !f.isDone()).findFirst();
            if (future.isPresent()) {
                future.get().complete(MetricsTrailSupport.has() ? MetricsTrailSupport.id() : null);
            } else {
                this.futures.add(CompletableFuture.completedFuture(MetricsTrailSupport.has() ? MetricsTrailSupport.id() : null));
            }
        }
    }

    public UUID waitForTask(int executionIdx) {
        CompletableFuture<UUID> future;
        synchronized (this.futures) {
            if (this.futures.size() < executionIdx) {
                future = this.futures.get(executionIdx);
            } else {
                future = new CompletableFuture<>();
                this.futures.add(future);
            }
        }

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return Assertions.fail(e);
        }
    }
}
