package com.gridgain.training.spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

public class City {
    @Id
    private Integer id;

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
