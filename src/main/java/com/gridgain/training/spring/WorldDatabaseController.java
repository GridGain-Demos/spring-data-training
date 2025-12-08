package com.gridgain.training.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing World Database queries as HTTP endpoints.
 *
 * This class demonstrates the final layer of the Spring Boot + Spring Data + Ignite stack:
 * HTTP requests are handled by Spring MVC, which delegates to Spring Data repositories,
 * which execute SQL against the Ignite cluster via JDBC.
 *
 * Request flow:
 * 1. HTTP GET /api/mostPopulated?limit=5
 * 2. Spring MVC routes to getMostPopulatedCities()
 * 3. Repository executes distributed SQL query on Ignite cluster
 * 4. Results are serialized to JSON and returned to client
 *
 * The @RestController annotation combines @Controller and @ResponseBody, indicating that
 * return values should be serialized directly to the response body (as JSON by default).
 */
@RestController
public class WorldDatabaseController {

    // Spring injects the repository proxy generated at runtime
    @Autowired
    CityRepository cityRepository;

    /**
     * Returns the most populated cities with their country names.
     *
     * Example: GET /api/mostPopulated?limit=5
     * Response: [{"cityName":"Mumbai (Bombay)","population":10500000,"countryName":"India"}, ...]
     *
     * The @GetMapping annotation maps HTTP GET requests to this method.
     * @RequestParam extracts query parameters, with defaultValue handling missing parameters.
     *
     * The return type (List of records) is automatically serialized to JSON by Spring's
     * Jackson integration. Record component names become JSON property names.
     */
    @GetMapping("/api/mostPopulated")
    public List<CityRepository.PopulousCity> getMostPopulatedCities(@RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {
        return cityRepository.findTopXMostPopulatedCities(limit);
    }

}
