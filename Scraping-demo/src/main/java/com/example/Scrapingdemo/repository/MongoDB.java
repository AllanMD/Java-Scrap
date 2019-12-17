package com.example.Scrapingdemo.repository;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.Document;

//Esto es para hacer manualmente la base de datos Mongodb, pero es mas facil y rapido usar MongoRepository. Esto esta como ejemplo nada mas
@Data
public class MongoDB {
    protected MongoDatabase database;

    public MongoDB(){
        // Creating a Mongo client
        MongoClient mongo = new MongoClient( "localhost" , 27017 );

        // Creating Credentials
        MongoCredential credential;
        credential = MongoCredential.createCredential("allan", "demo",
                "password".toCharArray());
        System.out.println("Connected to the database successfully");

        // Accessing the database
        MongoDatabase database = mongo.getDatabase("prueba"); // if the db dont exist, it creates it
        System.out.println("Credentials ::"+ credential);

        this.database = database;
    }

    public void createCollection(String name){
        try {
            //Creating a collection
            database.createCollection(name);
            System.out.println("Collection created successfully");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getAll(){
        // Retieving a collection
        MongoCollection<Document> collection = database.getCollection("personas");
        System.out.println("Collection selected successfully");
        System.out.println(collection);
        return collection;

    }

}
