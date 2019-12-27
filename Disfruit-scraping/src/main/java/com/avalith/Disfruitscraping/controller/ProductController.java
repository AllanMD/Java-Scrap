package com.avalith.Disfruitscraping.controller;

import com.avalith.Disfruitscraping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("")
public class ProductController {
    private ProductService productService;

    public ProductController(@Autowired ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public void scrap  (){
        try {
            productService.scrapSingleProduct("https://www.disfruit.co/producto/arandano/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
