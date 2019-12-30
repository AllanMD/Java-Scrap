package com.avalith.Disfruitscraping.controller;

import com.avalith.Disfruitscraping.model.Product;
import com.avalith.Disfruitscraping.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("")
@Slf4j //para log
public class ProductController {
    private ProductService productService;

    public ProductController(@Autowired ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/scrapProduct")
    public Product scrapSingleProduct() {
        try {
            return productService.scrapSingleProduct("https://www.disfruit.co/producto/arandano/");
        } catch (IOException e) {
            log.error("Error al scrapear un producto individual", e.getStackTrace()); // e.getStackTrace de ejemplo para mostrar como printear el stacktrace al logear
            throw new RuntimeException(e); // para enviar un error 500 en la peticion, sino retorna un status OK por mas que haya un error interno
        }
    }

    @GetMapping("/scrapPage")
    public List<Product> scrapPage() {
        try {
            return productService.scrapProductsPage("https://www.disfruit.co/product-category/frutas/page/1");
        } catch (IOException e) {
            log.error("Error al scrapear pagina");
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/scrapCategory")
    public List<Product> scrapCategory() {
        try {
            return productService.scrapCategory("https://www.disfruit.co/product-category/frutas/");
        } catch (IOException e) {

            log.error("Error al scrapear categoria");
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/scrapAll")
    public List<Product> scrapAllProducts() {
        try {
            return productService.scrapAllProducts("https://www.disfruit.co/");
        } catch (IOException e) {

            log.error("error al scrapear todos los productos");
            throw new RuntimeException(e);
        }
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }
}
