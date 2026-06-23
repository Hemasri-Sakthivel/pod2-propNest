package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.response.ApiResponse;
import com.cog.propNest.module.rentDepositManagement.dto.CreateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.dto.InvoiceResponse;
import com.cog.propNest.module.rentDepositManagement.dto.MessageResponse;
import com.cog.propNest.module.rentDepositManagement.dto.UpdateInvoiceRequest;
import com.cog.propNest.module.rentDepositManagement.service.RentInvoiceService;
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
 * REST endpoints for rent invoices (PropNest 2.4 — Rent &amp; Deposit Management).
 */
@RestController
@RequestMapping("/propNest/rentdeposit")
public class RentInvoiceController {

    private final RentInvoiceService invoiceService;

    public RentInvoiceController(RentInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/createInvoice")
    public ResponseEntity<MessageResponse> createInvoice(
            @RequestBody CreateInvoiceRequest request) {
        invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Invoice created successfully"));
    }

    @GetMapping("/fetchAllInvoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> fetchAllInvoices() {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getAllInvoices()));
    }

    @GetMapping("/fetchInvoiceById/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> fetchInvoiceById(
            @PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(invoiceService.getInvoiceById(invoiceId)));
    }

    @PutMapping("/updateInvoice/{invoiceId}")
    public ResponseEntity<MessageResponse> updateInvoice(
            @PathVariable Long invoiceId,
            @RequestBody UpdateInvoiceRequest request) {
        invoiceService.updateInvoice(invoiceId, request);
        return ResponseEntity.ok(new MessageResponse("Invoice updated successfully"));
    }
}
