package com.microsservicos.wallet_service.controller;

import com.microsservicos.wallet_service.dto.CreateWalletRequest;
import com.microsservicos.wallet_service.dto.OperationRequest;
import com.microsservicos.wallet_service.dto.WalletResponse;
import com.microsservicos.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@RequestBody @Valid CreateWalletRequest request) {
        WalletResponse response = walletService.create(request.userId());
        return ResponseEntity
                .created(URI.create("/api/wallets/" + response.userId()))
                .body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debit(
            @PathVariable UUID userId,
            @RequestBody @Valid OperationRequest request) {
        return ResponseEntity.ok(walletService.debit(userId, request.amount()));
    }

    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> credit(
            @PathVariable UUID userId,
            @RequestBody @Valid OperationRequest request) {
        return ResponseEntity.ok(walletService.credit(userId, request.amount()));
    }
}