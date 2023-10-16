package com.samuelwu.wex.tag.transaction.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import com.samuelwu.wex.tag.transaction.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByDate(LocalDate date, PageRequest pageSize);
}
