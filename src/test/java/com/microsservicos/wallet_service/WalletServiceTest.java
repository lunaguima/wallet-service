package com.microsservicos.wallet_service;

import com.microsservicos.wallet_service.domain.Wallet;
import com.microsservicos.wallet_service.dto.WalletResponse;
import com.microsservicos.wallet_service.exception.InsufficientBalanceException;
import com.microsservicos.wallet_service.exception.WalletAlreadyExistsException;
import com.microsservicos.wallet_service.exception.WalletNotFoundException;
import com.microsservicos.wallet_service.repository.WalletRepository;
import com.microsservicos.wallet_service.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Deve debitar com sucesso quando o saldo for suficiente")
    void shouldDebitSuccessfullyWhenBalanceIsSufficient() {
        BigDecimal debitAmount = new BigDecimal("40.00");
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.debit(userId, debitAmount);

        assertNotNull(response);
        assertEquals(new BigDecimal("60.00"), response.balance());
        verify(walletRepository, times(1)).findByUserId(userId);
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    @DisplayName("Deve lançar InsufficientBalanceException quando saldo for menor que o débito")
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        BigDecimal debitAmount = new BigDecimal("150.00");
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () -> walletService.debit(userId, debitAmount));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Deve lançar WalletNotFoundException quando a carteira não existir")
    void shouldThrowExceptionWhenWalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> walletService.debit(userId, new BigDecimal("10.00")));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Deve creditar saldo com sucesso")
    void shouldCreditSuccessfully() {
        BigDecimal creditAmount = new BigDecimal("50.00");
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.credit(userId, creditAmount);

        assertEquals(new BigDecimal("150.00"), response.balance());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    @DisplayName("Deve criar carteira com saldo zero")
    void shouldCreateWalletWithZeroBalance() {
        when(walletRepository.existsByUserId(userId)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            w.setBalance(BigDecimal.ZERO);
            return w;
        });

        WalletResponse response = walletService.create(userId);

        assertEquals(userId, response.userId());
        assertEquals(BigDecimal.ZERO, response.balance());
    }

    @Test
    @DisplayName("Deve lançar WalletAlreadyExistsException quando a carteira já existir")
    void shouldThrowExceptionWhenWalletAlreadyExists() {
        when(walletRepository.existsByUserId(userId)).thenReturn(true);

        assertThrows(WalletAlreadyExistsException.class, () -> walletService.create(userId));
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}