package com.samuelwu.wex.tag.transaction.model;

import java.util.*;

public class CurrencyData {

	public static final String RECORD_DATE = "record_date";
	public static final String EXCHANGE_RATE = "exchange_rate";
	public static final String COUNTRY_CURRENCY_DESC = "country_currency_desc";
	
	private List<Map<String, String>> data;

	public List<Map<String, String>> getData() {
		return data;
	}
	
	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}
	
	public List<String> getCurrencies() {
		var currencies = new ArrayList<String>();
		for (var currency: data) {
			currencies.add(currency.get(COUNTRY_CURRENCY_DESC));
		}
		return currencies;
	}
	
}

