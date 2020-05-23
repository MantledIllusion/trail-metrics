package com.mantledillusion.metrics.trail;

import com.mantledillusion.metrics.trail.api.jpa.DbMetric;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricAttribute;
import com.mantledillusion.metrics.trail.api.jpa.DbMetricsConsumerTrail;
import org.hibernate.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.EnumSet;

/**
 * Spring Boot auto configuration providing a {@link MetricsPersistor} instance.
 * <p>
 * The configuration is @{@link ConditionalOnBean} activated by the existence of an {@link EntityManager} bean. If
 * there are multiple {@link EntityManager} beans, the property {@value #PRTY_ENTITY_MANAGER_QUALIFIER} can be set to
 * the value of the @{@link Qualifier} of the {@link EntityManager} to use, by default the @{@link Qualifier}
 * {@value #ENTITY_MANAGER_DEFAULT_QUALIFIER} is used.
 * <p>
 * By default, the configuration will also use the {@link EntityManager}'s {@link DataSource} to migrate the database
 * to contain tables for the following entities:<br>
 * - {@link DbMetricsConsumerTrail}<br>
 * - {@link DbMetric}<br>
 * - {@link DbMetricAttribute}<br>
 * If not desired, the property {@value #PRTY_MIGRATE_DATABASE} can be set to <code>false</code>; creating those tables
 * will then be up to the developer.
 * <p>
 * The configuration will also configure these repositories for the entities:<br>
 *  * - {@link com.mantledillusion.metrics.trail.repositories.MetricsConsumerTrailRepository}<br>
 *  * - {@link com.mantledillusion.metrics.trail.repositories.MetricRepository}<br>
 *  * - {@link com.mantledillusion.metrics.trail.repositories.MetricAttributeRepository}<br>
 */
@Configuration
@ConditionalOnBean(EntityManager.class)
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableJpaRepositories("com.mantledillusion.metrics.trail.repositories")
public class TrailMetricsHibernateJpaAutoConfiguration {

    public static final String PRTY_ENTITY_MANAGER_QUALIFIER = "trailMetrics.jpa.entityManagerQualifier";
    public static final String PRTY_MIGRATE_DATABASE = "trailMetrics.jpa.doMigrate";
    private static final String ENTITY_MANAGER_DEFAULT_QUALIFIER = "entityManager";

    @Bean
    public MetricsPersistor metricsPersistor(@Value("${"+PRTY_ENTITY_MANAGER_QUALIFIER+":"+ENTITY_MANAGER_DEFAULT_QUALIFIER+"}") String entityManagerName,
                                             @Value("${"+PRTY_MIGRATE_DATABASE+":true}") boolean doMigrate,
                                             @Autowired ApplicationContext applicationContext) {
        EntityManager entityManager = applicationContext.getBean(entityManagerName, EntityManager.class);

        if (doMigrate) {
            Session session = (Session) entityManager.getDelegate();
            SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
            Dialect dialect = sessionFactory.getJdbcServices().getDialect();
            DataSource dataSource = ((EntityManagerFactoryInfo) entityManager.getEntityManagerFactory()).getDataSource();

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().
                    applySetting(Environment.DIALECT, dialect.getClass().getName()).
                    applySetting(Environment.DATASOURCE, dataSource).
                    build();
            Metadata metadata = new MetadataSources(serviceRegistry).
                    addAnnotatedClass(DbMetricsConsumerTrail.class).
                    addAnnotatedClass(DbMetric.class).
                    addAnnotatedClass(DbMetricAttribute.class).
                    buildMetadata();
            new SchemaExport().
                    execute(EnumSet.of(TargetType.DATABASE), SchemaExport.Action.CREATE, metadata);
        }

        return MetricsPersistor.from(entityManager);
    }
}
