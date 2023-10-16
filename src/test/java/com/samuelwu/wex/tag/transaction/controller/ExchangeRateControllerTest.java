package com.samuelwu.wex.tag.transaction.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.samuelwu.wex.tag.transaction.service.ExchangeRateService;

@WebMvcTest(ExchangeRateController.class)
@AutoConfigureMockMvc
public class ExchangeRateControllerTest {
	@Autowired
	private MockMvc mvc;

	@MockBean
	ExchangeRateService exchangeRateService;

	@Test
	public void getAllCurrencies_success() throws Exception {
		var currencies = new ArrayList<String>();
		currencies.add("currency1");
		currencies.add("currency2");
		Mockito.when(exchangeRateService.getValidCurrencies()).thenReturn(currencies);
		mvc.perform(MockMvcRequestBuilders.get("/api/exchange")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(ExchangeRateController.class))
				.andExpect(handler().methodName("getAllCurrencies"))
				.andExpect(content().string("[\"currency1\",\"currency2\"]"));
	}

	@Test
	public void getAllCurrencies_empty() throws Exception {
		var currencies = new ArrayList<String>();
		Mockito.when(exchangeRateService.getValidCurrencies()).thenReturn(currencies);
		mvc.perform(MockMvcRequestBuilders.get("/api/exchange")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()))
				.andExpect(handler().handlerType(ExchangeRateController.class))
				.andExpect(handler().methodName("getAllCurrencies"))
				.andExpect(content().string(""));
	}

	@Test
	public void getAllCurrencies_exception() throws Exception {
		Mockito.when(exchangeRateService.getValidCurrencies()).thenThrow(new RuntimeException());
		mvc.perform(MockMvcRequestBuilders.get("/api/exchange")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.andExpect(handler().handlerType(ExchangeRateController.class))
				.andExpect(handler().methodName("getAllCurrencies"))
				.andExpect(content().string("System error. Contact administrator."));

	}
}