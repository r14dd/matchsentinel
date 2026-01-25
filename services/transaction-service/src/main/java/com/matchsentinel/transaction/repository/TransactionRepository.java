package com.matchsentinel.transaction.repository;

import com.matchsentinel.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
