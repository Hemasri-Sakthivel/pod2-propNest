package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.rentDepositManagement.dto.InvoiceResponse;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.InvoiceNotFoundException;
import com.cog.propNest.module.rentDepositManagement.service.RentInvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RentInvoiceControllerTest {

    private RentInvoiceService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(RentInvoiceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new RentInvoiceController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String validBody() {
        return "{\"leaseId\":1,\"tenantId\":1,\"billingMonth\":\"2025-05\",\"rentAmount\":18000.00,"
                + "\"otherCharges\":500.00,\"totalAmount\":18500.00,\"dueDate\":\"2025-05-05\","
                + "\"status\":\"Generated\",\"paymentMethod\":\"Online\"}";
    }

    @Test
    void createInvoice_returns201AndMessage() throws Exception {
        String content = mockMvc.perform(post("/propNest/rentdeposit/createInvoice")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Invoice created successfully"));
    }

    @Test
    void createInvoice_missingField_returns400() throws Exception {
        when(service.createInvoice(any())).thenThrow(new BadRequestException("billingMonth is required"));
        String content = mockMvc.perform(post("/propNest/rentdeposit/createInvoice")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("billingMonth is required"));
    }

    @Test
    void fetchAllInvoices_returns200WithData() throws Exception {
        InvoiceResponse r = new InvoiceResponse();
        r.setInvoiceId(1L);
        when(service.getAllInvoices()).thenReturn(List.of(r));
        String content = mockMvc.perform(get("/propNest/rentdeposit/fetchAllInvoices"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("\"invoiceId\":1"));
    }

    @Test
    void fetchInvoiceById_returns200() throws Exception {
        InvoiceResponse r = new InvoiceResponse();
        r.setInvoiceId(5L);
        when(service.getInvoiceById(5L)).thenReturn(r);
        String content = mockMvc.perform(get("/propNest/rentdeposit/fetchInvoiceById/5"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("\"invoiceId\":5"));
    }

    @Test
    void fetchInvoiceById_notFound_returns404() throws Exception {
        when(service.getInvoiceById(99L)).thenThrow(new InvoiceNotFoundException(99L));
        mockMvc.perform(get("/propNest/rentdeposit/fetchInvoiceById/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInvoice_returns200AndMessage() throws Exception {
        String content = mockMvc.perform(put("/propNest/rentdeposit/updateInvoice/1")
                        .contentType("application/json").content("{\"status\":\"Paid\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Invoice updated successfully"));
    }

    @Test
    void updateInvoice_notFound_returns404() throws Exception {
        when(service.updateInvoice(eq(99L), any())).thenThrow(new InvoiceNotFoundException(99L));
        mockMvc.perform(put("/propNest/rentdeposit/updateInvoice/99")
                        .contentType("application/json").content("{\"status\":\"Paid\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInvoice_genericException_returns500() throws Exception {
        doThrow(new RuntimeException("boom")).when(service).createInvoice(any());
        mockMvc.perform(post("/propNest/rentdeposit/createInvoice")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isInternalServerError());
    }
}
