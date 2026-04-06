package com.nathanaellvincent.riskguard.repository;

import com.nathanaellvincent.riskguard.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.account.id = :accountId AND t.createdAt >= :since")
    long countRecentByAccountId(@Param("accountId") Long accountId, @Param("since") Instant since);
}
