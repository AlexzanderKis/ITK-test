package org.example.itktest.service;

import lombok.RequiredArgsConstructor;
import org.example.itktest.WalletOperationType;
import org.example.itktest.dto.WalletOperationRequest;
import org.example.itktest.dto.WalletResponseDTO;
import org.example.itktest.entity.WalletEntity;
import org.example.itktest.exception.NotEnoughFoundException;
import org.example.itktest.exception.WalletNotFoundException;
import org.example.itktest.repository.WalletRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepo walletRepo;

    @Transactional
    public WalletResponseDTO processOperation(WalletOperationRequest operationRequest) {
        WalletEntity walletEntity = walletRepo.findByIdWithLock(operationRequest.walletId())
                .orElseThrow(() -> new WalletNotFoundException(operationRequest.walletId()));
        BigDecimal currentBalance = walletEntity.getBalance();

        if (operationRequest.operationType() == WalletOperationType.DEPOSIT) {
            walletEntity.setBalance(currentBalance.add(operationRequest.amount()));

        } else if (operationRequest.operationType() == WalletOperationType.WITHDRAW){

            if (currentBalance.compareTo(operationRequest.amount()) < 0){
                throw new NotEnoughFoundException();
            } else {
                walletEntity.setBalance(currentBalance.subtract(operationRequest.amount()));
            }
        }

        // можно не вызывать save() из-за dirty-checking
        walletRepo.save(walletEntity);
        return new WalletResponseDTO(walletEntity.getWalletId(), walletEntity.getBalance());
    }

    @Transactional(readOnly = true)
    public WalletResponseDTO getBalance(UUID walletId){
        WalletEntity walletEntity = walletRepo.findById(walletId).orElseThrow(WalletNotFoundException::new);

        return new WalletResponseDTO(walletEntity.getWalletId(), walletEntity.getBalance());
    }
}
