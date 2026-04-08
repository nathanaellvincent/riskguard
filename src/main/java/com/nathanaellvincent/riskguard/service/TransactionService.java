package com.nathanaellvincent.riskguard.service;

import com.nathanaellvincent.riskguard.dto.TransactionRequest;
import com.nathanaellvincent.riskguard.dto.TransactionResponse;
import com.nathanaellvincent.riskguard.model.Account;
import com.nathanaellvincent.riskguard.model.Transaction;
import com.nathanaellvincent.riskguard.model.Transaction.TransactionStatus;
import com.nathanaellvincent.riskguard.repository.AccountRepository;
import com.nathanaellvincent.riskguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    // Risk thresholds (amounts in IDR)
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD  = new BigDecimal("10000000");  // 10 M
    private static final BigDecimal VERY_HIGH_THRESHOLD    = new BigDecimal("50000000");  // 50 M
    private static final int VELOCITY_WINDOW_MINUTES       = 5;
    private static final long VELOCITY_MAX_COUNT           = 3;

    private final TransactionRepository txRepo;
    private final AccountRepository accountRepo;

    @Transactional
    public TransactionResponse submit(String username, TransactionRequest req) {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));

        int score = calculateRiskScore(account.getId(), req.getAmount());
        TransactionStatus status = scoreToStatus(score);

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setAmount(req.getAmount());
        tx.setType(req.getType());
        tx.setDescription(req.getDescription());
        tx.setRiskScore(score);
        tx.setStatus(status);

        txRepo.save(tx);
        return TransactionResponse.from(tx);
    }

    public List<TransactionResponse> findByUsername(String username) {
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found: " + username));
        return txRepo.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    // Risk scoring: each signal adds points; 0–40 = APPROVED, 41–70 = FLAGGED, 71+ = REJECTED
    private int calculateRiskScore(Long accountId, BigDecimal amount) {
        int score = 0;

        if (amount.compareTo(VERY_HIGH_THRESHOLD) >= 0) {
            score += 60;
        } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += 40;
        }

        // Velocity check — too many transactions in a short window
        Instant since = Instant.now().minusSeconds(VELOCITY_WINDOW_MINUTES * 60L);
        long recentCount = txRepo.countRecentByAccountId(accountId, since);
        if (recentCount >= VELOCITY_MAX_COUNT) {
            score += 30;
        }

        // Off-hours signal (02:00–04:00 WIB = UTC+7)
        int hour = ZonedDateTime.now(ZoneId.of("Asia/Jakarta")).getHour();
        if (hour >= 2 && hour < 4) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    private TransactionStatus scoreToStatus(int score) {
        if (score >= 71) return TransactionStatus.REJECTED;
        if (score >= 41) return TransactionStatus.FLAGGED;
        return TransactionStatus.APPROVED;
    }
}
