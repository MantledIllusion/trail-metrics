package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Metric}s into a JPA database.
 */
public class MetricsPersistor implements MetricsConsumer {

    private static final String QUERY_SELECT_CONSUMER_TRAIL_BY_CONSUMERID_AND_TRAILD =
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
            DbMetricsConsumerTrail dbConsumerTrail = (DbMetricsConsumerTrail) this.em.
                    createQuery(QUERY_SELECT_CONSUMER_TRAIL_BY_CONSUMERID_AND_TRAILD).
                    setParameter("trailId", trailId).
                    setParameter("consumerId", consumerId).
                    getSingleResult();

            DbMetric dbMetric = DbMetric.from(metric);
            if (dbConsumerTrail == null) {
                dbConsumerTrail = new DbMetricsConsumerTrail(trailId, consumerId);
            }
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
