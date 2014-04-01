package com.store.cart;

import java.util.HashMap;

public class DBStub {
	private HashMap<String, ProductItem> database;
	private HashMap<String, DetailsItem> details;
	
	public DBStub() {
		database = new HashMap<String, ProductItem>(100);
		details = new HashMap<String, DetailsItem>(100);
		
		database.put("7891024123140", new ProductItem(
				"Colgate MW-MicroCrist", Float.valueOf((float) 5.89)));
		details.put("7891024123140", new DetailsItem(
				"Pasta do zębów Colgate MaxWhite Micro Cristal 125g"));

		database.put("3583788998921", new ProductItem(
				"Licznik rowerowy", Float.valueOf((float) 25.99)));
		details.put("3583788998921", new DetailsItem(
				"Licznik rowerowy najlepszy naświecie. Tylko u nas"));
		
		database.put("5907806525148", new ProductItem(
				"Koperta dla premiera", Float.valueOf((float) 0.98)));
		details.put("5907806525148", new DetailsItem(
				"Koperta z zawartością, neich ze kupi gość coś ładnego, niech wyjedzie w na Bahama i nigdy nie wraca"));

		database.put("5904134931719", new ProductItem(
				"Przykładny produkt", Float.valueOf((float) 459.99)));
		details.put("5904134931719", new DetailsItem(
				"Narzędzie do wszystkiego: prania sprzątania, gotowania, odpoczywania, ćwiczenia. Jedyne co musisz zrobić to kupić ten produkt"));
		
		database.put("5904134931689", new ProductItem(
				"Kukurydza na patyku", Float.valueOf((float) 3)));
		details.put("5904134931689", new DetailsItem(
				"patyk za zł + kukurydza fajna i przejrzysta"));
		
		database.put("5904134931726", new ProductItem(
				"Wagon H0 towarowy", Float.valueOf((float) 37)));
		details.put("5904134931726", new DetailsItem(
				"Model do sklejania wagonu towarowego typu F z kabiną hamowniczego. Skala H0 - 1:87."));
		
		database.put("5903621062622", new ProductItem(
				"LM", Float.valueOf((float) 37)));
		details.put("5903621062622", new DetailsItem(
				"Dokarmianie raka"));
	}
	
	public DetailsItem getDetailsById(String id) {
		DetailsItem ret = null;
		for (HashMap.Entry<String, DetailsItem> item: details.entrySet()) {
			if (item.getKey().equals(id)) {
				ret = item.getValue();
				break;
			}
		}
		
		return ret;
	}
	
	public ProductItem getProductById(String id) {
		ProductItem ret = null;
		for (HashMap.Entry<String, ProductItem> item: database.entrySet()) {
			if (item.getKey().equals(id) ) {
				ret = item.getValue();
				break;
			}
		}
		
		return ret;
	}
}
