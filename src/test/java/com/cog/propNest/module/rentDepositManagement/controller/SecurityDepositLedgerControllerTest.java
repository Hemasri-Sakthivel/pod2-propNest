package com.cog.propNest.module.rentDepositManagement.controller;

import com.cog.propNest.common.exception.GlobalExceptionHandler;
import com.cog.propNest.module.rentDepositManagement.dto.DepositResponse;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotRefundableException;
import com.cog.propNest.module.rentDepositManagement.service.SecurityDepositLedgerService;
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

class SecurityDepositLedgerControllerTest {

    private SecurityDepositLedgerService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SecurityDepositLedgerService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SecurityDepositLedgerController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String validBody() {
        return "{\"leaseId\":1,\"tenantId\":1,\"depositAmount\":54000.00,"
                + "\"receivedDate\":\"2025-01-01\",\"status\":\"Held\",\"remarks\":\"Deposit\"}";
    }

    @Test
    void createDeposit_returns201AndMessage() throws Exception {
        String content = mockMvc.perform(post("/propNest/rentdeposit/createDeposit")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Security deposit recorded successfully"));
    }

    @Test
    void createDeposit_missingField_returns400() throws Exception {
        when(service.createDeposit(any())).thenThrow(new BadRequestException("depositAmount is required"));
        mockMvc.perform(post("/propNest/rentdeposit/createDeposit")
                        .contentType("application/json").content(validBody()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetchAllDeposits_returns200WithData() throws Exception {
        DepositResponse r = new DepositResponse();
        r.setDepositId(1L);
        when(service.getAllDeposits()).thenReturn(List.of(r));
        String content = mockMvc.perform(get("/propNest/rentdeposit/fetchAllDeposits"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("\"depositId\":1"));
    }

    @Test
    void fetchDepositById_notFound_returns404() throws Exception {
        when(service.getDepositById(99L)).thenThrow(new DepositNotFoundException(99L));
        mockMvc.perform(get("/propNest/rentdeposit/fetchDepositById/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void processFullRefund_returns200AndMessage() throws Exception {
        String content = mockMvc.perform(put("/propNest/rentdeposit/processFullRefund/1")
                        .contentType("application/json").content("{\"refundDate\":\"2025-12-31\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Full refund processed successfully"));
    }

    @Test
    void processFullRefund_withoutBody_returns200() throws Exception {
        mockMvc.perform(put("/propNest/rentdeposit/processFullRefund/1"))
                .andExpect(status().isOk());
    }

    @Test
    void processFullRefund_notHeld_returns400() throws Exception {
        when(service.processFullRefund(eq(1L), any())).thenThrow(new DepositNotRefundableException());
        String content = mockMvc.perform(put("/propNest/rentdeposit/processFullRefund/1")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertTrue(content.contains("Deposit must be Held for full refund"));
    }

    @Test
    void processFullRefund_notFound_returns404() throws Exception {
        when(service.processFullRefund(eq(99L), any())).thenThrow(new DepositNotFoundException(99L));
        mockMvc.perform(put("/propNest/rentdeposit/processFullRefund/99")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
    }
}
