package com.gridgain.training.spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Entity class mapped to the CITY table in the Ignite cluster.
 *
 * Spring Data JDBC uses convention-based mapping: the class name determines the target table,
 * and field names map to column names. When the Java field name differs from the SQL column name
 * (e.g., camelCase vs UPPERCASE), use {@code @Column} to specify the exact column name.
 *
 * The {@code @Id} annotation marks the primary key field. Spring Data uses this for:
 * - Determining insert vs update behavior (null ID = insert, non-null = update)
 * - Implementing findById() and deleteById() repository methods
 *
 * Note: This entity does not define a foreign key relationship to Country. While the database
 * schema links cities to countries via countryCode, Spring Data JDBC does not automatically
 * resolve these relationships. Use explicit JOIN queries when you need related data.
 */
public class City {
    @Id
    private Integer id;

    // Column annotation required: Java field is camelCase, SQL column is COUNTRYCODE
    @Column(value = "COUNTRYCODE")
    private String countryCode;

    private String name;

    private String district;

    private Integer population;

    public City() {
    }

    public City(Integer id, String countryCode, String name, String district, Integer population) {
        this.id = id;
        this.countryCode = countryCode;
        this.name = name;
        this.district = district;
        this.population = population;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    @Override public String toString() {
        return "City{" +
            "name='" + name + '\'' +
            ", district='" + district + '\'' +
            ", population=" + population +
            '}';
    }
}
