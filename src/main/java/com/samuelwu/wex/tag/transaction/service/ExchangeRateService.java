package com.samuelwu.wex.tag.transaction.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.samuelwu.wex.tag.transaction.model.CurrencyData;

import reactor.core.publisher.Mono;

@Service
public class ExchangeRateService {
	
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final String baseUrl = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
	// get all the currency code
	private static final String currencyQueryUrl = "?fields=country_currency_desc&filter=record_date:gte:{data}&page[size]=350";
	// get the exchange rate for a currency
	private static final String exchangeQueryUrl = "?fields=country_currency_desc,exchange_rate,record_date&filter=country_currency_desc:eq:{currency},record_date:gte:{startDate},record_date:lte:{endDate}&sort=-record_date";
	private WebClient webClient = WebClient.create(baseUrl);
	// cache the valid currency codes
	private List<String> validCurrencies;		

	public List<String> getValidCurrencies() {
		
		if (validCurrencies != null)
			return validCurrencies;
		var date  = LocalDate.now().minusYears(1).format(DATE_FORMAT);

		var responseSpec = webClient.get().uri(currencyQueryUrl, date).retrieve();
		
		Mono<CurrencyData> responseBody = responseSpec.bodyToMono(CurrencyData.class);
		validCurrencies = responseBody.block().getCurrencies();
		return validCurrencies;
	}

	public Map<String, String> getExchangeRate(String currency, LocalDate date) {
		
		var startYear = date.minusMonths(6).format(DATE_FORMAT);
		var endYear = date.format(DATE_FORMAT);

		var responseSpec = webClient.get().uri(exchangeQueryUrl, currency, startYear, endYear).retrieve();
		
		Mono<CurrencyData> responseBody = responseSpec.bodyToMono(CurrencyData.class);
		var exchangeRates = responseBody.block().getData();
		if (exchangeRates == null || exchangeRates.isEmpty()) {
			return null;
		}
		// return latest exchange rate
		return exchangeRates.get(0);
	}

}
