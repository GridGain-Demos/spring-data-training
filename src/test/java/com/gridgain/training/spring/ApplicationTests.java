package com.gridgain.training.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

class ApplicationTests {
	@Autowired
	CountryRepository countryRepository;

	@Autowired
	CityRepository cityRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void countryRepositoryWorks() {
		System.out.println("count=" + countryRepository.findByPopulationGreaterThanOrderByPopulationDesc(100_000_000).size());
	}

	@Test
	void cityRepositoryWorks() {
		System.out.println("city = " + cityRepository.findById(34));

		System.out.println("top 5 = " + cityRepository.findTopXMostPopulatedCities(5));
	}

}
