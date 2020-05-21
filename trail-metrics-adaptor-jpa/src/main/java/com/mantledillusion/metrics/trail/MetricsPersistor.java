package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Metric}s into a JPA database.
 */
public class MetricsPersistor implements MetricsConsumer {

    private static final String QUERY_SELECT_CONSUMER_TRAIL_BY_CONSUMERID_AND_TRAILID =
            "SELECT ct " +
                    "FROM DbMetricsConsumerTrail ct " +
                    "WHERE ct.consumerId = :consumerId " +
                    "AND ct.trailId = :trailId";

    private final EntityManager em;

    private MetricsPersistor(EntityManager em) {
        this.em = em;
    }

    @Override
    public void consume(String consumerId, UUID trailId, Metric metric) throws Exception {
        EntityTransaction tx = this.em.getTransaction();
        tx.begin();

        try {
            DbMetricsConsumerTrail dbConsumerTrail;
            try {
                dbConsumerTrail = this.em.
                        createQuery(QUERY_SELECT_CONSUMER_TRAIL_BY_CONSUMERID_AND_TRAILID, DbMetricsConsumerTrail.class).
                        setParameter("trailId", trailId).
                        setParameter("consumerId", consumerId).
                        getSingleResult();
            } catch (NoResultException e) {
                dbConsumerTrail = new DbMetricsConsumerTrail(trailId, consumerId);
            }

            DbMetric dbMetric = DbMetric.from(metric);
            dbMetric.setTrail(dbConsumerTrail);

            this.em.persist(dbMetric);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    /**
     * Factory method for {@link MetricsPersistor}s.
     * <p>
     * Will use the given {@link EntityManager} to persist consumed metrics.
     *
     * @param em The {@link EntityManager} to persist with; might <b>not</b> be null.
     * @return A new {@link MetricsPersistor} instance, never null
     */
    public static MetricsPersistor from(EntityManager em) {
        if (em == null) {
            throw new IllegalArgumentException("Cannot create a metrics persistor from a null entity manager.");
        }
        return new MetricsPersistor(em);
    }
}
