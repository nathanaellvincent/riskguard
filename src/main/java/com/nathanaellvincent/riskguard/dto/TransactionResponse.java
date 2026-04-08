package com.nathanaellvincent.riskguard.dto;

import com.nathanaellvincent.riskguard.model.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private String description;
    private int riskScore;
    private Transaction.TransactionStatus status;
    private Instant createdAt;

    public static TransactionResponse from(Transaction t) {
        TransactionResponse r = new TransactionResponse();
        r.id = t.getId();
        r.amount = t.getAmount();
        r.type = t.getType();
        r.description = t.getDescription();
        r.riskScore = t.getRiskScore();
        r.status = t.getStatus();
        r.createdAt = t.getCreatedAt();
        return r;
    }
}
