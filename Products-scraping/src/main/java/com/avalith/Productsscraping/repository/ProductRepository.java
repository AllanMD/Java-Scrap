package com.avalith.Productsscraping.repository;

import com.avalith.Productsscraping.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> { // first parameter is the type of the data and the second the type of the id (if there is a duplicate id when saving, mongodb updates the data)

    //List<Person> findAllByAgeGreater(int floor); // eg custom query
    public Product findByName(String name);

    // MongoRepository provides all the basic methods like save, find, findall, delete, udpate, etc.
    // we have to specify host,port and database in the application.properties
}
