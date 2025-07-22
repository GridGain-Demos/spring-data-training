package com.gridgain.training.spring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;

public class Country {
    @Id
    private String code;

    private String name;

    private String continent;

    private String region;

    private Integer population;

    @Column(value = "SURFACEAREA")
    private BigDecimal surfaceArea;

    @Column("INDEPYEAR")
    private Short indepYear;

    @Column(value = "LIFEEXPECTANCY")
    private BigDecimal lifeExpectancy;

    private BigDecimal gnp;

    @Column(value = "GNPOLD")
    private BigDecimal gnpOld;

    @Column("LOCALNAME")
    private String localName;

    @Column("GOVERNMENTFORM")
    private String governmentForm;

    @Column("HEADOFSTATE")
    private String headOfState;

    private Integer capital;

    private String code2;

    public Country() {
    }

    public Country(String code, String name, String continent, String region, BigDecimal surfaceArea, Short indepYear, Integer population, BigDecimal lifeExpectancy, BigDecimal gnp, BigDecimal gnpOld, String localName, String governmentForm, String headOfState, Integer capital, String code2) {
        this.code = code;
        this.name = name;
        this.continent = continent;
        this.region = region;
        this.population = population;
        this.surfaceArea = surfaceArea;
        this.indepYear = indepYear;
        this.lifeExpectancy = lifeExpectancy;
        this.gnp = gnp;
        this.gnpOld = gnpOld;
        this.localName = localName;
        this.governmentForm = governmentForm;
        this.headOfState = headOfState;
        this.capital = capital;
        this.code2 = code2;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public BigDecimal getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(BigDecimal surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public Short getIndepYear() {
        return indepYear;
    }

    public void setIndepYear(Short indepYear) {
        this.indepYear = indepYear;
    }

    public BigDecimal getLifeExpectancy() {
        return lifeExpectancy;
    }

    public void setLifeExpectancy(BigDecimal lifeExpectancy) {
        this.lifeExpectancy = lifeExpectancy;
    }

    public BigDecimal getGnp() {
        return gnp;
    }

    public void setGnp(BigDecimal gnp) {
        this.gnp = gnp;
    }

    public BigDecimal getGnpOld() {
        return gnpOld;
    }

    public void setGnpOld(BigDecimal gnpOld) {
        this.gnpOld = gnpOld;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getGovernmentForm() {
        return governmentForm;
    }

    public void setGovernmentForm(String governmentForm) {
        this.governmentForm = governmentForm;
    }

    public String getHeadOfState() {
        return headOfState;
    }

    public void setHeadOfState(String headOfState) {
        this.headOfState = headOfState;
    }

    public Integer getCapital() {
        return capital;
    }

    public void setCapital(Integer capital) {
        this.capital = capital;
    }

    public String getCode2() {
        return code2;
    }

    public void setCode2(String code2) {
        this.code2 = code2;
    }

    @Override public String toString() {
        return "Country{" +
            "name='" + name + '\'' +
            ", continent='" + continent + '\'' +
            ", region='" + region + '\'' +
            ", population=" + population +
            ", surfaceArea=" + surfaceArea +
            ", indepYear=" + indepYear +
            ", lifeExpectancy=" + lifeExpectancy +
            ", gnp=" + gnp +
            ", gnpOld=" + gnpOld +
            ", localName='" + localName + '\'' +
            ", governmentForm='" + governmentForm + '\'' +
            ", headOfState='" + headOfState + '\'' +
            ", capital=" + capital +
            ", code2='" + code2 + '\'' +
            '}';
    }
}
