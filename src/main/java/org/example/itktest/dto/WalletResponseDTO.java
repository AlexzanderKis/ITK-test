package org.example.itktest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponseDTO(UUID walletId,
                                BigDecimal balance) {

}
