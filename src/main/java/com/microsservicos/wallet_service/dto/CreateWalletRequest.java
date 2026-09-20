package com.microsservicos.wallet_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateWalletRequest(
        @NotNull(message = "O userId não pode ser nulo")
        UUID userId
) {}