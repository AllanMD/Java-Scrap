package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private String name;
    private String address;
    private String extendedAddress;
    private String location;
    private String country;
    private String phone;
    private String score;
    private String price;
    //private String min_price; //  delete ?
    //private String max_price; // delete ?
    private List<String> meals;
    private List<String>cuisine;
    private String email;
    private String website;

    public Restaurant(String name, String address, String extendedAddress, String location, String country, String phone, String score, String price, List<String> meals, List<String> cuisine, String email, String website) {
        this.name = name;
        this.address = address;
        this.extendedAddress = extendedAddress;
        this.location = location;
        this.country = country;
        this.phone = phone;
        this.score = score;
        this.price = price;
        this.meals = meals;
        this.cuisine = cuisine;
        this.email = email;
        this.website = website;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExtendedAddress() {
        return extendedAddress;
    }

    public void setExtendedAddress(String extendedAddress) {
        this.extendedAddress = extendedAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<String> getMeals() {
        return meals;
    }

    public void setMeals(List<String> meals) {
        this.meals = meals;
    }

    public List<String> getCuisine() {
        return cuisine;
    }

    public void setCuisine(List<String> cuisine) {
        this.cuisine = cuisine;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", extendedAddress='" + extendedAddress + '\'' +
                ", location='" + location + '\'' +
                ", country='" + country + '\'' +
                ", phone='" + phone + '\'' +
                ", score='" + score + '\'' +
                ", price='" + price + '\'' +
                ", meals=" + meals +
                ", cuisine=" + cuisine +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                '}';
    }
}
