package com.avalith.Productsscraping;

import com.avalith.Productsscraping.model.Product;
import com.avalith.Productsscraping.service.ProductService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ProductsScrapingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsScrapingApplication.class, args);

	}

}
