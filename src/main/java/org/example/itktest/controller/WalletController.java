package org.example.itktest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itktest.dto.WalletOperationRequest;
import org.example.itktest.dto.WalletResponseDTO;
import org.example.itktest.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponseDTO> processOperation(@Valid @RequestBody WalletOperationRequest operationRequest){
        WalletResponseDTO walletResponseDTO = walletService.processOperation(operationRequest);
        return ResponseEntity.ok(walletResponseDTO);
    }

    @PostMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponseDTO> getBalance(@PathVariable UUID walletId){
        WalletResponseDTO walletResponseDTO = walletService.getBalance(walletId);
        return ResponseEntity.ok(walletResponseDTO);
    }
}
