package org.example.itktest.repository;

import org.example.itktest.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface WalletRepo extends JpaRepository<WalletEntity, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WalletEntity w SET w.balance = w.balance + :amount WHERE w.walletId = :id")
    int deposit(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WalletEntity w SET w.balance = w.balance - :amount WHERE w.walletId = :id AND w.balance >= :amount")
    int withdraw(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
