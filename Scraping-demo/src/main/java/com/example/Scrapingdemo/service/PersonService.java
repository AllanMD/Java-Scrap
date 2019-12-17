package com.example.Scrapingdemo.service;

import com.example.Scrapingdemo.domain.Person;
import com.example.Scrapingdemo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {
    @Autowired
    private PersonRepository personRepository;

    public List<Person> findAll(){
        return personRepository.findAll();
    }

    public void save (Person p){
        personRepository.save(p);
    }

    public void deleteById(String id){
        personRepository.deleteById(id);
    }
}
