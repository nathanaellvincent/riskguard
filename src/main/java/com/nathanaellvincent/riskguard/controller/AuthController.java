package com.nathanaellvincent.riskguard.controller;

import com.nathanaellvincent.riskguard.dto.AuthRequest;
import com.nathanaellvincent.riskguard.dto.AuthResponse;
import com.nathanaellvincent.riskguard.model.Account;
import com.nathanaellvincent.riskguard.repository.AccountRepository;
import com.nathanaellvincent.riskguard.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest req) {
        if (accountRepository.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "username already taken"));
        }
        Account account = new Account(req.getUsername(), passwordEncoder.encode(req.getPassword()));
        accountRepository.save(account);
        String token = jwtService.generateToken(account.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, account.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        String token = jwtService.generateToken(req.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, req.getUsername()));
    }
}
