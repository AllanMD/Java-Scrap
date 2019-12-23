package com.avalith.Productsscraping.model;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;
    private String name;
    private String category;
    private String metrics;
    private float priceInPesos;
    private float priceInUsd;
    private float priceInEur;
    private float yesterdayMaxPrice;
    private float variation;

    public Product(String name, String category, String metrics, float priceInPesos, float priceInUsd, float priceInEur, float yesterdayMaxPrice, float variation) {
        this.name = name;
        this.category = category;
        this.metrics = metrics;
        this.priceInPesos = priceInPesos;
        this.priceInUsd = priceInUsd;
        this.priceInEur = priceInEur;
        this.yesterdayMaxPrice = yesterdayMaxPrice;
        this.variation = variation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMetrics() {
        return metrics;
    }

    public void setMetrics(String metrics) {
        this.metrics = metrics;
    }

    public float getPriceInPesos() {
        return priceInPesos;
    }

    public void setPriceInPesos(float priceInPesos) {
        this.priceInPesos = priceInPesos;
    }

    public float getPriceInUsd() {
        return priceInUsd;
    }

    public void setPriceInUsd(float priceInUsd) {
        this.priceInUsd = priceInUsd;
    }

    public float getPriceInEur() {
        return priceInEur;
    }

    public void setPriceInEur(float priceInEur) {
        this.priceInEur = priceInEur;
    }

    public float getYesterdayMaxPrice() {
        return yesterdayMaxPrice;
    }

    public void setYesterdayMaxPrice(float yesterdayMaxPrice) {
        this.yesterdayMaxPrice = yesterdayMaxPrice;
    }

    public float getVariation() {
        return variation;
    }

    public void setVariation(float variation) {
        this.variation = variation;
    }
}

