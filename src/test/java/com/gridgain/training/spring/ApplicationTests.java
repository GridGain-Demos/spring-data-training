package com.gridgain.training.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests demonstrating Spring Data repository operations against a running Ignite cluster.
 *
 * These tests require:
 * 1. Docker Compose cluster running (docker compose up -d)
 * 2. Cluster initialized (cluster init command)
 * 3. World database loaded (sql --file=/opt/ignite/downloads/world.sql)
 *
 * The @SpringBootTest annotation loads the full application context, including:
 * - Auto-configured Ignite thin client connection
 * - Spring Data JDBC repositories with Ignite dialect
 * - JDBC DataSource connected to the cluster
 */
@SpringBootTest
class ApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CityRepository cityRepository;

    /**
     * Verifies Spring context loads successfully with all Ignite integration components.
     * A passing test confirms: thin client connects, JDBC driver loads, repositories initialize.
     */
    @Test
    void contextLoads() {
    }

    /**
     * Tests query derivation: Spring Data generates SQL from the method name.
     * findByPopulationGreaterThanOrderByPopulationDesc(100_000_000) should return
     * countries with population over 100 million, ordered by population descending.
     */
    @Test
    void countryRepositoryWorks() {
        var results = countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000);
        log.info("count={}", results.size());
        assertFalse(results.isEmpty());
    }

    /**
     * Tests both inherited CrudRepository methods and custom @Query operations.
     *
     * findById(34) exercises the inherited method with ID-based lookup.
     * findTopXMostPopulatedCities(5) exercises the custom JOIN query.
     */
    @Test
    void cityRepositoryWorks() {
        // Test inherited CrudRepository.findById()
        var city = cityRepository.findById(34);
        log.info("city={}", city.orElse(null));
        assertTrue(city.isPresent());
        assertEquals("Tirana", city.get().getName());

        // Test custom @Query with JOIN, GROUP BY, ORDER BY, LIMIT
        var populatedCities = cityRepository.findTopXMostPopulatedCities(5);
        log.info("top 5 cities={}", populatedCities);
        assertEquals(5, populatedCities.size());
        assertEquals("Mumbai (Bombay)", populatedCities.get(0).cityName());
    }
}
