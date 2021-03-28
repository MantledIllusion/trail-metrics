package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.api.jpa.DbTrailMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link JpaRepository} for {@link DbTrailMeasurement}s.
 */
@Repository
public interface MeasurementRepository extends JpaRepository<DbTrailMeasurement, Long> {

}
