package org.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.example.domain.Restaurant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Hello world!
 */
public class App {
    public static String BASE_URL = "https://www.tripadvisor.com.ar";

    public static void main(String[] args) throws Exception { // TODO: manejar excepciones
        System.out.println("Hello World!");
        /*Restaurant restaurant =
        getRestaurantByUrl("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d15801762-Reviews-Comedor_Central-Santiago_Santiago_Metropolitan_Region.html");
        System.out.println(restaurant);*/

        /*
        Restaurant r2 =
                getRestaurantInfoByPage("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d13137499-Reviews-Holy_Moly-Santiago_Santiago_Metropolitan_Region.html");

        System.out.println(r2); */
        //getRestaurantByUrl("https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d10021953-Reviews-Uncle_Fletch_Plaza_Nunoa-Santiago_Santiago_Metropolitan_Region.html");
        /*val restaurants = getRestaurantsByPage("https://www.tripadvisor.com.ar/Restaurants-g294305-Santiago_Santiago_Metropolitan_Region.html");
        System.out.println("Cantidad de restaurantes: " + restaurants.size());*/

        //scrapAllRestaurantsByCity("https://www.tripadvisor.com.ar/Restaurants-g294305-Santiago_Santiago_Metropolitan_Region.html");
        scrapAllRestaurantsByCity("https://www.tripadvisor.com.ar/Restaurants-g295425-Vina_del_Mar_Valparaiso_Region.html");

    }


    /**
     * To get the info of a restaurant of the page "tripadvisor.com.ar"
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static Restaurant getRestaurantByUrl(String url) throws Exception {
        //we have to find the script that contains all the info of the restaurant in the HTML of the page.
        // Tip: right click over a tag > copy > css selector
        //System.out.println("URL: " + url);
        String jsonString = getJsonFromPage(url);

        if (jsonString.isEmpty()) {
            throw new Exception("Could not get json from the page");
        }
        JsonNode jsonObject = getJsonObjectByString(jsonString); // check that jsonobject isnt null
        if (jsonObject.isNull()) {
            throw new Exception();
        }
        String restaurant_id = getRestaurantId(url);
        if (restaurant_id.isEmpty()) {
            throw new Exception("restaurant id empty");
        }

        Optional<JsonNode> restaurantDetails = Optional.ofNullable(jsonObject.get("api").get("responses").get("/data/1.0/location/" + restaurant_id));
        Optional<JsonNode> restaurantOverview = Optional.ofNullable(jsonObject.get("api").get("responses").get("/data/1.0/restaurant/" + restaurant_id + "/overview").get("data")); //another part of the JSON that also contains info


        Restaurant restaurant = getRestaurantFromData(restaurantDetails, restaurantOverview);
        return restaurant;
    }

    public static Restaurant getRestaurantFromData(Optional<JsonNode> restaurantDetails, Optional<JsonNode> restaurantOverview) {

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

        return Restaurant.builder()
                .name(name)
                .address(address)
                .extendedAddress(extendedAddress)
                .location(location)
                .country(country)
                .phone(phone)
                .score(score)
                .price(price)
                .cuisine(cuisine)
                .meals(meals)
                .email(email)
                .website(website)
                .build();
    }

    public static String getJsonFromPage(String url) {
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
            // redux ---> api -->  responses       ----> "/data/1.0/location/15801762" ??? is the api url
            String to = "\"page\":";
            int indexFrom = json.indexOf(from); //https://typingcode.wordpress.com/2013/03/19/manejo-de-strings-en-java/
            int indexTo = json.indexOf(to);
            if (indexFrom != -1 && indexTo != -1) {
                jsonString = json.substring(indexFrom, indexTo - 1);
                jsonString = "{" + jsonString + "}"; // to format the JSON properly. validar en: https://jsonformatter.curiousconcept.com/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public static JsonNode getJsonObjectByString(String json) {
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

    public static String getRestaurantId(String url) {
        // url example: "https://www.tripadvisor.com.ar/Restaurant_Review-g294305-d15801762-Reviews-Comedor_Central-Santiago_Santiago_Metropolitan_Region.html"
        // the id of the restaurant is: "15801762"
        String id = "";
        String from = "-d";
        String to = "-R";
        int indexFrom = url.indexOf(from);
        int indexTo = url.indexOf(to);
        if (indexFrom != -1 && indexTo != -1) {
            id = url.substring(indexFrom + 2, indexTo); // +2 because we have 2 chars that we dont want "-d"
        }
        return id;
    }

    /* para mejorar el codigo, para mas adelante //TODO: hacer o borrar
    public static JsonNode getRestaurantDetailsById(JsonNode jsonObject){

    }

    public static JsonNode getRestaurantOverviewById(JsonNode jsonObject){

    }
    */

