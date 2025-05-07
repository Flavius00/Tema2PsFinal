package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chain {
    private Long id;
    private String name;
    private String description;
    private String headquarters;
    private String website;
    private String logo;

    // List of hotels in the chain
    private List<Hotel> hotels;

    public Chain() {
        this.hotels = new ArrayList<>();
    }

    public Chain(Long id, String name, String description, String headquarters, String website, String logo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.headquarters = headquarters;
        this.website = website;
        this.logo = logo;
        this.hotels = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadquarters() {
        return headquarters;
    }

    public void setHeadquarters(String headquarters) {
        this.headquarters = headquarters;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<Hotel> getHotels() {
        return hotels;
    }

    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;
    }

    public void addHotel(Hotel hotel) {
        this.hotels.add(hotel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chain chain = (Chain) o;
        return Objects.equals(id, chain.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}