package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.TrailMetricsHibernateJpaAutoConfiguration;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link JpaRepository} for {@link DbMetricsConsumerTrail}s.
 */
@Repository
public interface MetricsConsumerTrailRepository extends JpaRepository<DbMetricsConsumerTrail, Long> {

    @Modifying
    @Transactional(TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER)
    @Query("DELETE FROM DbMetricsConsumerTrail t WHERE size(t.metrics) = 0")
    int cleanup();
}
