package com.ewallet.api.controller;

import com.ewallet.api.dto.wallet.WalletStatusChangeResponseDTO;
import com.ewallet.api.entity.WalletStatus;
import com.ewallet.api.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v0/admin/wallets")
@RequiredArgsConstructor
public class AdminWalletController {

    private final WalletService walletService;

    /**
     * Changes the status of a specific wallet
     * Restricted to admin and support roles
     * @param walletId The id of the wallet to modify
     * @param status The new target status
     * @param principal The authenticated admin/support user
     * @return WalletStatusChangeResponseDTO containing the result of the operation
     */
    @PatchMapping("/{walletId}/status")
    public ResponseEntity<WalletStatusChangeResponseDTO> changeStatus(
            @PathVariable Long walletId,
            @RequestParam WalletStatus status,
            Principal principal) {

        WalletStatusChangeResponseDTO response = walletService.changeWalletStatus(
                walletId,
                status,
                principal.getName()
        );
        return new ResponseEntity<>(response , HttpStatus.OK);
    }
}