    /**
     * Gets all the restaurants from a single page
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static List<Restaurant> getRestaurantsByPage(String url) throws Exception {
        System.out.println("scrapeando: " + url);
        String jsonString = getJsonFromPage(url);
        if (jsonString.isEmpty()) {
            throw new Exception("Json empty");
        }

        JsonNode jsonObject = getJsonObjectByString(jsonString);
        if (jsonObject == null) {
            throw new Exception("json object null");
        }

        JsonNode data = jsonObject.get("api").get("responses");
        //data/1.0/restaurants/294305/14785102,12950301,2410245,7755054,777610,14012119,1735271,15184025,799339,10021953,6638542,8494943,10761466,7312299,15248197,7161213,10503885,3843350,15247797,3838800,2311987,6529842,1493782,6131405,12701486,2410244,8755441,7291241,3547430,940625,5292694?tags=&reviewStubInfo=true&sponsoredLocationIndices=0     ---> the name of the part that haves all the restaurants of the page
        String json = data.toString();
        int indexFrom = json.indexOf("/data/1.0/restaurants/"); // when me use substring, the resulting string will contain this: "/data/1.0/restaurants/"
        String extension = "?tags=&reviewStubInfo=true&sponsoredLocationIndices=0";
        int indexTo = json.indexOf(extension);
        if (indexTo == -1) {
            extension = "?tags=&reviewStubInfo=false&sponsoredLocationIndices=0";
            //the extension can be different between restaurants, in some cases "&reviewStubInfo=true" and in other cases "&reviewStubInfo=false"
            indexTo = json.indexOf(extension);
        }
        String idsUrl = json.substring(indexFrom, indexTo);

        data = data.get(idsUrl + extension).get("data").get("restaurants");

        List<String> urls = getPagesUrls(data);
        System.out.println("urls array size: " + urls.size());
        val restaurants = getRestaurantsFromUrls(urls);

        System.out.println("ultimo restaurant scrapeado:" + restaurants.get(restaurants.size() - 1));
        return restaurants;
    }

    /**
     * Returns a list of restaurants from a list of restaurants urls
     *
     * @param urls
     * @return
     * @throws Exception
     */
    public static List<Restaurant> getRestaurantsFromUrls(List<String> urls) throws Exception {
        List<Restaurant> restaurants = new ArrayList<Restaurant>();

        for (int i = 0; i < urls.size(); i++) {
            restaurants.add(getRestaurantByUrl(urls.get(i)));
        }

        return restaurants;
    }

