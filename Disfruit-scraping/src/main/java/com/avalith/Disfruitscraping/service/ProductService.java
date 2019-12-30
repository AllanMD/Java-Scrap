package com.avalith.Disfruitscraping.service;

import com.avalith.Disfruitscraping.model.Product;
import com.avalith.Disfruitscraping.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j // para log
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(@Autowired ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(Product product) {
        productRepository.save(product);
    }

    public void saveAll(List<Product> products) {
        productRepository.saveAll(products);
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    /**
     * Scraps a single product page
     * @param url: The url of the single product
     * @return Product object
     * @throws IOException
     */
    public Product scrapSingleProduct(String url) throws IOException {

        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();

        Elements element = doc.getElementsByTag("script");
        String jsonString = getJsonStringFromScripts(element.toString());
        Optional<JsonNode> jsonObject = getJsonObjectFromString(jsonString);

        List<String> categories = getProductCategoryFromDocument(doc);
        Product product = getProductFromJsonObject(jsonObject, categories);

        return product;
    }

    /**
     * Gets the part of all the scripts that contains the Json with the info we need and formats it into a json string
     * @param scripts
     * @return
     */
    public String getJsonStringFromScripts(String scripts) {
        int indexFrom = scripts.indexOf("<script type=\"application/ld+json\">");
        String jsonString = scripts.substring(indexFrom);
        jsonString = jsonString.replace("<script type=\"application/ld+json\">", "");

        int indexTo = jsonString.indexOf("</script>");

        jsonString = jsonString.substring(0, indexTo);

        return jsonString;
    }

    /**
     * Casts the Json String into an Json Object
     * @param jsonString
     * @return
     * @throws IOException
     */
    public Optional<JsonNode> getJsonObjectFromString(String jsonString) throws IOException {
        ObjectMapper JSON_MAPPER = new ObjectMapper();

        Optional<JsonNode> jsonObject = Optional.ofNullable(JSON_MAPPER.readValue(jsonString, JsonNode.class));

        return jsonObject;
    }

    /**
     * Gets a Product Object from a Json Object
     * @param jsonObject: The json Object
     * @param categories: The product category
     * @return
     */
    public Product getProductFromJsonObject(Optional<JsonNode> jsonObject, List<String> categories) {
        String name = getProductNameFromJson(jsonObject);
        float minPrice = getMinPriceFromJson(jsonObject);
        float maxPrice = getMaxPriceFromJson(jsonObject);

        return Product.builder()
                .name(name)
                .categories(categories)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    /**
     * Gets the category from a products page
     * @param document: The page document
     * @return
     */
    public List<String> getProductCategoryFromDocument(Document document) {
        Optional<Elements> elements = Optional.ofNullable(document.select(".posted_in").select("a"));

        return elements.stream().flatMap(Collection::stream)//si hago stream sobre un optional, hay q usar flatmap
                .map(Element::text)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Scraps a single page with a list of products
     * @param url: The url of the products page
     * @return A list of products
     * @throws IOException
     */
    public List<Product> scrapProductsPage(String url) throws IOException {
        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();

        Optional<Elements> productsLinks = Optional.ofNullable(doc.getElementsByClass("products columns-5").select(".woocommerce-LoopProduct-link")); // selects the class tag:

        val productList = productsLinks.stream()
                .flatMap(Collection::stream) // si se hace stream sobre un optional hay que usar esto
                .map(element -> { //https://dzone.com/articles/exception-handling-in-java-streams
                    try {
                        return scrapSingleProduct(element.attr("href"));
                    } catch (IOException e) {
                        log.error("Error con la URL al scrapear un producto individual", e.getStackTrace());
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        System.out.println(productList);

        return productList;
    }

    /**
     * Scraps all the products of a certain category (going through all the pages)
     * @param url: The url of the category page
     * @return A list of products
     * @throws IOException
     */
    public List<Product> scrapCategory(String url) throws IOException {
        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();

        Optional<Element> pagesElement = Optional.ofNullable(doc.getElementsByClass("page-numbers").not(".current").not(".next").last()); // last para saber la cantidad de paginas que hay

        // Optional para comprobar si hay paginacion o no
        int pages = pagesElement
                .map(Element::text)
                .map(Integer::parseInt)
                .orElse(1);

        val products = new ArrayList<Product>();
        log.info("Scrapeando categoria..");
        for (int i = 1; i <= pages; i++) { //because the pages starts with 1 , and not 0
            System.out.println("Scrapeando: " + url + "page/" + i);
            products.addAll(scrapProductsPage(url + "page/" + i));
        }
        return products;
    }

    /**
     * Scraps all the products of the entire page (going through all the categories)
     * @param url : the url of the web page (disfruit.co)
     * @return A list of products
     * @throws IOException
     */
    public List<Product> scrapAllProducts(String url) throws IOException {
        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0)
                .timeout(600000)
                .get();

        Optional<Elements> categoriesLinksElement = Optional.ofNullable(doc.getElementById("primary-menu").getElementsByClass("sub-menu").first().select("a"));
        val categoriesLinks = categoriesLinksElement
                .stream()
                .flatMap(Collection::stream)
                .map(element -> element.attr("href"))
                .collect(Collectors.toList());

        val products = categoriesLinks.stream()
                .map(category -> {
                    try {
                        return scrapCategory(category);
                    } catch (IOException e) {
                        log.error("Error al scrapear todos los productos", e.getStackTrace());
                        throw new RuntimeException(e); // para cortar el programa ya que en este caso habria un problema con los URLS de la pagina y no se haria un scrapeo correcto
                    }
                })
                .flatMap(Collection::stream)// bajamos un nivel, osea sobre la lista retornada, hacemos stream tambien para poder tratar cada producto de forma individual y coleccionarlos a todos en una lista individual
                .collect(Collectors.toList());

        System.out.println("Cantidad de productos scrapeados (incluidos duplicados): " + products.size());

        saveAll(products);
        return products;

    }

    // ------------- GETTERS FOR JSONOBJECTS -------
    public String getProductNameFromJson(Optional<JsonNode> jsonObject) {
        //https://www.clubdetecnologia.net/blog/2017/java-8-entender-aceptar-y-aprovechar-la-clase-optional/
        return jsonObject
                .map(jsonNode -> jsonNode.get("name"))
                .map(JsonNode::toString)
                .orElse("");
    }

    public float getSinglePriceFromJson(Optional<JsonNode> jsonObject) {

        return jsonObject
                .map(jsonNode -> jsonNode.get("offers")) // Error: me tira nullpointer exception si no existe alguno de los get(). SOLUCIONADO
                .map(jsonNode -> jsonNode.get(0))
                .map(jsonNode -> jsonNode.get("price")) // Solucion : hay que separar todo en distintos maps para evitar el null pointer exception
                .map(JsonNode::asText)
                .map(Float::parseFloat)
                .orElse(0f);
    }

    public float getMinPriceFromJson(Optional<JsonNode> jsonObject) {
        return jsonObject
                .map(jsonNode -> jsonNode.get("offers"))
                .map(jsonNode -> jsonNode.get(0))
                .map(jsonNode -> jsonNode.get("lowPrice"))
                .map(JsonNode::asText)
                .map(Float::parseFloat)
                .orElse(getSinglePriceFromJson(jsonObject));
    }

    public float getMaxPriceFromJson(Optional<JsonNode> jsonObject) {
        return jsonObject
                .map(jsonNode -> jsonNode.get("offers"))
                .map(jsonNode -> jsonNode.get(0))
                .map(jsonNode -> jsonNode.get("highPrice"))
                .map(JsonNode::asText)
                .map(Float::parseFloat)
                .orElse(getSinglePriceFromJson(jsonObject));
    }
}


// TODO: borrar los prints ?
// TODO: cambiar comentarios ?
// TODO: preguntar por SKU