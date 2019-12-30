package com.avalith.Disfruitscraping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    private String name; // de esta forma no se guardan duplicados de los productos, se hace update en caso de ya existir en la bd
    private float minPrice;
    private float maxPrice;
    private List<String> categories;
    //SKU ?

}