    /**
     * Gets all the pages urls from a single page
     *
     * @param data
     * @return
     */
    public static List<String> getPagesUrls(JsonNode data) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            String pageUrl = BASE_URL + data.get(i).get("detailPageUrl").asText();
            urls.add(pageUrl);
        }
        return urls;
    }

    /**
     * Scraps all the restaurants from a city
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static List<Restaurant> scrapAllRestaurantsByCity(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();
        String pageId = getPageId(url);
        String cityExtension = getPageCityExtension(url);
        int numberOfPages = getNumberOfPages(Optional.ofNullable(doc));
        int restaurantsByPage = 30; // every page has 30 restaurants, we need this info because the pagination in the url is made by the amount of restaurants, and not the pages
        //for example: "oa30" : the restaurants from 30 to 60
        //(https://www.tripadvisor.com.ar/Restaurants-g294305-oa30-Santiago_Santiago_Metropolitan_Region.html#EATERY_LIST_CONTENTS)
        List<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < numberOfPages; i++) {
            int restaurantsNumber = i * restaurantsByPage;
            String pageUrl = "https://www.tripadvisor.com.ar/Restaurants" + pageId + "-oa" + restaurantsNumber + cityExtension;
            val restaurantList = getRestaurantsByPage(pageUrl);
            System.out.println("Pagina: " + i + " Ultimo restaurant" + restaurantList.get(restaurantList.size() - 1));
            restaurants.addAll(restaurantList);
        }
        //TODO: aca guardar en BD o en CSV, fijarse si hay que guardar por cada pagina o guardar de una la lista entera.
        //TODO: retornar la lista
        return restaurants;
    }

    /**
     * Gets the total page number of the pagination from a city restaurants page
     *
     * @param doc
     * @return
     */
    public static int getNumberOfPages(Optional<Document> doc) {
        int pages = doc
                .map(document -> document.getElementsByClass("pageNumbers"))
                .map(elements -> elements.select("a"))
                .map(elements -> elements.last())
                .map(Element::text)
                .map(Integer::parseInt)
                .orElse(0);
        return pages;
    }

    /**
     * Gets the page id of a URL
     *
     * @param url
     * @return
     */
    public static String getPageId(String url) {
        //Example: https://www.tripadvisor.com.ar/Restaurants-g294305-Santiago_Santiago_Metropolitan_Region.html
        // we want to extract "-g294305"
        int from = url.indexOf("-");
        int to = url.lastIndexOf("-");
        String id = url.substring(from, to);
        return id;
    }

    /**
     * Gets the city extension of a URL.
     *
     * @param url
     * @return
     */
    public static String getPageCityExtension(String url) {
        //https://www.tripadvisor.com.ar/Restaurants-g294305-Santiago_Santiago_Metropolitan_Region.html
        //we want to extract: "-Santiago_Santiago_Metropolitan_Region.html"
        int from = url.lastIndexOf("-");
        String extension = url.substring(from);
        return extension;
    }

    // ------ The methods here below are for getting info from the restaurant json ------
    public static String getNameFromJson(Optional<JsonNode> restaurantDetails) {

        val name = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("name"))
                .map(JsonNode::asText)
                .orElse(""); //to return it empty if the data doesn't exist in the JSON

        return name;
    }

    public static String getAddressFromJson(Optional<JsonNode> restaurantDetails) {

        String address = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("address_obj"))
                .map(restaurantJson -> restaurantJson.get("street1"))
                .map(JsonNode::asText)
                .orElse("");
        //Its a better way of doing this:
        /*
        String address1= "";
        if (restaurantDetails.get("data").get("address_obj").get("street1") != null){
            address1 = restaurantDetails.get("data").get("address_obj").get("street1").asText();
        }
         */
        return address;
    }

    public static String getExtendedAddressFromJson(Optional<JsonNode> restaurantDetails) {
        String extendedAddress = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("address_obj"))
                .map(restaurantJson -> restaurantJson.get("street2"))
                .map(JsonNode::asText)
                .orElse("");

        return extendedAddress;
    }

    public static String getLocationFromJson(Optional<JsonNode> restaurantDetails) {
        String location = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("location_string"))
                .map(JsonNode::asText)
                .orElse("");

        return location;
    }

    public static String getCountryFromJson(Optional<JsonNode> restaurantDetails) {
        String country = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("address_obj"))
                .map(restaurantJson -> restaurantJson.get("country"))
                .map(JsonNode::asText)
                .orElse("");

        return country;
    }

    public static String getPhoneFromJson(Optional<JsonNode> restaurantDetails) {
        String phone = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("phone"))
                .map(JsonNode::asText)
                .orElse("");

        return phone;
    }

    public static String getScoreFromJson(Optional<JsonNode> restaurantDetails) {
        String score = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("rating"))
                .map(JsonNode::asText)
                .orElse("");

        return score;
    }

    public static String getPriceFromJson(Optional<JsonNode> restaurantDetails) {
        String price = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("price"))
                .map(JsonNode::asText)
                .orElse("");

        return price;
    }

    public static List<String> getCuisineFromJson(Optional<JsonNode> restaurantDetails) {
        List<String> cuisine = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("cuisine"))
                .map(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false) // para que el jsonnode pueda soportar el stream, ya q si se hace Stream directamente, tira error
                        .map(restarauntJson -> restarauntJson.get("name"))
                        .map(JsonNode::asText)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        //the same as this:
        /*
        if (restaurantDetails.get("data").get("cuisine") != null){
            for (JsonNode node : restaurantDetails.get("data").get("cuisine")){ //the meals are inside an array named "cuisine"l
                cuisine.add(node.get("name").asText());
            }
        }
        */
        return cuisine;
    }

    public static List<String> getMealsFromJson(Optional<JsonNode> restaurantOverview) {
        List<String> meals = restaurantOverview
                .map(restaurantJson -> restaurantJson.get("detailCard"))
                .map(restaurantJson -> restaurantJson.get("tagTexts"))
                .map(restaurantJson -> restaurantJson.get("meals"))
                .map(restaurantJson -> restaurantJson.get("tags"))
                .map(jsonNode -> StreamSupport.stream(jsonNode.spliterator(), false) // para que el jsonnode pueda soportar el stream, ya q si se hace Stream directamente, tira error
                        .map(restaurantJson -> restaurantJson.get("tagValue"))
                        .map(JsonNode::asText)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        // the same as this:
        /*
        List<String> meals = new ArrayList<String>();
        if (restaurantOverview.get("detailCard").get("tagTexts").get("meals").get("tags") != null){
            for (JsonNode node : restaurantOverview.get("detailCard").get("tagTexts").get("meals").get("tags")){
                meals.add(node.get("tagValue").asText());
            }
        }
         */
        return meals;
    }

    public static String getEmailFromJson(Optional<JsonNode> restaurantDetails) {
        String email = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("email"))
                .map(JsonNode::asText)
                .orElse("");

        return email;
    }

    public static String getWebsiteFromJson(Optional<JsonNode> restaurantDetails) {
        String website = restaurantDetails
                .map(restaurantJson -> restaurantJson.get("data"))
                .map(restaurantJson -> restaurantJson.get("website"))
                .map(JsonNode::asText)
                .orElse("");

        return website;
    }

}