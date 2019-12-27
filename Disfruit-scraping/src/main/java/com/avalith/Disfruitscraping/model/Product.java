package com.avalith.Disfruitscraping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    private String name;
    private float price;
    private List<String> categories;

}
