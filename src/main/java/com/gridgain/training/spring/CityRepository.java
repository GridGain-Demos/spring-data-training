package com.gridgain.training.spring;

import java.util.List;
import javax.cache.Cache;

import com.gridgain.training.spring.model.City;
import com.gridgain.training.spring.model.CityKey;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.Query;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@RepositoryConfig(cacheName = "City")
@Repository
public interface CityRepository extends IgniteRepository<City, CityKey> {

    // Cache.Entry exposes the full key (id + countryCode affinity field), not just the value.
    Cache.Entry<CityKey, City> findById(int id);

    @Query("SELECT city.name, MAX(city.population), country.name FROM country " +
           "JOIN city ON city.countrycode = country.code " +
           "GROUP BY city.name, country.name, city.population " +
           "ORDER BY city.population DESC LIMIT ?")
    List<List<?>> findTopXMostPopulatedCities(int limit);
}
