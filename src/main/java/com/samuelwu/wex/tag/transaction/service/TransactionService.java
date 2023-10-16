package com.samuelwu.wex.tag.transaction.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.samuelwu.wex.tag.transaction.model.CurrencyData;
import com.samuelwu.wex.tag.transaction.model.Transaction;
import com.samuelwu.wex.tag.transaction.repository.TransactionRepository;

@Service
public class TransactionService {
	@Value ("${transaction.page.size:50}")
	private int pageLimit;

	@Autowired
	private TransactionRepository transactionRepository;
	
	@Autowired
	private ExchangeRateService exchangeRateService;
	
	private DecimalFormat decimalFormat = new DecimalFormat("#.##");

	//get all transactions if transaction date is not provided
	public List<Transaction> getAllTransactions(LocalDate date) {
		var transactions = new ArrayList<Transaction>();
		PageRequest pageSize = PageRequest.of(0, pageLimit);
		if (date == null)
			transactionRepository.findAll(pageSize).forEach(transactions::add);
		else
			transactionRepository.findByDate(date, pageSize).forEach(transactions::add);
		return transactions;

	}

	//get the transaction by its Id and convert its amount with the exchange rate 
	public Map<String, Object> getTransactionById(long id, String currency) {
		var transactionOptional = transactionRepository.findById(id);
		if (transactionOptional.isEmpty()) {
			return null;
		}
		var transaction = transactionOptional.get();
		// if the currency code is not provide, keep it as US dollar
		Map<String, String> exchangeRateData = getExchangeRate(currency, transaction);
		if (exchangeRateData == null) {
			return new HashMap<String, Object>();
		}
				
		return createTransactionData(transaction, exchangeRateData);
	}


	public Transaction createTransaction(LocalDate date, String description, float amount) {
		// round up
		amount = Float.parseFloat(decimalFormat.format(amount));
		return transactionRepository.save(new Transaction(date, description, amount));
	}

	private Map<String, String> getExchangeRate(String currency, Transaction transaction) {
		var date = transaction.getDate();
		Map<String, String> exchangeRateData;
		if (currency == null) {
			exchangeRateData = new HashMap<>();
			exchangeRateData.put(CurrencyData.COUNTRY_CURRENCY_DESC, "U.S.-Dollar");
			exchangeRateData.put(CurrencyData.EXCHANGE_RATE, "1");
			exchangeRateData.put(CurrencyData.RECORD_DATE, date.format(ExchangeRateService.DATE_FORMAT));
		} else {
			 exchangeRateData= exchangeRateService.getExchangeRate(currency, date);
		}
		return exchangeRateData;
	}

	private Map<String, Object> createTransactionData(Transaction transaction, Map<String, String> exchangeRateData) {
		Map<String, Object> transactionData = new LinkedHashMap<>();
		// transaction data
		transactionData.put("transaction_id", transaction.getId());
		transactionData.put("description", transaction.getDescription());
		transactionData.put("original_amount", transaction.getAmount());
		transactionData.put("transaction_date", transaction.getDate().format(ExchangeRateService.DATE_FORMAT));

		//foreign exchange
		var rate = Float.parseFloat(exchangeRateData.get(CurrencyData.EXCHANGE_RATE));
		transactionData.put("currency_amount", Float.parseFloat(decimalFormat.format(transaction.getAmount()*rate)));
		transactionData.put("curreny", exchangeRateData.get(CurrencyData.COUNTRY_CURRENCY_DESC));
		transactionData.put("exchange_rate", Float.valueOf(exchangeRateData.get(CurrencyData.EXCHANGE_RATE)));
		transactionData.put("rate_date", exchangeRateData.get(CurrencyData.RECORD_DATE));
		return transactionData;
	}
}
