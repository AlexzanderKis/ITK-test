package org.example.itktest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponseDTO(UUID walletId,
                                BigDecimal balance) {

}
