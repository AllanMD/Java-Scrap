package com.avalith.Productsscraping;

import com.avalith.Productsscraping.model.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ProductsScrapingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductsScrapingApplication.class, args);
		Elements lists = getListsFromPage("https://www.lamayorista.com.co/");
		List<Product> products = getAllProductsFromLists(lists);
		System.out.println(products);
		System.out.println("Cantidad de productos:" + products.size());
	}

	/**
	 * Gets all the <li></li> elements that contains the products data of the url
	 * @param url
	 * @return
	 */
	public static Elements getListsFromPage(String url){
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
			e.printStackTrace();
		}
		return lists;
	}

	/**
	 *Gets all the products of an individual <li></li> element
	 * @param list
	 * @return
	 */
	public static List<Product> getProductsFromList(Element list){
		List<Product> products = new ArrayList<Product>();
		if (list != null){
			Product p = new Product();
			String category = list.selectFirst("a").text();
			p.setCategory(category); // all the products in each list belongs to the same category
			Elements rows = list.getElementsByClass("gradeA");
			for (Element row: rows) {
				String name = getProductNameFromRow(row);
				String metrics = getProductMetricsFromRow(row);
				Float priceInPesos = getPricePesosFromRow(row);
				Float priceInUsd = getPriceUsdFromRow(row);
				Float priceInEur = getPriceEurFromRow(row);
				Float yesterdayMaxPrice = getYesterdayMaxPriceFromRow(row);
				Float variation = getProductVariationFromRow(row);

				p.setName(name);
				p.setMetrics(metrics);
				p.setPriceInPesos(priceInPesos);
				p.setPriceInUsd(priceInUsd);
				p.setPriceInEur(priceInEur);
				p.setYesterdayMaxPrice(yesterdayMaxPrice);
				p.setVariation(variation);

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

	public static Float getPricePesosFromRow(Element row){

		String priceInPesos = "";
		Float price = 0f;

		if (row.getElementsByTag("td").get(2) != null){
			priceInPesos = row.getElementsByTag("td").get(2).text();
			priceInPesos = priceInPesos.substring(priceInPesos.indexOf("$")+1);
			priceInPesos = priceInPesos.replace(",",".");

			price = Float.parseFloat(priceInPesos);
		}
		return price;
	}

	public static Float getPriceUsdFromRow(Element row){

		String priceInUsd = "";
		Float price = 0f;
		if (row.getElementsByTag("td").get(3) != null){
			priceInUsd = row.getElementsByTag("td").get(3).text();
			priceInUsd = priceInUsd.substring(priceInUsd.indexOf("$")+1);
			priceInUsd = priceInUsd.replace(",",".");

			price = Float.parseFloat(priceInUsd);
		}
		return price;
	}

	public static float getPriceEurFromRow(Element row){

		String priceInEur = "";
		Float price = 0f;
		if (row.getElementsByTag("td").get(4) != null){
			priceInEur = row.getElementsByTag("td").get(4).text();

			priceInEur = priceInEur.substring(priceInEur.indexOf("€")+1);
			priceInEur = priceInEur.replace(",",".");

			price = Float.parseFloat(priceInEur);
		}
		return price;
	}

	public static Float getYesterdayMaxPriceFromRow(Element row){

		String yesterdayMaxPrice = "";
		Float maxPrice = 0f;
		if (row.getElementsByTag("td").get(5) != null){
			yesterdayMaxPrice = row.getElementsByTag("td").get(5).text();

			yesterdayMaxPrice = yesterdayMaxPrice.substring(yesterdayMaxPrice.indexOf("$")+1);
			yesterdayMaxPrice = yesterdayMaxPrice.replace(",",".");

			maxPrice = Float.parseFloat(yesterdayMaxPrice);

		}
		return maxPrice;
	}

	public static Float getProductVariationFromRow(Element row){

		String variation = "";
		Float vtion = 0f;

		if (row.getElementsByTag("td").get(6) != null){
			variation = row.getElementsByTag("td").get(6).text();

			variation = variation.substring(0, variation.indexOf("%")); // from the begining to "%"

			vtion = Float.parseFloat(variation);
		}
		return vtion;
	}


}
