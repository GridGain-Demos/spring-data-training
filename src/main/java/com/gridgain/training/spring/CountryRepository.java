package com.gridgain.training.spring;

import com.gridgain.training.spring.model.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for Country entities.
 *
 * Extending CrudRepository provides standard CRUD operations (save, findById, findAll, delete)
 * without writing any implementation code. Spring Data generates the implementation at runtime
 * based on the entity type (Country) and primary key type (String).
 *
 * The repository uses the JDBC connection configured in application.properties, with SQL dialect
 * support provided by IgniteDialectProvider. All generated queries execute against the Ignite
 * cluster's distributed SQL engine.
 */
@Repository
public interface CountryRepository extends CrudRepository<Country, String> {

    /**
     * Query derivation example: Spring Data parses this method name and generates SQL automatically.
     *
     * Method name breakdown:
     * - findBy: SELECT query prefix
     * - Population: WHERE clause on the population column
     * - GreaterThan: comparison operator (>)
     * - OrderByPopulationDesc: ORDER BY population DESC
     *
     * Generated SQL equivalent:
     * SELECT * FROM country WHERE population > ? ORDER BY population DESC
     *
     * This pattern eliminates boilerplate SQL for common query patterns. For more complex queries
     * involving JOINs or aggregations, use the @Query annotation instead.
     */
    List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
}
