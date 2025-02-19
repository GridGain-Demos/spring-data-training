package com.gridgain.training.thinclient;

import com.gridgain.training.spring.model.City;
import com.gridgain.training.spring.model.CityKey;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class IgniteThinClient implements ApplicationRunner {

    @Autowired
    private IgniteClient client;

    private static final String QUERY = "SELECT city.name, MAX(city.population), country.name FROM country JOIN city ON city.countrycode = country.code GROUP BY city.name, country.name, city.population ORDER BY city.population DESC LIMIT ?";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("ServiceWithIgniteClient.run");
        System.out.println("Cache names existing in cluster: " + client.cacheNames());

        ClientCache<CityKey, City> cityCache = client.cache("City");
        FieldsQueryCursor<List<?>> cursor = cityCache.query(new SqlFieldsQuery(QUERY).setArgs(3));
        System.out.printf("%15s %12s %10s\n", "City", "Country", "Population");
        System.out.printf("%15s %12s %10s\n", "===============", "============", "==========");
        cursor.forEach((row) -> {
            System.out.printf("%15s %12s %10d\n", row.get(0), row.get(2), row.get(1));
        });
    }
}