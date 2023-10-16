package com.samuelwu.wex.tag.transaction.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.samuelwu.wex.tag.transaction.model.CurrencyData;
import com.samuelwu.wex.tag.transaction.model.Transaction;
import com.samuelwu.wex.tag.transaction.repository.TransactionRepository;
import com.samuelwu.wex.tag.transaction.service.ExchangeRateService;


@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private TransactionRepository transactionRepository;
	
	@MockBean
	private ExchangeRateService exchangeRateService;
	
	@Test
	public void getAllTransactions_empty() throws Exception {
		Mockito.when(transactionRepository.findAll(PageRequest.of(0, 50))).thenReturn(new PageImpl<Transaction>(new ArrayList<Transaction>()));
		getTransactions_empty(null);
	}

	@Test
	public void getTransactionsByDate_empty() throws Exception {
		var date = LocalDate.now();
		Mockito.when(transactionRepository.findByDate(date, PageRequest.of(0, 50))).thenReturn(new ArrayList<Transaction>());
		getTransactions_empty(date);
	}
	
	@Test
	public void getAllTransactions_common() throws Exception {
		var transactions = getSampleTransactions();
		Mockito.when(transactionRepository.findAll( PageRequest.of(0, 50))).thenReturn(new PageImpl<Transaction>(transactions));
		getTransactions_common(null, transactions);
	}
	
	@Test
	public void getTransactionsByDate_common() throws Exception {
		var date = LocalDate.now();
		var transactions = getSampleTransactions();
		Mockito.when(transactionRepository.findByDate(date, PageRequest.of(0, 50))).thenReturn(transactions);
		getTransactions_common(date, transactions);
	}
	
	@Test
	public void getAllTransactions_exception() throws Exception {
		Mockito.when(transactionRepository.findAll()).thenThrow(new RuntimeException());

		mvc.perform(MockMvcRequestBuilders.get("/api/transactions").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
				.andExpect(handler().handlerType(TransactionController.class))
				.andExpect(handler().methodName("getAllTransactions"))
				.andExpect(content().string("System error. Contact administrator."));
	}
	
	@Test
	public void getTransactionById_common() throws Exception {
		var currency = "Canada-Dollar";
		var exchangeRateData = new HashMap<String, String>();
		exchangeRateData.put(CurrencyData.COUNTRY_CURRENCY_DESC, currency);
		exchangeRateData.put(CurrencyData.EXCHANGE_RATE, "1.3");
		exchangeRateData.put(CurrencyData.RECORD_DATE, "2023-06-30");
		var expectedResult = "{\"transaction_id\":123,\"description\":\"description\",\"original_amount\":123.12,\"transaction_date\":\"2023-10-01\",\"currency_amount\":160.06,\"curreny\":\"Canada-Dollar\",\"exchange_rate\":1.3,\"rate_date\":\"2023-06-30\"}";
		Mockito.when(exchangeRateService.getExchangeRate(currency, LocalDate.of(2023, 10, 1))).thenReturn(exchangeRateData);
		getTransactionById(currency, expectedResult);
	}
	
	@Test
	public void getTransactionById_noCurrency() throws Exception {
		var expectedResult = "{\"transaction_id\":123,\"description\":\"description\",\"original_amount\":123.12,\"transaction_date\":\"2023-10-01\",\"currency_amount\":123.12,\"curreny\":\"U.S.-Dollar\",\"exchange_rate\":1,\"rate_date\":\"2023-10-01\"}";
		getTransactionById(null, expectedResult);
	}

	@Test
	public void getTransactionById_noExchangeRate() throws Exception {
		var currency = "Canada-Dollar";
		var date = LocalDate.of(2023, 10, 1);
		var transaction = new Transaction(date, "description", (float)123.12);
		transaction.setId((long)123);
		Mockito.when(transactionRepository.findById((long) 123)).thenReturn(Optional.of(transaction));
		Mockito.when(exchangeRateService.getExchangeRate(currency, date)).thenReturn(null);
		
		var builder = createGetTransactionByIdRequest(currency);
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("getTransactionById"))
		.andExpect(content().string("Failed to find the exchange rate"));
	}

	@Test
	public void getTransactionById_noTransaction() throws Exception {

		Mockito.when(transactionRepository.findById((long) 123)).thenReturn(Optional.empty());
		
		var builder = createGetTransactionByIdRequest(null);
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.NOT_FOUND.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("getTransactionById"))
		.andExpect(content().string("Failed to find the transaction ID"));
	}

	@Test
	public void getTransactionById_exception() throws Exception {

		Mockito.when(transactionRepository.findById((long) 123)).thenThrow(new RuntimeException());
		
		var builder = createGetTransactionByIdRequest(null);
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("getTransactionById"))
		.andExpect(content().string("System error. Contact administrator."));
	}
	
	@Test
	public void createTransaction_common() throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var transaction = new Transaction(date, "description", (float)123.128);
		var transactionRound = new Transaction(date, "description", (float)123.13);

		Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenReturn(transactionRound);
		

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		var builder = createCreateTransactionRequest(objectMapper.writeValueAsString(transaction));
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.CREATED.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("createTransaction"));
		//verify the saved value
		ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
		Mockito.verify(transactionRepository).save(transactionCaptor.capture());
		//
		Transaction transactionSaved = transactionCaptor.getValue();
		assertEquals(transactionRound.getAmount(), transactionSaved.getAmount());
		assertEquals(transactionRound.getDate(), transactionSaved.getDate());
		assertEquals(transactionRound.getDescription(), transactionSaved.getDescription());
		assertEquals(transactionRound.toString(), transactionSaved.toString());
	}

	@Test
	public void createTransaction_negativeAmount() throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var transaction = new Transaction(date, "description", (float)-123.128);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		var builder = createCreateTransactionRequest(objectMapper.writeValueAsString(transaction));
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("createTransaction"))
		.andExpect(content().string("[\"Transaction amount needs to be a positive number\"]"));

	}

	@Test
	public void createTransaction_longDescription() throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var description = "description";
		description += "12345678901234567890";
		description += "12345678901234567890";
		description += "12345678901234567890";
		var transaction = new Transaction(date, description, (float)123.128);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		var builder = createCreateTransactionRequest(objectMapper.writeValueAsString(transaction));
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("createTransaction"))
		.andExpect(content().string("[\"Description can't be over 50 characters\"]"));

	}

	@Test
	public void createTransaction_bothErrors() throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var description = "description";
		description += "12345678901234567890";
		description += "12345678901234567890";
		description += "12345678901234567890";
		var transaction = new Transaction(date, description, (float)-123.128);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		var builder = createCreateTransactionRequest(objectMapper.writeValueAsString(transaction));
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("createTransaction"))
		.andExpect(content().string("[\"Description can't be over 50 characters\",\"Transaction amount needs to be a positive number\"]"));

	}
	
	@Test
	public void createTransaction_exception() throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var transaction = new Transaction(date, "description", (float)123.128);

		Mockito.when(transactionRepository.save(Mockito.any(Transaction.class))).thenThrow(new RuntimeException());
		

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		var builder = createCreateTransactionRequest(objectMapper.writeValueAsString(transaction));
		mvc.perform(builder)
		.andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("createTransaction"))
		.andExpect(content().string("System error. Contact administrator."));
	}
	
	private void getTransactionById(String currency, String expectedResult) throws Exception {
		var date = LocalDate.of(2023, 10, 1);
		var transaction = new Transaction(date, "description", (float)123.12);
		transaction.setId((long)123);
		
		Mockito.when(transactionRepository.findById((long) 123)).thenReturn(Optional.of(transaction));
		
		var builder = createGetTransactionByIdRequest(currency);
		mvc.perform(builder)
		.andExpect(status().isOk())
		.andExpect(handler().handlerType(TransactionController.class))
		.andExpect(handler().methodName("getTransactionById"))
		.andExpect(content().json(expectedResult));
	}

	private MockHttpServletRequestBuilder createGetTransactionByIdRequest(String currency) {
		var builder = MockMvcRequestBuilders.get("/api/transactions/123")
		.accept(MediaType.APPLICATION_JSON);
		if (currency != null) {
			builder.queryParam("currency", currency);
		}
		return builder;
	}

	private MockHttpServletRequestBuilder createCreateTransactionRequest(String content) {
		var builder = MockMvcRequestBuilders.post("/api/transactions")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.content(content);
		return builder;
	}
	
	private ArrayList<Transaction> getSampleTransactions() {
		var transactions = new ArrayList<Transaction>();
		var date = LocalDate.now();
		var lastMonth = date.minusMonths(1);
		transactions.add(new Transaction(date, "description", (float)123.12));
		transactions.add(new Transaction(lastMonth, "description", (float)100.45));
		return transactions;
	}

	private void getTransactions_empty(LocalDate date) throws Exception {
		mvc.perform(createGetTransactionsRequestBuilder(date))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()))
				.andExpect(handler().handlerType(TransactionController.class))
				.andExpect(handler().methodName("getAllTransactions"))
				.andExpect(content().string(""));
	}

	private MockHttpServletRequestBuilder createGetTransactionsRequestBuilder(LocalDate date) {
		var builder = MockMvcRequestBuilders.get("/api/transactions")
		.accept(MediaType.APPLICATION_JSON);
		if (date != null) {
			builder.queryParam("date", date.format(ExchangeRateService.DATE_FORMAT));
		}
		return builder;
	}

	private void getTransactions_common(LocalDate date, List<Transaction> transactions) throws Exception {
		var result = mvc.perform(createGetTransactionsRequestBuilder(date))
				.andExpect(status().isOk())
				.andExpect(handler().handlerType(TransactionController.class))
				.andExpect(handler().methodName("getAllTransactions")).andReturn();
		
		String json = result.getResponse().getContentAsString();
		var type = new TypeReference<List<Transaction>>() {};
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		List<Transaction> responseContent = objectMapper.readValue(json, type);
		
		assertEquals(transactions.size(), responseContent.size());
		for (int i = 0; i < transactions.size(); i++) {
			assertEquals(transactions.get(i).getAmount(), responseContent.get(i).getAmount());
			assertEquals(transactions.get(i).getDate(), responseContent.get(i).getDate());
			assertEquals(transactions.get(i).getDescription(), responseContent.get(i).getDescription());
			assertEquals(transactions.get(i).getId(), responseContent.get(i).getId());
		}
	}
}