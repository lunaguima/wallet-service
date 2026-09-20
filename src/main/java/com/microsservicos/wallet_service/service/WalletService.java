package com.microsservicos.wallet_service.service;

import com.microsservicos.wallet_service.domain.Wallet;
import com.microsservicos.wallet_service.dto.WalletResponse;
import com.microsservicos.wallet_service.exception.WalletAlreadyExistsException;
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

    @Transactional
    public WalletResponse create(UUID userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new WalletAlreadyExistsException("Já existe uma carteira para o usuário: " + userId);
        }
        Wallet saved = walletRepository.save(Wallet.builder().userId(userId).build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(UUID userId) {
        return toResponse(findWalletOrThrow(userId));
    }

    @Transactional
    public WalletResponse debit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletOrThrow(userId);
        wallet.debit(amount);
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponse credit(UUID userId, BigDecimal amount) {
        Wallet wallet = findWalletOrThrow(userId);
        wallet.credit(amount);
        return toResponse(walletRepository.save(wallet));
    }

    private Wallet findWalletOrThrow(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Carteira não encontrada para o usuário: " + userId));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance());
    }
}