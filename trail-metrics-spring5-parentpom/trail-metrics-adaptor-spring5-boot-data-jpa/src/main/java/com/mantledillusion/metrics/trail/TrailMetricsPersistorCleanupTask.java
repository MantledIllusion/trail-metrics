package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.repositories.EventRepository;
import com.mantledillusion.metrics.trail.repositories.ConsumerRepository;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class TrailMetricsPersistorCleanupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrailMetricsPersistorCleanupTask.class);

    @Value("${trailMetrics.jpa.cleanup.identifiersLike:%}")
    private List<String> identifiersLike;
    @Value("${trailMetrics.jpa.cleanup.age:7d}")
    private Duration age;
    @Value("${"+TrailMetricsHibernateJpaAutoConfiguration.PRTY_METRICS_CLEANUP_LOCKING+":false}")
    private boolean lock;
    @Value("${trailMetrics.jpa.cleanup.lock.atLeastFor:5m}")
    private Duration lockAtLeastFor;
    @Value("${trailMetrics.jpa.cleanup.lock.atMostFor:1h}")
    private Duration lockAtMostFor;

    @Autowired
    @Qualifier(TrailMetricsHibernateJpaAutoConfiguration.ENTITY_MANAGER_QUALIFIER)
    private EntityManager entityManager;
    @Autowired(required = false)
    @Qualifier(TrailMetricsHibernateJpaAutoConfiguration.CLEANUP_TASK_EXECUTOR_QUALIFIER)
    private LockingTaskExecutor lockingTaskExecutor;
    @Autowired
    private ConsumerRepository consumerRepository;
    @Autowired
    private EventRepository eventRepository;


    @Scheduled(cron = "${trailMetrics.jpa.cleanup.cron:0 0 */1 ? * *}")
    public void schedule() {
        if (this.lock) {
            LockConfiguration lockConfiguration = new LockConfiguration(Instant.now(),
                    TrailMetricsPersistorCleanupTask.class.getSimpleName(), this.lockAtMostFor, this.lockAtLeastFor);

            this.lockingTaskExecutor.executeWithLock((Runnable) this::cleanup, lockConfiguration);
        } else {
            cleanup();
        }
    }

    private void cleanup() {
        int metricCount = this.identifiersLike.stream().map(identifierLike -> this.eventRepository.
                cleanup(LocalDateTime.now().minus(this.age), identifierLike)).
                reduce(0, Integer::sum);

        if (metricCount > 0) {
            int trailCount = this.consumerRepository.cleanup();
            LOGGER.info(String.format("Cleaned up %s metrics, cleaned up %s now metric-less trails.", metricCount, trailCount));
        }
    }
}
