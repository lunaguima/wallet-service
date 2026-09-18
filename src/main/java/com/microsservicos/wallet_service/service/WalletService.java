package com.microsservicos.wallet_service.service;

import com.microsservicos.wallet_service.domain.Wallet;
import com.microsservicos.wallet_service.dto.WalletResponse;
import com.microsservicos.wallet_service.exception.WalletNotFoundException;
import com.microsservicos.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(UUID userId) {
        Wallet wallet = findWalletOrThrow(userId);
        return new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance());
    }

    @Transactional
    public WalletResponse debit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletOrThrow(userId);
        wallet.debit(amount);
        Wallet saved = walletRepository.save(wallet);
        return new WalletResponse(saved.getId(), saved.getUserId(), saved.getBalance());
    }

    @Transactional
    public WalletResponse credit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletOrThrow(userId);
        wallet.credit(amount);
        Wallet saved = walletRepository.save(wallet);
        return new WalletResponse(saved.getId(), saved.getUserId(), saved.getBalance());
    }

    private Wallet findWalletOrThrow(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId));
    }
}