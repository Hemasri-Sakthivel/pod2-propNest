package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreateDepositRequest;
import com.cog.propNest.module.rentDepositManagement.dto.ProcessRefundRequest;
import com.cog.propNest.module.rentDepositManagement.entity.DepositStatus;
import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotRefundableException;
import com.cog.propNest.module.rentDepositManagement.repository.SecurityDepositLedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Service-layer unit tests for {@link SecurityDepositLedgerService} (Mockito). */
class SecurityDepositLedgerServiceTest {

    private SecurityDepositLedgerRepository depositRepository;
    private SecurityDepositLedgerService service;

    @BeforeEach
    void setUp() {
        depositRepository = mock(SecurityDepositLedgerRepository.class);
        service = new SecurityDepositLedgerService(depositRepository);
        when(depositRepository.save(any(SecurityDepositLedger.class))).thenAnswer(i -> i.getArgument(0));
    }

    private CreateDepositRequest validCreate() {
        CreateDepositRequest r = new CreateDepositRequest();
        r.setLeaseId(1L);
        r.setTenantId(1L);
        r.setDepositAmount(new BigDecimal("54000.00"));
        r.setReceivedDate(LocalDate.parse("2025-01-01"));
        r.setStatus("Held");
        r.setRemarks("Deposit at lease start");
        return r;
    }

    private SecurityDepositLedger existing(long id, DepositStatus status) {
        SecurityDepositLedger d = new SecurityDepositLedger();
        d.setDepositId(id);
        d.setDepositAmount(new BigDecimal("54000.00"));
        d.setReceivedDate(LocalDate.parse("2025-01-01"));
        d.setRefundAmount(BigDecimal.ZERO);
        d.setStatus(status);
        return d;
    }

    @Test
    void createDeposit_success_returnsResponse() {
        assertEquals("Held", service.createDeposit(validCreate()).getStatus());
    }

    @Test
    void createDeposit_savesEntity() {
        service.createDeposit(validCreate());
        verify(depositRepository).save(any(SecurityDepositLedger.class));
    }

    @Test
    void createDeposit_missingDepositAmount_throws() {
        CreateDepositRequest r = validCreate();
        r.setDepositAmount(null);
        assertEquals("depositAmount is required",
                assertThrows(BadRequestException.class, () -> service.createDeposit(r)).getMessage());
    }

    @Test
    void createDeposit_missingReceivedDate_throws() {
        CreateDepositRequest r = validCreate();
        r.setReceivedDate(null);
        assertEquals("receivedDate is required",
                assertThrows(BadRequestException.class, () -> service.createDeposit(r)).getMessage());
    }

    @Test
    void createDeposit_defaultsStatusToHeld() {
        CreateDepositRequest r = validCreate();
        r.setStatus(null);
        assertEquals("Held", service.createDeposit(r).getStatus());
    }

    @Test
    void createDeposit_invalidStatus_throws() {
        CreateDepositRequest r = validCreate();
        r.setStatus("Frozen");
        assertEquals("Invalid deposit status: Frozen",
                assertThrows(BadRequestException.class, () -> service.createDeposit(r)).getMessage());
    }

    @Test
    void getAllDeposits_returnsMappedList() {
        when(depositRepository.findAll()).thenReturn(List.of(existing(1, DepositStatus.Held)));
        assertEquals(1, service.getAllDeposits().size());
    }

    @Test
    void getDepositById_found() {
        when(depositRepository.findById(1L)).thenReturn(Optional.of(existing(1, DepositStatus.Held)));
        assertEquals(1L, service.getDepositById(1L).getDepositId());
    }

    @Test
    void getDepositById_notFound_throws() {
        when(depositRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DepositNotFoundException.class, () -> service.getDepositById(99L));
    }

    @Test
    void processFullRefund_success_setsFullyRefunded() {
        when(depositRepository.findById(1L)).thenReturn(Optional.of(existing(1, DepositStatus.Held)));
        assertEquals("FullyRefunded",
                service.processFullRefund(1L, new ProcessRefundRequest()).getStatus());
    }

    @Test
    void processFullRefund_setsRefundAmountToDepositAmount() {
        when(depositRepository.findById(1L)).thenReturn(Optional.of(existing(1, DepositStatus.Held)));
        assertEquals(0, service.processFullRefund(1L, new ProcessRefundRequest())
                .getRefundAmount().compareTo(new BigDecimal("54000.00")));
    }

    @Test
    void processFullRefund_defaultsRefundDateWhenNull() {
        when(depositRepository.findById(1L)).thenReturn(Optional.of(existing(1, DepositStatus.Held)));
        assertNotNull(service.processFullRefund(1L, new ProcessRefundRequest()).getRefundDate());
    }

    @Test
    void processFullRefund_notHeld_throws() {
        when(depositRepository.findById(1L)).thenReturn(Optional.of(existing(1, DepositStatus.FullyRefunded)));
        assertEquals("Deposit must be Held for full refund",
                assertThrows(DepositNotRefundableException.class,
                        () -> service.processFullRefund(1L, new ProcessRefundRequest())).getMessage());
    }

    @Test
    void processFullRefund_notFound_throws() {
        when(depositRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(DepositNotFoundException.class,
                () -> service.processFullRefund(99L, new ProcessRefundRequest()));
    }
}
