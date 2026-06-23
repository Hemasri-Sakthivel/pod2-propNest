package com.cog.propNest.module.rentDepositManagement.service;

import com.cog.propNest.module.rentDepositManagement.dto.CreateDepositRequest;
import com.cog.propNest.module.rentDepositManagement.dto.DepositResponse;
import com.cog.propNest.module.rentDepositManagement.dto.ProcessRefundRequest;
import com.cog.propNest.module.rentDepositManagement.entity.DepositStatus;
import com.cog.propNest.module.rentDepositManagement.entity.SecurityDepositLedger;
import com.cog.propNest.module.rentDepositManagement.exception.BadRequestException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotFoundException;
import com.cog.propNest.module.rentDepositManagement.exception.DepositNotRefundableException;
import com.cog.propNest.module.rentDepositManagement.repository.SecurityDepositLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for managing security deposits and their refunds.
 */
@Service
@Transactional
public class SecurityDepositLedgerService {

    private final SecurityDepositLedgerRepository depositRepository;

    public SecurityDepositLedgerService(SecurityDepositLedgerRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    public DepositResponse createDeposit(CreateDepositRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        require(request.getLeaseId(), "leaseId");
        require(request.getTenantId(), "tenantId");
        require(request.getDepositAmount(), "depositAmount");
        require(request.getReceivedDate(), "receivedDate");

        SecurityDepositLedger deposit = new SecurityDepositLedger();
        deposit.setLeaseId(request.getLeaseId());
        deposit.setTenantId(request.getTenantId());
        deposit.setDepositAmount(request.getDepositAmount());
        deposit.setReceivedDate(request.getReceivedDate());
        deposit.setStatus(request.getStatus() != null
                ? parseStatus(request.getStatus()) : DepositStatus.Held);
        deposit.setRemarks(request.getRemarks());

        return DepositResponse.from(depositRepository.save(deposit));
    }

    @Transactional(readOnly = true)
    public List<DepositResponse> getAllDeposits() {
        return depositRepository.findAll().stream()
                .map(DepositResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepositResponse getDepositById(Long depositId) {
        return DepositResponse.from(findDeposit(depositId));
    }

    public DepositResponse processFullRefund(Long depositId, ProcessRefundRequest request) {
        SecurityDepositLedger deposit = findDeposit(depositId);
        // A full refund is only valid for a deposit that is still being held.
        if (deposit.getStatus() != DepositStatus.Held) {
            throw new DepositNotRefundableException();
        }
        deposit.setRefundAmount(deposit.getDepositAmount());
        deposit.setRefundDate(request != null && request.getRefundDate() != null
                ? request.getRefundDate() : LocalDate.now());
        deposit.setStatus(DepositStatus.FullyRefunded);
        if (request != null && request.getRemarks() != null) {
            deposit.setRemarks(request.getRemarks());
        }
        return DepositResponse.from(depositRepository.save(deposit));
    }

    private SecurityDepositLedger findDeposit(Long depositId) {
        return depositRepository.findById(depositId)
                .orElseThrow(() -> new DepositNotFoundException(depositId));
    }

    private DepositStatus parseStatus(String status) {
        try {
            return DepositStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid deposit status: " + status);
        }
    }

    private void require(Object value, String field) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            throw new BadRequestException(field + " is required");
        }
    }
}
