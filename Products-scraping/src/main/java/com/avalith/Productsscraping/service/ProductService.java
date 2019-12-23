package com.avalith.Productsscraping.service;

import com.avalith.Productsscraping.model.Product;
import com.avalith.Productsscraping.repository.ProductRepository;
import com.opencsv.CSVWriter;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ProductService {

    //TODO: Reemplazar inyeccion de dependencias del field, por inyeccion de constructor o de setter // LISTO (?)

    private ProductRepository productRepository;

    public ProductService(@Autowired ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save (Product product){
        return productRepository.save(product);
    }

    public void save (List<Product> products){
        products.forEach(product -> productRepository.save(product));
    }

    public List<Product> getAll(){
        return productRepository.findAll();
    }

    public List<Product> scrap (String url){
        //TODO: si podes reemplazar por val, y nombrar con ducktyping
        val lists = getListsFromPage("https://www.lamayorista.com.co/"); // CHANGE LATER TO URL // https://www.genbeta.com/desarrollo/entendiendo-la-inmutabilidad-que-es-para-que-sirve-y-como-usarla
        List<Product> products = getAllProductsFromLists(lists);
        //TODO: borrar comentarios // LISTO
        //TODO: retornar cantidad de productos o string // LISTO , retornado todos los productos scrapeados
        System.out.println("Cantidad de productos:" + products.size());

        save(products); // persistimos los datos en mongodb
        saveListToCSV(products);

        return products;
    }



    /**
     * Gets all the <li></li> elements that contains the products data of the url
     * @param url
     * @return
     */
    public static Elements getListsFromPage(String url){
        //TODO: trata de evitar null // usar optional
        //TODO: investigar como manejar excepciones sin try catch, sino con either, maybe, try monad
        Elements lists = null;

        try {
            Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0) // jsoup has a default maxBodySize and thats why i received an uncomplete body
                    .timeout(600000)
                    .get();

            lists = doc.getElementById("Tap").getElementsByTag("li"); // "Tap" contains the li elements

        } catch (IOException e) {
            //TODO: loggear error en vez de printear; // LISTO (?)

            // Create a Logger
            Logger logger = Logger.getLogger(ProductService.class.getName());
            logger.info(e.getMessage());

            //e.printStackTrace(); ---> delete
        }
        return lists;
    }

    /**
     *Gets all the products of an individual <li></li> element
     * @param list
     * @return
     */
    public static List<Product> getProductsFromList(Element list){
        // TODO: tratar de no mutar
        List<Product> products = new ArrayList<Product>();
        //TODO: manejar con optional
        if (list != null){
            String category = list.selectFirst("a").text(); // all the products in each list belongs to the same category
            Elements rows = list.getElementsByClass("gradeA");
            // TODO: iterar funcionalmente
            //StreamSupport.stream(rows.spliterator(), false).map().filter().reduce();

            /*
            ** Select anchors
            * getElements
            * armar producto(extraer a un metodo)
            * coleccionar a una lista
             */
            for (Element row: rows) {

                String name = getProductNameFromRow(row);
                String metrics = getProductMetricsFromRow(row);
                float priceInPesos = getPricePesosFromRow(row);
                float priceInUsd = getPriceUsdFromRow(row);
                //TODO: reemplazar por primitivos // LISTO
                float priceInEur = getPriceEurFromRow(row);
                float yesterdayMaxPrice = getYesterdayMaxPriceFromRow(row);
                float variation = getProductVariationFromRow(row);

                //TODO: usar un builder // LISTO
                Product p = Product.builder()
                        .category(category) // all the products in each list belongs to the same category
                        .name(name)
                        .metrics(metrics)
                        .priceInPesos(priceInPesos)
                        .priceInUsd(priceInUsd)
                        .priceInEur(priceInEur)
                        .yesterdayMaxPrice(yesterdayMaxPrice)
                        .variation(variation)
                        .build();

                products.add(p);
            }
        }

        return products;
    }

    /**
     * Get all the products of all <li></li> elements
     * @param lists
     * @return
     */
    public static List<Product> getAllProductsFromLists(Elements lists){
        List<Product> products = new ArrayList<Product>();
        List<Product> array = new ArrayList<>();
        for (Element list : lists){
            array = getProductsFromList(list);
            for (Product p: array) {
                products.add(p); // we have to combine all the products arrays in one
            }
        }
        return products;
    }

    // ------- GETTERS FOR SINGLE ROW OF THE <li> ELEMENT ------
    public static String getProductNameFromRow(Element row){

        //TODO: Con optional
        String name = "";
        if (row.getElementsByTag("td").get(0) != null){
            name = row.getElementsByTag("td").get(0).text();  // brings only the text inside the tag
        }
        return name;
    }

    public static String getProductMetricsFromRow(Element row){

        String metrics = "";
        if (row.getElementsByTag("td").get(1) != null){
            metrics = row.getElementsByTag("td").get(1).text();
        }
        return metrics;
    }

    public static float getPricePesosFromRow(Element row){

        String priceInPesos = "";
        float price = 0f;

        if (row.getElementsByTag("td").get(2) != null){
            priceInPesos = row.getElementsByTag("td").get(2).text();

            //TODO: usar replace() o replaceAll() // LISTO
            priceInPesos = priceInPesos.replace("$", ""); // so we can parse to float, else if it haves a "$" in the string, it will throw a parse to float exception
            priceInPesos = priceInPesos.replace(",",".");

            price = Float.parseFloat(priceInPesos);
        }
        return price;
    }

    public static float getPriceUsdFromRow(Element row){

        String priceInUsd = "";
        float price = 0f;
        if (row.getElementsByTag("td").get(3) != null){
            priceInUsd = row.getElementsByTag("td").get(3).text();

            priceInUsd = priceInUsd.replace("US$", "");
            priceInUsd = priceInUsd.replace(",",".");

            price = Float.parseFloat(priceInUsd);
        }
        return price;
    }

    public static float getPriceEurFromRow(Element row){

        String priceInEur = "";
        float price = 0f;
        if (row.getElementsByTag("td").get(4) != null){
            priceInEur = row.getElementsByTag("td").get(4).text();

            priceInEur = priceInEur.replace("€","");
            priceInEur = priceInEur.replace(",",".");

            price = Float.parseFloat(priceInEur);
        }
        return price;
    }

    public static float getYesterdayMaxPriceFromRow(Element row){

        String yesterdayMaxPrice = "";
        float maxPrice = 0f;
        if (row.getElementsByTag("td").get(5) != null){
            yesterdayMaxPrice = row.getElementsByTag("td").get(5).text();

            //TODO: usar replace() o replaceAll() // LISTO
            yesterdayMaxPrice = yesterdayMaxPrice.replace("$", "");
            yesterdayMaxPrice = yesterdayMaxPrice.replace(",",".");

            maxPrice = Float.parseFloat(yesterdayMaxPrice);

        }
        return maxPrice;
    }

    public static float getProductVariationFromRow(Element row){

        String variation = "";
        float vtion = 0f;

        if (row.getElementsByTag("td").get(6) != null){
            variation = row.getElementsByTag("td").get(6).text();

            variation = variation.replace("%", "");

            vtion = Float.parseFloat(variation);
        }
        return vtion;
    }

    //----------------------------------------------------------------

    //------------------ CSV Code-----------------

    // https://www.callicoder.com/java-read-write-csv-file-opencsv/
    // https://www.baeldung.com/java-csv
    // https://www.baeldung.com/opencsv
    // https://www.youtube.com/watch?v=J6oXEXVNNwo
    /**
     * Save a product in a CSV File
     * @param product
     */
    public void saveProductToCSV(Product product){
        String FILE_NAME = "products.csv";
        boolean exist = new File(FILE_NAME).exists();

        //TODO: Si podes usar try() para cerrar los resources // LISTO
        //https://picodotdev.github.io/blog-bitix/2018/04/la-sentencia-try-with-resources-de-java/
        try(CSVWriter outputCsv = new CSVWriter(new FileWriter(FILE_NAME, true), ',')) { // de esta manera, el recurso outputcsv se cierra solo sin tener que llamar de forma explicita a la funcion cloe

            if (!exist){ // To add a header to the CSV File
                //https://stackoverflow.com/questions/3413586/string-to-string-array-conversion-in-java
                String[] header = new String[] {"name", "category", "metrics", "priceInPesos","priceInUsd", "priceInEur", "yesterdayMaxPrice", "variation"};
                outputCsv.writeNext(header);
            }


            String[] content = productToStringArray(product);

            outputCsv.writeNext(content);

            //outputCsv.close(); // not necessary with the use of try-with-resources

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Saves a List of Products in a CSV File
     * @param products
     */
    public void saveListToCSV (List<Product> products){
        String FILE_NAME = "products.csv";
        boolean exist = new File(FILE_NAME).exists();

        try (CSVWriter outputCsv = new CSVWriter(new FileWriter(FILE_NAME, true), ',')) {


            if (!exist){
                //https://stackoverflow.com/questions/3413586/string-to-string-array-conversion-in-java
                String[] header = new String[] {"name", "category", "metrics", "priceInPesos","priceInUsd", "priceInEur", "yesterdayMaxPrice", "variation"};
                outputCsv.writeNext(header);
            }


            List<String[]> content = listToStringArray(products);

            outputCsv.writeAll(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAllFromCSV(){
        // COMPLETE IN THE FUTURE
        return new ArrayList<>();
    }

    /**
     * Receives a product and transform it into a String[]
     * @param product
     * @return
     */
    public String[] productToStringArray(Product product){
        String name = product.getName();
        String category = product.getCategory();
        String metrics = product.getMetrics();
        String priceInPesos = String.valueOf(product.getPriceInPesos());
        String priceInUsd = String.valueOf(product.getPriceInUsd());
        String priceInEur = String.valueOf(product.getPriceInEur());
        String yesterdayMaxPrice = String.valueOf(product.getYesterdayMaxPrice());
        String variation = String.valueOf(product.getVariation());

        //example of a String array : {"allan", "maduro", "123"}
        String[] content = new String[] {name, category, metrics, priceInPesos, priceInUsd, priceInEur, yesterdayMaxPrice, variation};

        return content;
    }

    /**
     * Receives a List of products and transforms it into a List of String[], so it can be saved in a CSV file
     * @param products
     * @return
     */
    public List<String[]> listToStringArray(List<Product> products){
        List<String[]> list = new ArrayList<String[]>();
        for (Product p : products){
            String[] content = productToStringArray(p);
            list.add(content);
        }
        return list;
    }

}
