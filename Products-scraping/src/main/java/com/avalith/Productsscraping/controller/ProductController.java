package com.avalith.Productsscraping.controller;

import com.avalith.Productsscraping.model.Product;
import com.avalith.Productsscraping.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("")
public class ProductController {

    private ProductService productService;

    public ProductController(@Autowired ProductService productService) {
        this.productService = productService;
    }

    // RequestBody --> receive things from de HTTP Body (object)
    // RequestParam ---> receive things from the URL in query format. eg: /products?id=1
    // PathVariable ---> receive things from the URL . eg: /product/{id}
    @PostMapping("")
    public Product save(@RequestBody Product product){
        return productService.save(product);
    }

    @PostMapping("saveList")
    public void save (@RequestBody List<Product> products){
        productService.save(products);
    }

    @GetMapping("")
    public List<Product> getAll (){
        return productService.getAll();
    }

    @PostMapping("scrap")
    public List<Product> scrapPage (@RequestBody String url){
        return productService.scrap(url);
    }

}
