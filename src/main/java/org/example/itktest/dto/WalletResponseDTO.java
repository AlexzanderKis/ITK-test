package org.example.itktest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

// no getter setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponseDTO {
    private UUID id;
    private BigDecimal balance;
}
