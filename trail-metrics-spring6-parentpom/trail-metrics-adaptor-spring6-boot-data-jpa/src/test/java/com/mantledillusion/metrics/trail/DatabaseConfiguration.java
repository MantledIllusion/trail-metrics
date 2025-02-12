package com.mantledillusion.metrics.trail;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public TransactionTemplate transactionTemplate(@Qualifier(TrailMetricsHibernateJpaAutoConfiguration.TRANSACTION_MANAGER_QUALIFIER) PlatformTransactionManager platformTransactionManager) {
        return new TransactionTemplate(platformTransactionManager);
    }
}
