package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.api.jpa.DbMetricAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link JpaRepository} for {@link DbMetricAttribute}s.
 */
@Repository
public interface MetricAttributeRepository extends JpaRepository<DbMetricAttribute, Long> {

}
