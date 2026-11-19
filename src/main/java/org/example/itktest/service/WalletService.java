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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepo walletRepo;

    @Transactional
    public WalletResponseDTO processOperation(WalletOperationRequest operationRequest) {

        int updated;
        if (operationRequest.operationType() == WalletOperationType.DEPOSIT) {
            updated = walletRepo.deposit(operationRequest.walletId(), operationRequest.amount());
        } else {
            updated = walletRepo.withdraw(operationRequest.walletId(), operationRequest.amount());
        }

        if (updated == 0) {

            // кошелька нет ИЛИ на снятие не хватило денег
            // различаем кошельки через exists:
            if (!walletRepo.existsById(operationRequest.walletId())) {
                throw new WalletNotFoundException(operationRequest.walletId());
            }
            throw new NotEnoughFoundException();
        }

        WalletEntity walletEntity = walletRepo.findById(operationRequest.walletId()).orElseThrow();

        // можно не вызывать save() из-за dirty-checking
//        walletRepo.save(walletEntity);
        return new WalletResponseDTO(walletEntity.getWalletId(), walletEntity.getBalance());
    }

    @Transactional(readOnly = true)
    public WalletResponseDTO getBalance(UUID walletId){
        WalletEntity walletEntity = walletRepo.findById(walletId).orElseThrow(WalletNotFoundException::new);

        return new WalletResponseDTO(walletEntity.getWalletId(), walletEntity.getBalance());
    }
}
