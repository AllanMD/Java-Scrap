package com.avalith.Disfruitscraping.service;

import com.avalith.Disfruitscraping.model.Product;
import com.avalith.Disfruitscraping.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(@Autowired ProductRepository productRepository){
        this.productRepository = productRepository;
    }




    public void scrapSingleProduct(String url) throws IOException {

        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();

        Elements element = doc.getElementsByTag("script");
        String jsonString = getJsonStringFromScripts(element.toString());

        Optional<JsonNode> jsonObject =  getJsonObjectFromString(jsonString);

        List<String> categories = getProductCategoryFromDocument(doc);
        Product product = getProductFromJsonObject(jsonObject , categories);

        System.out.println(product);
    }

    public String getJsonStringFromScripts(String scripts){
        int indexFrom = scripts.indexOf("<script type=\"application/ld+json\">");
        String jsonString = scripts.substring(indexFrom);
        jsonString = jsonString.replace("<script type=\"application/ld+json\">", "");

        int indexTo = jsonString.indexOf("</script>");

        jsonString = jsonString.substring(0, indexTo);

        return jsonString;
    }

    public Optional<JsonNode> getJsonObjectFromString(String jsonString) throws IOException {
        ObjectMapper JSON_MAPPER = new ObjectMapper();

        Optional<JsonNode> jsonObject = Optional.ofNullable(JSON_MAPPER.readValue(jsonString, JsonNode.class));

        return jsonObject;

    }

    public Product getProductFromJsonObject(Optional<JsonNode> jsonObject, List<String> categories){
        String name = getProductNameFromJson(jsonObject);
        float price = getProductPriceFromJson(jsonObject);

        return Product.builder()
                .name(name)
                .categories(categories)
                .price(price)
                .build();
    }


    public List<String> getProductCategoryFromDocument(Document document){
        Optional<Elements> elements = Optional.ofNullable(document.select(".posted_in").select("a"));

        List<String> categories = elements.get().stream()// preguntar esto. si saco el get(), el mapeado junta todo en un solo string
                .map(element -> element.text())
                .map(String::valueOf)
                .collect(Collectors.toList());

        return categories;
    }

    // ------------- GETTERS FOR JSONOBJECTS -------
    public String getProductNameFromJson (Optional<JsonNode> jsonObject){
        //https://www.clubdetecnologia.net/blog/2017/java-8-entender-aceptar-y-aprovechar-la-clase-optional/
        return jsonObject
                .map(jsonNode -> jsonNode.get("name"))
                .map(JsonNode::toString)
                .orElse("");
    }

    public float getProductPriceFromJson (Optional<JsonNode> jsonObject){

        return jsonObject
                .map(jsonNode -> jsonNode.get("offers").get(0).get("priceSpecification").get("price").asText()) // preguntar aca lo De NULL, me tira nullpointer exception si no existe alguno de los get()
                .map(Float::parseFloat)
                .orElse(0f);
    }


}
