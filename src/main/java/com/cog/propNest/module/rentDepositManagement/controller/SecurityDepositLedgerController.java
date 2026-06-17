package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.response.ApiResponse;
import com.cog.propNest.module.rentDepositManagement.dto.CreateDepositRequest;
import com.cog.propNest.module.rentDepositManagement.dto.DepositResponse;
import com.cog.propNest.module.rentDepositManagement.dto.MessageResponse;
import com.cog.propNest.module.rentDepositManagement.dto.ProcessRefundRequest;
import com.cog.propNest.module.rentDepositManagement.service.SecurityDepositLedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for security deposits (PropNest 2.4 — Rent &amp; Deposit Management).
 */
@RestController
@RequestMapping("/propNest/rentdeposit")
public class SecurityDepositLedgerController {

    private final SecurityDepositLedgerService depositService;

    public SecurityDepositLedgerController(SecurityDepositLedgerService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/createDeposit")
    public ResponseEntity<MessageResponse> createDeposit(
            @RequestBody CreateDepositRequest request) {
        depositService.createDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Security deposit recorded successfully"));
    }

    @GetMapping("/fetchAllDeposits")
    public ResponseEntity<ApiResponse<List<DepositResponse>>> fetchAllDeposits() {
        return ResponseEntity.ok(ApiResponse.success(depositService.getAllDeposits()));
    }

    @GetMapping("/fetchDepositById/{depositId}")
    public ResponseEntity<ApiResponse<DepositResponse>> fetchDepositById(
            @PathVariable Long depositId) {
        return ResponseEntity.ok(ApiResponse.success(depositService.getDepositById(depositId)));
    }

    @PutMapping("/processFullRefund/{depositId}")
    public ResponseEntity<MessageResponse> processFullRefund(
            @PathVariable Long depositId,
            @RequestBody(required = false) ProcessRefundRequest request) {
        depositService.processFullRefund(depositId, request);
        return ResponseEntity.ok(new MessageResponse("Full refund processed successfully"));
    }
}
