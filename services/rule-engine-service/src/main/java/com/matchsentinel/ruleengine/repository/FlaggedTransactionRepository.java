package com.matchsentinel.ruleengine.repository;

import com.matchsentinel.ruleengine.domain.FlaggedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FlaggedTransactionRepository extends JpaRepository<FlaggedTransaction, UUID> {
}
