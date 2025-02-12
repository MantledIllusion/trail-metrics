package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.Event;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailEvent;
import com.mantledillusion.metrics.trail.api.jpa.DbTrailConsumer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.UUID;

/**
 * {@link MetricsConsumer} implementation that is able to persist consumed {@link Event}s into a JPA database.
 */
public class MetricsPersistor implements MetricsConsumer {

    private final EntityManager em;

    private MetricsPersistor(EntityManager em) {
        this.em = em;
    }

    @Override
    public void consume(String consumerId, UUID correlationId, Event event) {
        EntityTransaction tx = this.em.getTransaction();
        tx.begin();

        try {
            CriteriaBuilder builder = this.em.getCriteriaBuilder();
            CriteriaQuery<DbTrailConsumer> query = builder.createQuery(DbTrailConsumer.class);
            Root<DbTrailConsumer> root = query.from(DbTrailConsumer.class);

            query.select(root).where(builder.and(
                    builder.equal(root.get("consumerId"), consumerId),
                    builder.equal(root.get("correlationId"), correlationId)
            ));
            TypedQuery<DbTrailConsumer> trailTypedQuery = this.em.createQuery(query);

            DbTrailConsumer dbConsumerTrail;
            try {
                dbConsumerTrail = trailTypedQuery.getSingleResult();
            } catch (NoResultException e) {
                dbConsumerTrail = new DbTrailConsumer(correlationId, consumerId);
            }

            DbTrailEvent dbTrailEvent = DbTrailEvent.from(event);
            dbTrailEvent.setTrail(dbConsumerTrail);

            dbConsumerTrail.setEvents(dbConsumerTrail.getEvents() != null ? dbConsumerTrail.getEvents() : new ArrayList<>());
            dbConsumerTrail.getEvents().add(dbTrailEvent);

            this.em.persist(dbTrailEvent);
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
