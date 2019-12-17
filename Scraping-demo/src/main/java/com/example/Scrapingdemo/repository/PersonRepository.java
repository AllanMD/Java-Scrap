package com.example.Scrapingdemo.repository;

import com.example.Scrapingdemo.domain.Person;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends MongoRepository<Person,String> { // first parameter is the type of the data and the second the type of the id

    //List<Person> findAllByAgeGreater(int floor); // ejemplo de una query personalizada
    public Person findByName(String name);



    // MongoRepository provides all the basic methods like save, find, findall, delete, udpate, etc.

}
