package com.gridgain.training.spring.model;

import org.springframework.data.relational.core.mapping.Column;

public record CityKey (Integer id, @Column(value = "COUNTRYCODE") String countryCode) {
}
