package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.response.ApiResponse;
import com.cog.propNest.module.rentDepositManagement.dto.CreatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.dto.MessageResponse;
import com.cog.propNest.module.rentDepositManagement.dto.PaymentResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdatePaymentRequest;
import com.cog.propNest.module.rentDepositManagement.service.RentPaymentService;
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
 * REST endpoints for rent payments (PropNest 2.4 — Rent &amp; Deposit Management).
 */
@RestController
@RequestMapping("/propNest/rentdeposit")
public class RentPaymentController {

    private final RentPaymentService paymentService;

    public RentPaymentController(RentPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/createPayment")
    public ResponseEntity<MessageResponse> createPayment(
            @RequestBody CreatePaymentRequest request) {
        paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Payment recorded successfully"));
    }

    @GetMapping("/fetchAllPayments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> fetchAllPayments() {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getAllPayments()));
    }

    @GetMapping("/fetchPaymentById/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> fetchPaymentById(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(paymentId)));
    }

    @PutMapping("/updatePayment/{paymentId}")
    public ResponseEntity<MessageResponse> updatePayment(
            @PathVariable Long paymentId,
            @RequestBody UpdatePaymentRequest request) {
        paymentService.updatePayment(paymentId, request);
        return ResponseEntity.ok(new MessageResponse("Payment updated successfully"));
    }
}
