package com.example.Scrapingdemo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//https://www.baeldung.com/spring-response-entity
@RestController()
@RequestMapping("")
public class MainController {

    @GetMapping("")
    public ResponseEntity<String> HelloAvalith(){
        return new ResponseEntity<>("Hola Avalith", HttpStatus.OK);
    }
}
