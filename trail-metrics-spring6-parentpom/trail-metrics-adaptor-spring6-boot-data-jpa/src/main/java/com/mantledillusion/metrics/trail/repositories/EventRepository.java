package com.mantledillusion.metrics.trail.repositories;

import com.mantledillusion.metrics.trail.TrailMetricsHibernateJpaAutoConfiguration;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link JpaRepository} for {@link DbTrailEvent}s.
 */
@Repository
public interface EventRepository extends JpaRepository<DbTrailEvent, Long> {

    @Modifying
    @Transactional(TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER)
    @Query("DELETE FROM DbTrailEvent e WHERE e.timestamp < :oldest AND e.identifier LIKE :identifierLike")
    int cleanup(@Param("oldest") LocalDateTime oldest, @Param("identifierLike") String identifierLike);

    @Query("SELECT e FROM DbTrailEvent e WHERE e.identifier LIKE :identifierLike")
    List<DbTrailEvent> findByIdentifier(@Param("identifierLike") String identifierLike);

    @Query("SELECT COUNT(e) FROM DbTrailEvent e WHERE e.identifier LIKE :identifierLike")
    long countByIdentifier(@Param("identifierLike") String identifierLike);
}
