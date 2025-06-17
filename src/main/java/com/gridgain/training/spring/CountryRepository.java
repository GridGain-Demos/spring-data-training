package com.gridgain.training.spring;

import com.gridgain.training.spring.model.Country;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends CrudRepository<Country,String> {

     List<Country> findByPopulationGreaterThanOrderByPopulationDesc(int population);

}
