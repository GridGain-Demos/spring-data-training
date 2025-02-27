package com.gridgain.training.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gridgain.training.spring.model.City;

import java.util.List;

@RestController
public class WorldDatabaseController {
    @Autowired
    CityRepository cityRepository;

    @GetMapping("/api/mostPopulated")
    public List<List<?>> getMostPopulatedCities(@RequestParam(value = "limit", required = true) Integer limit) {
        return cityRepository.findTopXMostPopulatedCities(limit);
    }

}