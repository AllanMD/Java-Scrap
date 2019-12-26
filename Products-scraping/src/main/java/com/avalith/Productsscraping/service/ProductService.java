package com.avalith.Productsscraping.service;

import com.avalith.Productsscraping.model.Product;
import com.avalith.Productsscraping.repository.ProductRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // otra forma de inyectar dependencia, lo inyecta a private final productRepository. El "final" debe estar para indicar que ese atributo es requerido en el constructor
public class ProductService {

    //Reemplazar inyeccion de dependencias del field, por inyeccion de constructor o de setter // LISTO (?)
    private final ProductRepository productRepository;


    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void save(List<Product> products) {
        products.forEach(product -> productRepository.save(product));
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public List<Product> scrap(String url) throws IOException {

        //TODO: si podes reemplazar por val, y nombrar con ducktyping // LISTO
        // http://blog.chuidiang.org/2013/01/09/duck-typing/
        val listsElements = getListsFromPage("https://www.lamayorista.com.co/"); // CHANGE LATER TO URL // https://www.genbeta.com/desarrollo/entendiendo-la-inmutabilidad-que-es-para-que-sirve-y-como-usarla
        // val --> if the object wont change in the future
        // var ---> if the object may change in the future
        List<Product> products = getAllProductsFromLists(listsElements.get());

        System.out.println("Cantidad de productos:" + products.size());

        save(products); // persistimos los datos en mongodb
        saveListToCSV(products); // persistimos los datos en archivo CSV

        return products;
    }


    /**
     * Gets all the <li></li> elements that contains the products data of the url
     *
     * @param url
     * @return
     */
    public Optional<Elements> getListsFromPage(String url) throws IOException {
        //TODO: trata de evitar null // usar optional //LISTO (?)

        //Elements lists = null; // PREGUNTAR
        Document doc = Jsoup.connect(url) // https://github.com/jhy/jsoup/issues/287 , to solve the problem that certain times jsoup didnt bring all the HTML
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .maxBodySize(0) // jsoup has a default maxBodySize and thats why i received an uncomplete body
                .timeout(600000)
                .get();

        //TODO // mapearlo en doc
        Optional<Elements> lists = Optional.ofNullable(doc.getElementById("Tap").getElementsByTag("li")); // "Tap" contains the li elements
        return lists;
    }

    /**
     * Gets all the products of an individual <li></li> element
     *
     * @param list
     * @return
     */
    public List<Product> getProductsFromList(Optional<Element> list) {
        String category = list
                .map(element -> element.selectFirst("a"))
                .map(Element::text)
                .orElse("");

        //String category = list.selectFirst("a").text(); // all the products in each list belongs to the same category
        Optional<Elements> rows = list
                .map(element -> element.getElementsByClass("gradeA"));

        val productList = rows.stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(singleRow -> getProductFromRow(singleRow, category))
                //.filter(product -> product.getCategory().equalsIgnoreCase("frutas")) // only for example, (delete)
                .collect(Collectors.toList());

        return productList;
    }

    /**
     * gets all the product info from a row and returns it in a Product obj
     *
     * @param row
     * @return
     */
    public Product getProductFromRow(Element row, String category) {
        String name = getProductNameFromRow(row);
        String metrics = getProductMetricsFromRow(row);
        float priceInPesos = getPricePesosFromRow(row);
        float priceInUsd = getPriceUsdFromRow(row);
        float priceInEur = getPriceEurFromRow(row);
        float yesterdayMaxPrice = getYesterdayMaxPriceFromRow(row);
        float variation = getProductVariationFromRow(row);

        return Product.builder()
                .category(category) // all the products in each list belongs to the same category
                .name(name)
                .metrics(metrics)
                .priceInPesos(priceInPesos)
                .priceInUsd(priceInUsd)
                .priceInEur(priceInEur)
                .yesterdayMaxPrice(yesterdayMaxPrice)
                .variation(variation)
                .build();
    }

    /**
     * Get all the products of all <li></li> elements
     *
     * @param lists
     * @return
     */
    public List<Product> getAllProductsFromLists(Elements lists) {
        /*List<Product> products = new ArrayList<Product>();
        List<Product> array = new ArrayList<>();
        for (Element list : lists) {
            array = getProductsFromList(Optional.ofNullable(list));
            for (Product p : array) {
                products.add(p); // we have to combine all the products arrays in one array
            }
        }*/ // es lo mismo que lo de abajo

        val products = lists.stream()
                .map(element -> getProductsFromList(Optional.ofNullable(element))) // obtenemos la lista de products
                .flatMap(Collection::stream) // bajamos un nivel, y obtenemos los productos directamente
                .collect(Collectors.toList()); // los productos los coleccionamos en una lista
        return products;
    }

    // ------- GETTERS FOR SINGLE ROW OF THE <li> ELEMENT ------
    public String getProductNameFromRow(Element row) {

        //Optional<Element> nameElementOptional = Optional.of(row.getElementsByTag("td").get(0));
        //String name = Optional.of(nameElementOp.get().text()).orElse("");
        
        String name = Optional.ofNullable(row) // in the first position of the td, me have the name
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(Elements::first)
                .map(Element::text)//text(): brings only the text inside the tag
                .orElse("");
        // a more simplified way to do it

        return name;
    }

    public String getProductMetricsFromRow(Element row) {
        Optional<Element> metricElementOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(1));

        // .map() ---> para obtener datos con determinado criterio
        //.filter() ---> para filtrar esos datos en base a una condicion que se cumpla (true o false)

        String metrics = metricElementOptional
                .map(Element::text)
                .orElse("");

        return metrics;
    }

