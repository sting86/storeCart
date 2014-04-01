package com.store.cart;

public class ProductItem {
	private String productName;
	private Float productPrice;
	//TODO: weight support

	public ProductItem(String name, Float price) {
		productName = name;
		productPrice = price;
	}
	
	public Float getProductPrice() {
		return productPrice;
	}
	
	public String getProductName() {
		return productName;
	}
}
