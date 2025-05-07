package model;

import java.util.Objects;

public class Location {
    private Long id;
    private String country;
    private String city;
    private String street;
    private String number;
    private String postalCode;

    public Location() {
    }

    public Location(Long id, String country, String city, String street, String number, String postalCode) {
        this.id = id;
        this.country = country;
        this.city = city;
        this.street = street;
        this.number = number;
        this.postalCode = postalCode;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    // Get full address as a formatted string
    public String getFullAddress() {
        return street + " " + number + ", " + city + ", " + country + " " + postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return city + ", " + country;
    }
}