    public float getPricePesosFromRow(Element row) {
        Optional<Element> priceInPesosOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(2));

        float price = priceInPesosOptional
                .map(Element::text)
                .map(priceString -> priceString.replace("$", "")) // so we can parse to float, else if it haves a "$" in the string, it will throw a parse to float exception
                .map(priceString -> priceString.replace(",", "."))
                .map(Float::parseFloat)
                .orElse(0f);

        /*
        String priceInPesos = "";
        float price = 0f;

        if (row.getElementsByTag("td").get(2) != null){
            priceInPesos = row.getElementsByTag("td").get(2).text();

            priceInPesos = priceInPesos.replace("$", "");
            priceInPesos = priceInPesos.replace(",",".");

            price = Float.parseFloat(priceInPesos);
        }*/
        return price;
    }

    public float getPriceUsdFromRow(Element row) {
        Optional<Element> priceInUsdOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(3));

        float price = priceInUsdOptional
                .map(Element::text)
                .map(priceString -> priceString.replace("US$", ""))
                .map(priceString -> priceString.replace(",", "."))
                .map(Float::parseFloat)
                .orElse(0f);

        return price;
    }

    public float getPriceEurFromRow(Element row) {
        Optional<Element> priceInEurOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(4));

        float price = priceInEurOptional
                .map(Element::text)
                .map(priceString -> priceString.replace("€", ""))
                .map(priceString -> priceString.replace(",", "."))
                .map(Float::parseFloat)
                .orElse(0f);

        return price;
    }

    public float getYesterdayMaxPriceFromRow(Element row) {
        Optional<Element> maxPriceOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(5));

        float maxPrice = maxPriceOptional
                .map(Element::text)
                .map(maxPriceString -> maxPriceString.replace("$", ""))
                .map(maxPriceString -> maxPriceString.replace(",", "."))
                .map(Float::parseFloat)
                .orElse(0f);

        return maxPrice;
    }

    public float getProductVariationFromRow(Element row) {
        Optional<Element> variationOptional = Optional.ofNullable(row)
                .map(rowNonNull -> rowNonNull.getElementsByTag("td"))
                .map(elements -> elements.get(6));

        float variation = variationOptional
                .map(Element::text)
                .map(variationString -> variationString.replace("%", ""))
                .map(Float::parseFloat)
                .orElse(0f);
        return variation;
    }

    //----------------------------------------------------------------

    //------------------ CSV Code-----------------

    // https://www.callicoder.com/java-read-write-csv-file-opencsv/
    // https://www.baeldung.com/java-csv
    // https://www.baeldung.com/opencsv
    // https://www.youtube.com/watch?v=J6oXEXVNNwo

    /**
     * Save a product in a CSV File
     *
     * @param product
     */
    public void saveProductToCSV(Product product) {
        String FILE_NAME = "products.csv";
        boolean exist = new File(FILE_NAME).exists();

        //https://picodotdev.github.io/blog-bitix/2018/04/la-sentencia-try-with-resources-de-java/
        try (CSVWriter outputCsv = new CSVWriter(new FileWriter(FILE_NAME, true), ',')) { // de esta manera, el recurso outputcsv se cierra solo sin tener que llamar de forma explicita a la funcion cloe

            if (!exist) { // To add a header to the CSV File
                //https://stackoverflow.com/questions/3413586/string-to-string-array-conversion-in-java
                String[] header = new String[]{"name", "category", "metrics", "priceInPesos", "priceInUsd", "priceInEur", "yesterdayMaxPrice", "variation"};
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
     *
     * @param products
     */
    public void saveListToCSV(List<Product> products) {
        String FILE_NAME = "products.csv";
        boolean exist = new File(FILE_NAME).exists();

        try (CSVWriter outputCsv = new CSVWriter(new FileWriter(FILE_NAME, true), ',')) {


            if (!exist) {
                //https://stackoverflow.com/questions/3413586/string-to-string-array-conversion-in-java
                String[] header = new String[]{"name", "category", "metrics", "priceInPesos", "priceInUsd", "priceInEur", "yesterdayMaxPrice", "variation"};
                outputCsv.writeNext(header);
            }


            List<String[]> content = listToStringArray(products);

            outputCsv.writeAll(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAllFromCSV() {
        // COMPLETE IN THE FUTURE
        return new ArrayList<>();
    }

    /**
     * Receives a product and converts it into a String[]
     *
     * @param product
     * @return
     */
    public String[] productToStringArray(Product product) {
        String name = product.getName();
        String category = product.getCategory();
        String metrics = product.getMetrics();
        String priceInPesos = String.valueOf(product.getPriceInPesos());
        String priceInUsd = String.valueOf(product.getPriceInUsd());
        String priceInEur = String.valueOf(product.getPriceInEur());
        String yesterdayMaxPrice = String.valueOf(product.getYesterdayMaxPrice());
        String variation = String.valueOf(product.getVariation());

        //example of a String array : {"allan", "maduro", "123"}
        String[] content = new String[]{name, category, metrics, priceInPesos, priceInUsd, priceInEur, yesterdayMaxPrice, variation};

        return content;
    }

    /**
     * Receives a List of products and transforms it into a List of String[], so it can be saved in a CSV file
     *
     * @param products
     * @return
     */
    public List<String[]> listToStringArray(List<Product> products) {
        List<String[]> list = new ArrayList<String[]>();
        for (Product p : products) {
            String[] content = productToStringArray(p);
            list.add(content);
        }
        return list;
    }

}

/*
Optional.of(objeto) // para convertir un objeto en optional
Optional<Tipo objeto> nombre objetoi
*/