package org.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.Restaurant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Restaurant restaurant =
        getRestaurantInfoByPage("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d15801762-Reviews-Comedor_Central-Santiago_Santiago_Metropolitan_Region.html");
    }

    /**
     * To get the info of a restaurant of the page "tripadvisor.com.ar"
     * @param url
     * @return
     */
    public static Restaurant getRestaurantInfoByPage(String url){
        String jsonString = getJsonFromPage(url);
        JsonNode jsonObject = getJsonObjectByString(jsonString);
        String restaurant_id = getRestaurantId(url);
        jsonObject = jsonObject.get("api").get("responses").get("/data/1.0/location/"+ restaurant_id);
        System.out.println("Restauran id: " + restaurant_id);

        String name = jsonObject.get("data").get("name").asText();
        System.out.println("NOMBRE DE RESTAURANTE: " + name);

        String address = jsonObject.get("data").get("address_obj").get("street1").asText();
        System.out.println("Direccion: " + address);

        String extendedAddress = jsonObject.get("data").get("address_obj").get("street2").asText();
        System.out.println("Direccion extendida: " + extendedAddress);

        String location = jsonObject.get("data").get("location_string").asText();
        System.out.println("location: " + location);

        String country = jsonObject.get("data").get("address_obj").get("country").asText();
        System.out.println("country: " + country);

        String phone = jsonObject.get("data").get("phone").asText();
        System.out.println("phone: " + phone);

        String score = jsonObject.get("data").get("rating").asText();
        System.out.println("score: " + score);

        String price = jsonObject.get("data").get("price").asText();
        System.out.println("price: " + price);

        // List<String> meals = jsonObject.get("data").get() // meals y mealstypes estan en otra parte del JSON

        String email = jsonObject.get("data").get("email").asText();
        System.out.println("email: " + email);

        return new Restaurant();

        // buscar el script que tiene toda la info en el html, de ahi se saca toda la info. Hacer click derecho > copiar > selector de css
    }

    public static String getJsonFromPage(String url){
        String jsonString = "";
        try {
            Document doc = Jsoup.connect(url).get();
            Element body = doc.selectFirst("#BODY_BLOCK_JQUERY_REFLOW");  // trae toda la pagina entera, de ahi se busca el JSON que contiene todos los datos importantes
             String json = body.toString();
            String from = "\"api\":"; // https://www.lawebdelprogramador.com/foros/Java/596168-introduccion-de-comillas-en-un-string-java.html
            // redux ---> api -->  responses       ----> "/data/1.0/location/15801762" ??? es la url de la api
            String to = "\"page\":";
            int indexFrom = json.indexOf(from); //https://typingcode.wordpress.com/2013/03/19/manejo-de-strings-en-java/
            int indexTo = json.indexOf(to);
            if (indexFrom != -1 && indexTo != -1){
                jsonString = json.substring(indexFrom, indexTo-1);
                jsonString = "{" + jsonString + "}"; // formatear correctamente el json. validar en: https://jsonformatter.curiousconcept.com/
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static JsonNode getJsonObjectByString(String json){
        ObjectMapper JSON_MAPPER = new ObjectMapper();
        JsonNode jsonObject = null;
        try {
            jsonObject = JSON_MAPPER.readValue(json, JsonNode.class); // json node es el equivalente a JSON Object de Gson.
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static String getRestaurantId (String url){
        // url example: "https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d15801762-Reviews-Comedor_Central-Santiago_Santiago_Metropolitan_Region.html"
        // the id of the restaurant is: "15801762"
        String id = "";
        String from = "-d";
        String to = "-R";

        int indexFrom = url.indexOf(from);
        int indexTo = url.indexOf(to);
        if (indexFrom != -1 && indexTo != -1){
            id = url.substring(indexFrom+2, indexTo); // +2 because we have 2 chars that we dont want "-d"
        }
        return id;
    }




}
