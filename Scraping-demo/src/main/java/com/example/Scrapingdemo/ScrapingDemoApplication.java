package com.example.Scrapingdemo;

import com.example.Scrapingdemo.domain.Person;
import com.example.Scrapingdemo.repository.MongoDB;
import com.example.Scrapingdemo.repository.PersonRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@SpringBootApplication
public class ScrapingDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapingDemoApplication.class, args);
		// ---------------- JSOUP ---------------------
		/*
		String bodyFragment =
				"<div><a href=\"/documentation\">Stack Overflow Documentation</a></div>";

		Document doc = Jsoup.parseBodyFragment(bodyFragment);
		String link = doc
				.select("div > a")
				.first()
				.attr("href");

		System.out.println(link);*/

/*
		
		//https://jsoup.org/cookbook/extracting-data/dom-navigation
		try {
			Document doc = Jsoup.connect("http://en.wikipedia.org/").get();
			System.out.println(doc.title());
			Element content = doc.getElementById("mp-tfa"); // to get the featured article of the day in wikipedia
			Elements article = content.getElementsByTag("p"); // from the featured article DOM, we get all the <p> tags

			System.out.println(article); //prints the text including html tags
			System.out.println(article.text()); // prints only the text of the element, ignoring the HTML tags inside of it

			Elements links = content.getElementsByTag("a");
			for (Element link : links) {
				String linkHref = link.attr("href");
				String linkText = link.text();

				System.out.println(linkHref); // the link which is referenced in the text
				System.out.println(linkText); // the text displayed on screen
			}

			Elements hrefs = content.getElementsByTag("p").select("a"); //inside of the <p> tags searches for all the <a> tags
			Element href = content.getElementsByTag("p").select("a").first(); // first(): Get the first matched element.
			String relHref = href.attr("href");
			String absHref = href.attr("abs:href");
			//System.out.println(hrefs);
			//System.out.println(href);

			System.out.println("rel href: " + relHref); // returns the relative link. eg: /hotels
			System.out.println("abs href: " + absHref); // prints the absolute href. eg: https://example.com/hotels
		} catch (IOException e) {
			e.printStackTrace();
		}

		//there are more JSOUP functions. investigate. for eg: remove elements from a html
*/
		/// ------------------ JACKSON -----------------
		// ---> agregar dependencia con la version mas reciente
		ObjectMapper JSON_MAPPER = new ObjectMapper();

		Person person = new Person("1", "Allan", 22);
		String jsonString = null;
		try {
			jsonString = JSON_MAPPER.writeValueAsString(person);
			System.out.println("JSONSTRING: " + jsonString);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}


		try {
			Person allan = JSON_MAPPER.readValue(jsonString, Person.class);
			System.out.println("allan:" + allan);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Person a = new Person("2","Juan",25);
		Person b = new Person("3","Mario", 42);
		ArrayList<Person> array = new ArrayList<Person>();
		array.add(a);
		array.add(b);
		//json array
		try {
			jsonString = JSON_MAPPER.writeValueAsString(array);
			ArrayList<Person> persons = JSON_MAPPER.readValue(jsonString,
					JSON_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Person.class));

			System.out.println("array JSON:" + persons);
		} catch (IOException e) {
			e.printStackTrace();
		}




		//--------- MONGODB --------
		// made in the PersonRepository y tested in the MainController

	}

}
