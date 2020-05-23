package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link JpaRepository} for {@link DbMetricsConsumerTrail}s.
 */
@Repository
public interface MetricsConsumerTrailRepository extends JpaRepository<DbMetricsConsumerTrail, Long> {

}
