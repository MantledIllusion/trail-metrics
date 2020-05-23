package com.mantledillusion.metrics.trail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class DatabaseConfiguration {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public EntityManager entityManager() {
        return this.em;
    }
}
