package org.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.Restaurant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hello world!
 *
 */
public class App 
{
    public static String BASE_URL = "https://www.tripadvisor.com.ar";

    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );
        /*Restaurant restaurant =
        getRestaurantByUrl("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d15801762-Reviews-Comedor_Central-Santiago_Santiago_Metropolitan_Region.html");
        System.out.println(restaurant);*/

        /*
        Restaurant r2 =
                getRestaurantInfoByPage("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d13137499-Reviews-Holy_Moly-Santiago_Santiago_Metropolitan_Region.html");

        System.out.println(r2); */
        //getRestaurantByUrl("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d10021953-Reviews-Uncle_Fletch_Plaza_Nunoa-Santiago_Santiago_Metropolitan_Region.html");
        getRestaurantsByPage("https://www.tripadvisor.com.ar/Restaurants-g294305-Santiago_Santiago_Metropolitan_Region.html");
    }


    /**
     * To get the info of a restaurant of the page "tripadvisor.com.ar"
     * @param url
     * @return
     * @throws Exception
     */
    public static Restaurant getRestaurantByUrl(String url) throws Exception {
        //we have to find the script that contains all the info of the restaurant in the HTML of the page.
        // Tip: right click over a tag > copy > css selector
        System.out.println("URL: " + url);
        String jsonString = getJsonFromPage(url);
        System.out.println("JSON: " + jsonString);

        if (jsonString.isEmpty()){
            throw new Exception("Could not get json from the page");
        }
        JsonNode jsonObject = getJsonObjectByString(jsonString); // check that jsonobject isnt null
        if (jsonObject.isNull()){
            throw new Exception();
        }
        String restaurant_id = getRestaurantId(url);
        if (restaurant_id.isEmpty()){
            throw new Exception("restaurant id empty");
        }

        JsonNode restaurantDetails = jsonObject.get("api").get("responses").get("/data/1.0/location/"+ restaurant_id);
        JsonNode restaurantOverview = jsonObject.get("api").get("responses").get("/data/1.0/restaurant/"+restaurant_id+"/overview").get("data"); //another part of the JSON that also contains info

        String name = getNameFromJson(restaurantDetails);

        String address = getAddressFromJson(restaurantDetails);

        String extendedAddress = getExtendedAddressFromJson(restaurantDetails);

        String location = getLocationFromJson(restaurantDetails);

        String country = getCountryFromJson(restaurantDetails);

        String phone = getPhoneFromJson(restaurantDetails);

        String score = getScoreFromJson(restaurantDetails);

        String price = getPriceFromJson(restaurantDetails);

        List<String> cuisine = getCuisineFromJson(restaurantDetails);

        // meals are in another part of the JSON
        List<String> meals = getMealsFromJson(restaurantOverview);

        String email = getEmailFromJson(restaurantDetails);

        String website = getWebsiteFromJson(restaurantDetails);

        Restaurant restaurant = new Restaurant(name,address,extendedAddress,location,country,phone,score,price,meals,cuisine,email,website);

        return restaurant;
    }

    public static String getJsonFromPage(String url){
        String jsonString = "";
        try {
            Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0) // jsoup has a default maxBodySize and thats why i received an uncomplete body
                    .timeout(600000)
                    .get();

	    // we get all the scripts of the page, and from that list of scripts, we keep the one that contains "window.__WEB_CONTEXT", since 		    //there is all the info
            Optional<String> script = doc.getElementsByTag("script").stream()
                    .map(Element::toString)
                    .filter(scriptBody -> scriptBody.contains("window.__WEB_CONTEXT_"))
                    .findAny();
                    // getElementsByTag: Elements // returns Elements
                    // .stream : Stream<Element>
                    // ? : Stream<String>
                    // .filter : Stream<Element>
                    // .collect : Element


            String json = script.get(); // get() to obtain the data from the optional

             String from = "\"api\":"; // https://www.lawebdelprogramador.com/foros/Java/596168-introduccion-de-comillas-en-un-string-java.html
            // redux ---> api -->  responses       ----> "/data/1.0/location/15801762" ??? es la url de la api
            String to = "\"page\":";
            int indexFrom = json.indexOf(from); //https://typingcode.wordpress.com/2013/03/19/manejo-de-strings-en-java/
            int indexTo = json.indexOf(to);
            if (indexFrom != -1 && indexTo != -1){
                jsonString = json.substring(indexFrom, indexTo-1);
                jsonString = "{" + jsonString + "}"; // to format the JSON properly. validar en: https://jsonformatter.curiousconcept.com/
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
            jsonObject = JSON_MAPPER.readValue(json, JsonNode.class); //JsonNode is the equivalent to the JSON Object from Gson.
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

    /* para mejorar el codigo, para mas adelante
    public static JsonNode getRestaurantDetailsById(JsonNode jsonObject){

    }

    public static JsonNode getRestaurantOverviewById(JsonNode jsonObject){

    }
    */


    public static List<Restaurant> getRestaurantsByPage(String url) throws Exception {
        String jsonString = getJsonFromPage(url);
        if (jsonString.isEmpty()){
            throw new Exception("Json empty");
        }

        JsonNode jsonObject = getJsonObjectByString(jsonString);
        if (jsonObject == null){
            throw new Exception("json object null");
        }

        JsonNode data = jsonObject.get("api").get("responses");//.get("data").get("restaurants");
        //data/1.0/restaurants/294305/14785102,12950301,2410245,7755054,777610,14012119,1735271,15184025,799339,10021953,6638542,8494943,10761466,7312299,15248197,7161213,10503885,3843350,15247797,3838800,2311987,6529842,1493782,6131405,12701486,2410244,8755441,7291241,3547430,940625,5292694?tags=&reviewStubInfo=true&sponsoredLocationIndices=0     ---> the name of the part that haves all the restaurants of the page
        String json = data.toString();
        int indexFrom = json.indexOf("/data/1.0/restaurants/"); // when me use substring, the resulting string will contain this: "/data/1.0/restaurants/"
        int indexTo = json.indexOf("?tags=&reviewStubInfo=true&sponsoredLocationIndices=0");
        String idsUrl = json.substring(indexFrom, indexTo);

        data = data.get( idsUrl + "?tags=&reviewStubInfo=true&sponsoredLocationIndices=0").get("data").get("restaurants");

        List<String> urls = getPagesUrls(data);
        System.out.println("urls array size: " + urls.size());
        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        for (int i = 0; i < urls.size(); i++) {
            restaurants.add(getRestaurantByUrl(urls.get(i)));

        }

        System.out.println(restaurants);
        return restaurants;
    }

    public static List<String> getPagesUrls(JsonNode data){
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String pageUrl = BASE_URL + data.get(i).get("detailPageUrl").asText();
            urls.add(pageUrl);
        }
        return urls;
    }
    // ------ The methods here below are for getting info from the restaurant json ------
    public static String getNameFromJson(JsonNode restaurantDetails){
        String name = ""; //to return it empty if the data doesnt exist in the JSON

        if (restaurantDetails.get("data").get("name") != null){
            name = restaurantDetails.get("data").get("name").asText();
        }

        return name;
    }

    public static String getAddressFromJson(JsonNode restaurantDetails){
        String address = "";
        if (restaurantDetails.get("data").get("address_obj").get("street1") != null){
            address = restaurantDetails.get("data").get("address_obj").get("street1").asText();
        }
        return address;
    }

    public static String getExtendedAddressFromJson(JsonNode restaurantDetails){
        String extendedAddress = "";
        if (restaurantDetails.get("data").get("address_obj").get("street2") != null){
            extendedAddress = restaurantDetails.get("data").get("address_obj").get("street2").asText();
        }
        return extendedAddress;
    }

    public static String getLocationFromJson(JsonNode restaurantDetails){
        String location = "";
        if (restaurantDetails.get("data").get("location_string") != null){
            location = restaurantDetails.get("data").get("location_string").asText();
        }
        return location;
    }

    public static String getCountryFromJson(JsonNode restaurantDetails){
        String country = "";
        if (restaurantDetails.get("data").get("address_obj").get("country") != null){
            country = restaurantDetails.get("data").get("address_obj").get("country").asText();
        }
        return country;
    }

    public static String getPhoneFromJson(JsonNode restaurantDetails){
        String phone = "";
        if (restaurantDetails.get("data").get("phone") != null){
            phone = restaurantDetails.get("data").get("phone").asText();
        }
        return phone;
    }

    public static String getScoreFromJson(JsonNode restaurantDetails){
        String score = "";
        if (restaurantDetails.get("data").get("rating") != null){
            score = restaurantDetails.get("data").get("rating").asText();
        }
        return score;
    }

    public static String getPriceFromJson(JsonNode restaurantDetails){
        String price = "";
        if (restaurantDetails.get("data").get("price") != null){
            price = restaurantDetails.get("data").get("price").asText();
        }
        return price;
    }

    public static List<String> getCuisineFromJson(JsonNode restaurantDetails){
        List<String> cuisine = new ArrayList<String>();
        if (restaurantDetails.get("data").get("cuisine") != null){
            for (JsonNode node : restaurantDetails.get("data").get("cuisine")){ //the meals are inside an array named "cuisine"l
                cuisine.add(node.get("name").asText());
            }
        }
        return cuisine;
    }

    public static List<String> getMealsFromJson(JsonNode restaurantOverview){
        List<String> meals = new ArrayList<String>();
        if (restaurantOverview.get("detailCard").get("tagTexts").get("meals").get("tags") != null){
            for (JsonNode node : restaurantOverview.get("detailCard").get("tagTexts").get("meals").get("tags")){
                meals.add(node.get("tagValue").asText());
            }
        }
        return meals;
    }

    public static String getEmailFromJson(JsonNode restaurantDetails){
        String email = "";

        if (restaurantDetails.get("data").get("email") != null){
            email = restaurantDetails.get("data").get("email").asText();
        }

        return email;
    }

    public static String getWebsiteFromJson(JsonNode restaurantDetails){
        String website = "";

        if (restaurantDetails.get("data").get("website") != null){
            website = restaurantDetails.get("data").get("website").asText();
        }

        return website;
    }

}
