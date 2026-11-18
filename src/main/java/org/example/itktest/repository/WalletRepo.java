package org.example.itktest.repository;

import jakarta.persistence.LockModeType;
import org.example.itktest.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepo extends JpaRepository<WalletEntity, UUID> {

    // LockModeType.PESSIMISTIC_WRITE заставит PostgreSQL наложить эксклюзивную блокировку на строку (SELECT...FOR UPDATE),
    // выстроив параллельные транзакции в очередь и предотвратив race contidion
    @Lock(LockModeType.PESSIMISTIC_WRITE)

    @Query("SELECT w from WalletEntity w where w.id = :id")
    Optional<WalletEntity> findByIdWithLock(@Param("id") UUID id);
}
