package org.example.itktest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

// сущность для работы с таблицей в БД
@Entity
@Table(name = "wallets")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class WalletEntity {

    @Id
    private UUID id;

    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance;
}
