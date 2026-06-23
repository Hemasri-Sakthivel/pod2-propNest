package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.rentDepositManagement.dto.PaymentResponse;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentAlreadyReceivedException;
import com.cog.propNest.module.rentDepositManagement.exception.PaymentNotFoundException;
import com.cog.propNest.module.rentDepositManagement.service.RentPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RentPaymentControllerTest {

    private RentPaymentService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(RentPaymentService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RentPaymentController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String validBody() {
        return "{\"invoiceId\":1,\"paidAmount\":18500.00,\"paymentDate\":\"2025-05-03\","
                + "\"method\":\"Online\",\"status\":\"Received\",\"remarks\":\"Full payment\"}";
    }

    @Test
    void createPayment_returns201AndMessage() throws Exception {
        String content = mockMvc.perform(post("/propNest/rentdeposit/createPayment")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Payment recorded successfully"));
    }

    @Test
    void createPayment_invoiceNotFound_returns404() throws Exception {
        when(service.createPayment(any())).thenThrow(new InvoiceNotFoundException(1L));
        mockMvc.perform(post("/propNest/rentdeposit/createPayment")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_missingField_returns400() throws Exception {
        when(service.createPayment(any())).thenThrow(new BadRequestException("paidAmount is required"));
        mockMvc.perform(post("/propNest/rentdeposit/createPayment")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchAllPayments_returns200WithData() throws Exception {
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(1L);
        when(service.getAllPayments()).thenReturn(List.of(r));
        String content = mockMvc.perform(get("/propNest/rentdeposit/fetchAllPayments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("\"paymentId\":1"));
    }

    @Test
    void fetchPaymentById_notFound_returns404() throws Exception {
        when(service.getPaymentById(99L)).thenThrow(new PaymentNotFoundException(99L));
        mockMvc.perform(get("/propNest/rentdeposit/fetchPaymentById/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePayment_returns200AndMessage() throws Exception {
        String content = mockMvc.perform(put("/propNest/rentdeposit/updatePayment/2")
                        .contentType("application/json").content("{\"status\":\"Received\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Payment updated successfully"));
    }

    @Test
    void updatePayment_alreadyReceived_returns400() throws Exception {
        when(service.updatePayment(eq(1L), any())).thenThrow(new PaymentAlreadyReceivedException());
        String content = mockMvc.perform(put("/propNest/rentdeposit/updatePayment/1")
                        .contentType("application/json").content("{\"status\":\"Received\"}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Cannot modify a Received payment"));
    }

    @Test
    void updatePayment_notFound_returns404() throws Exception {
        when(service.updatePayment(eq(99L), any())).thenThrow(new PaymentNotFoundException(99L));
        mockMvc.perform(put("/propNest/rentdeposit/updatePayment/99")
                        .contentType("application/json").content("{\"status\":\"Received\"}"))
                .andExpect(status().isNotFound());
    }
}
