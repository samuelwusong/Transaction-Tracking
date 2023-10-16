package com.samuelwu.wex.tag.transaction.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samuelwu.wex.tag.transaction.model.Transaction;
import com.samuelwu.wex.tag.transaction.service.TransactionService;



@RestController
@RequestMapping("/api")
public class TransactionController {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private static final int lengthLimit = 50;
	
	// read messages from application.properties
	@Value("${common.msgSystemError}")
	private String msgSystemError; 
	@Value("${transaction.msgInvalidTransactionAmount}")
	private String msgInvalidTransactionAmount; 
	@Value("${transaction.msgInvalidDescription}")
	private String msgInvalidDescription; 
	@Value("${transaction.msgIdNotFound}")
	private String msgIdNotFound; 
	@Value("${transaction.msgExchangeRateNotFound}")
	private String msgExchangeRateNotFound; 
	
	@Autowired
	private TransactionService transactionService;

	// get transactions from database 
	@GetMapping("/transactions")
	public ResponseEntity<Object> getAllTransactions(@RequestParam(required = false) LocalDate date) {
		try {
			var transactions = transactionService.getAllTransactions(date);

			if (transactions.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(transactions, HttpStatus.OK);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new ResponseEntity<>(msgSystemError, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// get full transaction information for a transaction Id
	@GetMapping("/transactions/{id}")
	public ResponseEntity<Object> getTransactionById(@PathVariable("id") long id, @RequestParam(required = false) String currency) {
		try {
			var transactionData = transactionService.getTransactionById(id, currency);

			if (transactionData == null) {
				return new ResponseEntity<>(msgIdNotFound, HttpStatus.NOT_FOUND);
			} else if (transactionData.isEmpty()) {
				return new ResponseEntity<>(msgExchangeRateNotFound, HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(transactionData, HttpStatus.OK);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new ResponseEntity<>(msgSystemError, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// create a new transaction 
	@PostMapping("/transactions")
	public ResponseEntity<Object> createTransaction(@RequestBody Transaction transaction) {
		var description = transaction.getDescription();
		var amount = transaction.getAmount();
		var validationErrors = validateInput(description, amount);
		if (validationErrors.size() > 0) {
			return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST); 
		}
		try {
			var transactionCreated = transactionService.createTransaction(transaction.getDate(), description, amount);
			return new ResponseEntity<>(transactionCreated, HttpStatus.CREATED);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new ResponseEntity<>(msgSystemError, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private ArrayList<String> validateInput(String description, float amount) {
		var validationErrors = new ArrayList<String>();
		// description is too long
		if (description.length() > lengthLimit) {
			validationErrors.add(msgInvalidDescription);
		}
		// amount needs to be positive
		if (amount <= 0 ) {
			validationErrors.add(msgInvalidTransactionAmount);
		}
		return validationErrors;
	}
}
