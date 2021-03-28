package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.TrailMetricsHibernateJpaAutoConfiguration;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailConsumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link JpaRepository} for {@link DbTrailConsumer}s.
 */
@Repository
public interface ConsumerRepository extends JpaRepository<DbTrailConsumer, Long> {

    @Modifying
    @Transactional(TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER)
    @Query("DELETE FROM DbTrailConsumer c WHERE size(c.events) = 0")
    int cleanup();
}
