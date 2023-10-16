package com.samuelwu.wex.tag.transaction.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samuelwu.wex.tag.transaction.service.ExchangeRateService;

@RestController
@RequestMapping("/api")
public class ExchangeRateController {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	// read messages from application.properties
	@Value("${common.msgSystemError}")
	private String msgSystemError; 
	
	@Autowired
	private ExchangeRateService exchangeRateService;

	// collect all available currency names
	@GetMapping("/exchange")
	public ResponseEntity<Object> getAllCurrencies() {
		try {
			var currencies = exchangeRateService.getValidCurrencies();

			if (currencies.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(currencies, HttpStatus.OK);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			return new ResponseEntity<>(msgSystemError, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
