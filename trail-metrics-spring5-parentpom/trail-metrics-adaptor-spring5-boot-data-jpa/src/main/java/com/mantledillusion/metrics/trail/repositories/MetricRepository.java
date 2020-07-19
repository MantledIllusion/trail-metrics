package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.TrailMetricsHibernateJpaAutoConfiguration;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link JpaRepository} for {@link DbMetric}s.
 */
@Repository
public interface MetricRepository extends JpaRepository<DbMetric, Long> {

    @Modifying
    @Transactional(TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER)
    @Query("DELETE FROM DbMetric m WHERE m.timestamp < :oldest AND m.identifier LIKE :identifierLike")
    int cleanup(@Param("oldest") LocalDateTime oldest, @Param("identifierLike") String identifierLike);

    @Query("SELECT m FROM DbMetric m WHERE m.identifier LIKE :identifierLike")
    List<DbMetric> findByIdentifier(@Param("identifierLike") String identifierLike);

    @Query("SELECT COUNT(m) FROM DbMetric m WHERE m.identifier LIKE :identifierLike")
    long countByIdentifier(@Param("identifierLike") String identifierLike);
}
