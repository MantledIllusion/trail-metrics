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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Spring Boot auto configuration providing a {@link MetricsPersistor} instance.
 * <p>
 * The configuration requires a {@link DataSource} to create an own {@link EntityManagerFactory} and
 * {@link EntityManager} from; there are multiple {@link DataSource} beans, the property
 * {@value #PRTY_DATA_SOURCE_QUALIFIER} can be set to the value of the @{@link Qualifier} of the {@link DataSource} to
 * use, by default the @{@link Qualifier} {@value #DATA_SOURCE_DEFAULT_QUALIFIER} is used.
 * <p>
 * By default, the configuration will also use the {@link DataSource} to migrate the database to contain tables for the
 * following entities:<br>
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
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.mantledillusion.metrics.trail.repositories",
        entityManagerFactoryRef = TrailMetricsHibernateJpaAutoConfiguration.ENTITY_MANAGER_FACTORY_QUALIFIER,
        transactionManagerRef = TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER)
public class TrailMetricsHibernateJpaAutoConfiguration {

    public static final String ENTITY_MANAGER_FACTORY_QUALIFIER = "MetricsEntityManagerFactory";
    public static final String ENTITY_MANAGER_QUALIFIER = "MetricsEntityManager";
    public static final String TRANSACTION_MANAGER_QUALIFIER = "MetricsTransactionManager";
    public static final String CLEANUP_TASK_QUALIFIER = "MetricsCleanupTask";
    public static final String PERSISTENCE_UNIT = "MetricsPersistenceUnit";

    public static final String PRTY_DATA_SOURCE_QUALIFIER = "trailMetrics.jpa.dataSourceQualifier";
    public static final String PRTY_MIGRATE_DATABASE = "trailMetrics.jpa.doMigrate";
    public static final String PRTY_METRICS_CLEANUP = "trailMetrics.jpa.doCleanup";
    public static final String DATA_SOURCE_DEFAULT_QUALIFIER = "dataSource";

    @Value("${"+PRTY_MIGRATE_DATABASE+":true}")
    private boolean doMigrate;
    @Value("${"+ PRTY_DATA_SOURCE_QUALIFIER +":"+ DATA_SOURCE_DEFAULT_QUALIFIER +"}")
    private String dataSourceQualifier;

    @Bean
    public MetricsPersistor metricsPersistor(@Qualifier(ENTITY_MANAGER_QUALIFIER) EntityManager entityManager,
                                             ApplicationContext applicationContext) {
        if (this.doMigrate) {
            Session session = (Session) entityManager.getDelegate();
            SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
            Dialect dialect = sessionFactory.getJdbcServices().getDialect();
            DataSource dataSource = applicationContext.getBean(this.dataSourceQualifier, DataSource.class);

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

    @Bean(ENTITY_MANAGER_FACTORY_QUALIFIER)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(ApplicationContext applicationContext) {
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(), Collections.emptyMap(), null).
                dataSource(applicationContext.getBean(this.dataSourceQualifier, DataSource.class)).
                packages("com.mantledillusion.metrics.trail.api.jpa").
                persistenceUnit(PERSISTENCE_UNIT).
                build();
    }

    @Bean(ENTITY_MANAGER_QUALIFIER)
    public EntityManager entityManager(@Qualifier(ENTITY_MANAGER_FACTORY_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean(TRANSACTION_MANAGER_QUALIFIER)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER_FACTORY_QUALIFIER) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(CLEANUP_TASK_QUALIFIER)
    @ConditionalOnProperty(name = PRTY_METRICS_CLEANUP, havingValue = "true")
    public TrailMetricsPersistorCleanupTask trailMetricsPersistorCleanupTask() {
        return new TrailMetricsPersistorCleanupTask();
    }
}
