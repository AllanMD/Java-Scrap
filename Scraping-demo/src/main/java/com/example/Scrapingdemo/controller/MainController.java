package com.example.Scrapingdemo.controller;

import com.example.Scrapingdemo.domain.Person;
import com.example.Scrapingdemo.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

//https://www.baeldung.com/spring-response-entity
//here we test mongodb and httpclient
@RestController()
@RequestMapping("")
public class MainController {

    @Autowired
    PersonService personSerivce;

    @GetMapping("")
    public ResponseEntity<String> HelloAvalith(){
        return new ResponseEntity<>("Hola Avalith", HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Person> savePerson(@RequestBody Person person){
        personSerivce.save(person);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @GetMapping("persons")
    public ResponseEntity<List<Person>> getAll(){
        List<Person> persons = personSerivce.findAll();
        if (persons.size()==0){
            return new ResponseEntity<>(persons,HttpStatus.NO_CONTENT);
        }else {
            return new ResponseEntity<>(persons, HttpStatus.OK);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<Boolean> delete(@RequestBody String id){
        personSerivce.deleteById(id);
        return new ResponseEntity<Boolean>(HttpStatus.OK); // comprobar que este bien esto.
    }

    @GetMapping("httpclient")
    public String htttpClientExample(){
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/posts/1")) //api example
                .GET()
                .build();
        HttpResponse<String> response = null;
        try {
             response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e){
            e.printStackTrace();
        }

        return response.body();

        // solution for the net.http library not found: https://stackoverflow.com/questions/52340914/intellij-cant-find-java-net-http-when-compiling-with-java-11
    }

}
