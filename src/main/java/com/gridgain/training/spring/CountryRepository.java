package com.gridgain.training.spring;

import java.util.List;

import com.gridgain.training.spring.model.Country;
import org.apache.ignite.springdata.repository.IgniteRepository;
import org.apache.ignite.springdata.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@RepositoryConfig(cacheName = "Country")
@Repository
public interface CountryRepository extends IgniteRepository<Country, String> {

    List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);
}
