package com.bodybuilder.exercise;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * Servlet implementation class ProductBrowser
 */
@WebServlet("/ProductBrowser")
public class ProductBrowser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String htmlStart = "<HTML><HEAD><TITLE>Bodybuilding.com - Jaime Zaragoza Interview Exercise</TITLE></HEAD><BODY>";
	private static final String htmlEnd = "</BODY></HTML>";
	private static final String mainApi = "http://api.bodybuilding.com/api-proxy/commerce/products";
	private static final String brandApi = "http://api.bodybuilding.com/api-proxy/commerce/brand/%s";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProductBrowser() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		JsonObject jsonObject = getJsonObjectFromURLApi(mainApi);
	    JsonArray productsJson = jsonObject.getAsJsonArray("data");
	    List<Entry> availableProducts = new ArrayList<Entry>();
	    Gson myGson = new Gson();
	    String table = "<table border='1'>"
	    		+ "<tr>"
	    		+ "<th>#</th>"
	    		+ "<th>Product Image</th>"
	    		+ "<th>Brand</th>"
	    		+ "<th>Product Name</th>"
	    		+ "<th>Brief Description</th>"
	    		+ "<th>Quantity Sold</th>"
	    		+ "</tr>";
	    for ( JsonElement productElement : productsJson){
		      Entry entry = myGson.fromJson(productElement, Entry.class);
		      if (!entry.isDiscontinued()){
		    	availableProducts.add(entry);
		      }
	    }
	    table = createTable(table, availableProducts);

 
		out.println (htmlStart + table + htmlEnd);
	}
	
	private String createTable (String table, List<Entry> list) throws IOException {
		int n = 0;
		
		Collections.sort(list, new Comparator<Entry>() {
			
			@Override
			public int compare (Entry e1, Entry e2) {
				return (e1.getSoldInLast30Days() > e2.getSoldInLast30Days()) ? 1 : -1;
			}
		});
		
		for (Entry entry : list){
		String nextRow = "";
		JsonObject brandObject = getJsonObjectFromURLApi(String.format(
				brandApi, entry.getBrandId()));
		JsonArray brandArray = brandObject.getAsJsonArray("data");
		String brandUrl = "";
		if (brandArray.size() > 0) {
			JsonElement element = brandArray.get(0).getAsJsonObject()
					.get("brandUrl");
			brandUrl = element.getAsString();
		}
		nextRow += "<td>" + entry.getImageUrl() + "</td> " + "<td>"
				+ entry.getBrandUrl(brandUrl) + "</td>" + "<td>"
				+ entry.getproductUrl() + "</td>" + "<td>"
				+ entry.getBriefDescription() + "</td>" + "<td>"
				+ entry.getSoldInLast30Days() + "</td>";
		table += "<tr><td>" + (n++) + "</td>" + nextRow + "</tr>";
      }
		table += "</table>";
		
		return table;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	private JsonObject getJsonObjectFromURLApi (String urlApi) throws IOException{
		URLConnection urlConnection =  new URL(urlApi).openConnection();
		urlConnection.connect();
		JsonReader reader = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));
	    JsonParser parser = new JsonParser();
	    JsonElement rootElement = parser.parse(reader);
	    return rootElement.getAsJsonObject();
	}
	
	private class Entry {
		String white70PxImgUrl;
		String brandName;
		String brandId;
		String name;
		String productUrl;
		String briefDescription;
		String numberSoldInLast30Days;
		String discontinued;
		
		public String getBrandId (){
			return brandId;
		}
		
		public boolean isDiscontinued (){
			return Boolean.parseBoolean(discontinued);
		}
		
		public String getImageUrl () {
			return "<img src=http://store.bbcomcdn.com" + white70PxImgUrl + ">";
		}
		
		public String getBrandUrl (String brandSuffix){
			if (brandName == null) {
				return "No Brand";
			}
			if (brandSuffix == null){
				return brandName;
			} 
			return "<a href=\"http://www.bodybuilding.com/store" + brandSuffix + "\">" + brandName + "</a>"; 
		}
		
		public String getproductUrl (){
			if (productUrl == null){
				return name;
			}
			return "<a href=\"http://www.bodybuilding.com/store" + productUrl + "\">" + name + "</a>";
		}
		
		public String getBriefDescription (){
			if (briefDescription == null){
				return "No Description";
			}
			return briefDescription;
		}
		
		public int getSoldInLast30Days (){
			return Integer.parseInt(numberSoldInLast30Days);
		}
		
		
	}

}
