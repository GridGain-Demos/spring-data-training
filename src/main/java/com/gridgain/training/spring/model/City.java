package com.gridgain.training.spring.model;

import org.springframework.data.annotation.Id;

public record City (@Id CityKey id, String name, String district, Integer population) {
}
