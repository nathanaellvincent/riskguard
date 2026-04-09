package com.nathanaellvincent.riskguard.controller;

import com.nathanaellvincent.riskguard.dto.TransactionRequest;
import com.nathanaellvincent.riskguard.dto.TransactionResponse;
import com.nathanaellvincent.riskguard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> submit(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody TransactionRequest req) {
        TransactionResponse response = transactionService.submit(principal.getUsername(), req);
        HttpStatus status = switch (response.getStatus()) {
            case APPROVED -> HttpStatus.CREATED;
            case FLAGGED  -> HttpStatus.ACCEPTED;
            case REJECTED -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping
    public List<TransactionResponse> history(@AuthenticationPrincipal UserDetails principal) {
        return transactionService.findByUsername(principal.getUsername());
    }
}
