package com.samuelwu.wex.tag.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.samuelwu.wex.tag.transaction.model.CurrencyData;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {
	@Mock
	private WebClient webClientMock;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpecMock;

	@Mock
	private WebClient.RequestBodySpec requestBodySpecMock;

	@SuppressWarnings("rawtypes")
	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpecMock;

	@SuppressWarnings("rawtypes")
	@Mock
	private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

	@Mock
	private WebClient.ResponseSpec responseSpecMock;
	@Mock
	private Mono<CurrencyData> postResponseMock;

	private static final String baseUrl = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
	// get the exchange rate for a currency
	private static final String exchangeQueryUrl = "?fields=country_currency_desc,exchange_rate,record_date&filter=country_currency_desc:eq:{currency},record_date:gte:{startDate},record_date:lte:{endDate}&sort=-record_date";

	@InjectMocks
	private ExchangeRateService exchangeRateService;

	@Test
	public void getValidCurrencies() throws Exception {
		try (MockedStatic<WebClient> utilities = Mockito.mockStatic(WebClient.class)) {
			setupMocks(utilities);
			Mockito.when(requestHeadersUriSpecMock.uri( Mockito.anyString(), Mockito.anyString()))
			.thenReturn(requestHeadersSpecMock);

			List<Map<String, String>> currencies = getSampleCurrencies();

			CurrencyData data = new CurrencyData();
			data.setData(currencies);
			Mockito.when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<CurrencyData>>notNull()))
					.thenReturn(Mono.just(data));

			var response = exchangeRateService.getValidCurrencies();

			assertEquals(currencies.size(), response.size());
			for (int i = 0; i < currencies.size(); i++) {
				assertEquals(currencies.get(i).get(CurrencyData.COUNTRY_CURRENCY_DESC), response.get(i));
			}
			// check cached values
			assertEquals(response, exchangeRateService.getValidCurrencies());
		}
	}

	@Test
	public void getExchangeRate_success() throws Exception {
		try (MockedStatic<WebClient> utilities = Mockito.mockStatic(WebClient.class)) {
			setupMocks(utilities);
			var date = LocalDate.now();
			var startYear = date.minusMonths(6).format(ExchangeRateService.DATE_FORMAT);
			var endYear = date.format(ExchangeRateService.DATE_FORMAT);
			var currency = "currency1";
			Mockito.when(requestHeadersUriSpecMock.uri(exchangeQueryUrl, currency, startYear, endYear))
			.thenReturn(requestHeadersSpecMock);

			List<Map<String, String>> currencies = getSampleExchangeRates();

			CurrencyData data = new CurrencyData();
			data.setData(currencies);
			Mockito.when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<CurrencyData>>notNull()))
					.thenReturn(Mono.just(data));
			var response = exchangeRateService.getExchangeRate(currency, date);
			assertEquals(currencies.get(0), response);
		}
	}

	@Test
	public void getExchangeRate_empty1() throws Exception {
		testGetExchangeRate_empty(null);
	}

	@Test
	public void getExchangeRate_empty2() throws Exception {
		testGetExchangeRate_empty(new ArrayList<>());
	}

	private void testGetExchangeRate_empty(List<Map<String, String>> data) {
		try (MockedStatic<WebClient> utilities = Mockito.mockStatic(WebClient.class)) {
			setupMocks(utilities);
			var date = LocalDate.now();
			var startYear = date.minusMonths(6).format(ExchangeRateService.DATE_FORMAT);
			var endYear = date.format(ExchangeRateService.DATE_FORMAT);
			var currency = "currency1";
			Mockito.when(requestHeadersUriSpecMock.uri(exchangeQueryUrl, currency, startYear, endYear))
			.thenReturn(requestHeadersSpecMock);
			
			var currencyData = new CurrencyData();
			currencyData.setData(data);
			Mockito.when(responseSpecMock.bodyToMono(ArgumentMatchers.<Class<CurrencyData>>notNull()))
					.thenReturn(Mono.just(currencyData));
			var response = exchangeRateService.getExchangeRate(currency, date);
			assertEquals(null, response);
		}
	}
	
	private List<Map<String, String>> getSampleCurrencies() {
		List<Map<String, String>> currencies = new ArrayList<>();
		Map<String, String> currency = new HashMap<>();
		currency.put(CurrencyData.COUNTRY_CURRENCY_DESC, "currency1");
		currencies.add(currency);
		currency = new HashMap<>();
		currency.put(CurrencyData.COUNTRY_CURRENCY_DESC, "currency2");
		currencies.add(currency);
		return currencies;
	}


	private List<Map<String, String>> getSampleExchangeRates() {
		List<Map<String, String>> currencies = new ArrayList<>();
		Map<String, String> currency = new HashMap<>();
		currency.put(CurrencyData.COUNTRY_CURRENCY_DESC, "currency1");
		currency.put(CurrencyData.EXCHANGE_RATE, "1.35");
		currency.put(CurrencyData.RECORD_DATE, "1995-06-30");
		currencies.add(currency);
		currency = new HashMap<>();
		currency.put(CurrencyData.COUNTRY_CURRENCY_DESC, "currency1");
		currency.put(CurrencyData.EXCHANGE_RATE, "1.36");
		currency.put(CurrencyData.RECORD_DATE, "1995-03-30");
		currencies.add(currency);
		return currencies;
	}

	@SuppressWarnings("unchecked")
	private void setupMocks(MockedStatic<WebClient> utilities) {
		utilities.when(() -> WebClient.create(baseUrl)).thenReturn(webClientMock);

		Mockito.when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
		Mockito.when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
	}
}