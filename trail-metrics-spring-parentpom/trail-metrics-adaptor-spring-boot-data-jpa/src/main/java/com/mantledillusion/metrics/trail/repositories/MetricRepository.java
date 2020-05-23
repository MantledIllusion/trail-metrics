package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link JpaRepository} for {@link DbMetric}s.
 */
@Repository
public interface MetricRepository extends JpaRepository<DbMetric, Long> {

}
