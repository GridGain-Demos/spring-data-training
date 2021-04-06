package com.gridgain.training.spring.model;

import java.io.Serializable;
import java.util.Objects;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;

public class CityKey implements Serializable {
    private int ID;

    @AffinityKeyMapped
    private String COUNTRYCODE;

    public CityKey(int id, String countryCode) {
        this.ID = id;
        this.COUNTRYCODE = countryCode;
    }

    public int getId() {
        return ID;
    }

    public String getCountryCode() {
        return COUNTRYCODE;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CityKey key = (CityKey)o;
        return ID == key.ID &&
            COUNTRYCODE.equals(key.COUNTRYCODE);
    }

    @Override public int hashCode() {
        return Objects.hash(ID, COUNTRYCODE);
    }

    @Override public String toString() {
        return "CityKey{" +
            "ID=" + ID +
            ", COUNTRYCODE='" + COUNTRYCODE + '\'' +
            '}';
    }
}
