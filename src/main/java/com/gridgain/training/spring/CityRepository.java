package com.gridgain.training.spring;

import com.gridgain.training.spring.model.City;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for City entities demonstrating custom SQL queries.
 *
 * While CrudRepository provides basic operations and query derivation handles simple WHERE clauses,
 * complex queries involving JOINs, aggregations, or specific SQL syntax require the @Query annotation.
 *
 * This repository shows both patterns:
 * - Inherited methods (findById, findAll, save, delete) from CrudRepository
 * - Custom SQL via @Query for multi-table operations
 */
@Repository
public interface CityRepository extends CrudRepository<City, Integer> {

    /**
     * DTO record for query results that span multiple tables.
     *
     * When a query returns columns from different tables or computed values, define a record
     * (or class) to hold the results. Spring Data maps query result columns to record components
     * by matching column aliases to component names (city_name -> cityName via convention).
     *
     * Records provide immutability and automatic equals/hashCode/toString implementations,
     * making them ideal for DTOs in Spring Data projections.
     */
    record PopulousCity(String cityName, Integer population, String countryName) {}

    /**
     * Custom SQL query demonstrating JOIN operations with Ignite's distributed SQL engine.
     *
     * Query features:
     * - JOIN between city and country tables on the foreign key relationship
     * - Aggregation (MAX) with GROUP BY
     * - Result ordering and limiting
     * - Named parameter binding (:limit)
     *
     * Column aliasing (city.name AS city_name) ensures results map correctly to the PopulousCity
     * record components. The underscore-to-camelCase conversion is handled automatically.
     *
     * Ignite executes this query across the distributed cluster, with each node processing
     * its local data partition before aggregating results.
     */
    @Query("SELECT city.name as city_name, MAX(city.population) as population, country.name as country_name FROM country " +
            "JOIN city ON city.countrycode = country.code " +
            "GROUP BY city.name, country.name, city.population " +
            "ORDER BY city.population DESC LIMIT :limit")
    public List<PopulousCity> findTopXMostPopulatedCities(int limit);
}
