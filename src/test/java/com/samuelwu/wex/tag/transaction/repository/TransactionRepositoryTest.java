package com.samuelwu.wex.tag.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import com.samuelwu.wex.tag.transaction.model.Transaction;

@DataJpaTest
public class TransactionRepositoryTest {
	@Autowired
	private TransactionRepository transactionRepository;
	
	@Test
	void getTransactionByTest() {
		var date = LocalDate.now();
		var transactionsRetrieved = transactionRepository.findByDate(date, PageRequest.of(0, 50));
		assertEquals (0, transactionsRetrieved.size());
		var transaction = new Transaction(date, "Remarks", (float)3.33);
		
		// create new records
		transactionRepository.save(transaction);
		transaction = new Transaction(date.minusDays(3), transaction.getDescription(), transaction.getAmount());
		transactionRepository.save(transaction);
		
		// get all records
		transactionsRetrieved = transactionRepository.findAll();
		assertEquals (2, transactionsRetrieved.size());
		
		// get by date
		transactionsRetrieved = transactionRepository.findByDate(date, PageRequest.of(0, 50));
		assertEquals (1, transactionsRetrieved.size());
		var transactionRetrieved = transactionsRetrieved.get(0);
		assertEquals (transaction.getDate().minusDays(-3), transactionRetrieved.getDate());
		assertEquals (transaction.getDescription(), transactionRetrieved.getDescription());
		assertEquals (transaction.getAmount(), transactionRetrieved.getAmount());
		
		// get by id
		var transactionOptional = transactionRepository.findById(transactionRetrieved.getId());
		assertFalse(transactionOptional.isEmpty());
		transactionRetrieved = transactionOptional.get();
		assertEquals (transaction.getDate().minusDays(-3), transactionRetrieved.getDate());
		assertEquals (transaction.getDescription(), transactionRetrieved.getDescription());
		assertEquals (transaction.getAmount(), transactionRetrieved.getAmount());

	}
}
