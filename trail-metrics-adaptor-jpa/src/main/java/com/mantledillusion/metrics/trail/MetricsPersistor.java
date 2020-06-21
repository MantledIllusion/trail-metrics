package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Metric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Metric}s into a JPA database.
 */
public class MetricsPersistor implements MetricsConsumer {

    private final EntityManager em;

    private MetricsPersistor(EntityManager em) {
        this.em = em;
    }

    @Override
    public void consume(String consumerId, UUID trailId, Metric metric) {
        EntityTransaction tx = this.em.getTransaction();
        tx.begin();

        try {
            CriteriaBuilder builder = this.em.getCriteriaBuilder();
            CriteriaQuery<DbMetricsConsumerTrail> query = builder.createQuery(DbMetricsConsumerTrail.class);
            Root<DbMetricsConsumerTrail> root = query.from(DbMetricsConsumerTrail.class);

            query.select(root).where(builder.and(
                    builder.equal(root.get("consumerId"), consumerId),
                    builder.equal(root.get("trailId"), trailId)
            ));
            TypedQuery<DbMetricsConsumerTrail> trailTypedQuery = this.em.createQuery(query);

            DbMetricsConsumerTrail dbConsumerTrail;
            try {
                dbConsumerTrail = trailTypedQuery.getSingleResult();
            } catch (NoResultException e) {
                dbConsumerTrail = new DbMetricsConsumerTrail(trailId, consumerId);
            }

            DbMetric dbMetric = DbMetric.from(metric);
            dbMetric.setTrail(dbConsumerTrail);

            dbConsumerTrail.setMetrics(dbConsumerTrail.getMetrics() != null ? dbConsumerTrail.getMetrics() : new ArrayList<>());
            dbConsumerTrail.getMetrics().add(dbMetric);

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
