
// package com.cog.propNest.module.tenantOnboardingLease.service;

// import com.cog.propNest.module.tenantOnboardingLease.dto.LeaseAgreementDTO;
// import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;
// import com.cog.propNest.module.tenantOnboardingLease.repository.LeaseAgreementRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import java.time.LocalDate;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// @Service
// public class LeaseAgreementService {

//     @Autowired
//     private LeaseAgreementRepository repo;

//     // POST - createLease
//     public Map<String, Object> createLease(
//                                LeaseAgreementDTO dto) {
//         LeaseAgreement lease = new LeaseAgreement();
//         lease.setUnitId(dto.getUnitId());
//         lease.setTenantId(dto.getTenantId());
//         lease.setOwnerId(dto.getOwnerId());
//         lease.setStartDate(dto.getStartDate());
//         lease.setEndDate(dto.getEndDate());
//         lease.setMonthlyRent(dto.getMonthlyRent());
//         lease.setSecurityDeposit(dto.getSecurityDeposit());
//         if (dto.getEscalationPercent() != null) {
//             lease.setEscalationPercent(
//                 dto.getEscalationPercent());
//         } else {
//             lease.setEscalationPercent(0.0);
//         }
//         if (dto.getNoticePeriodDays() != null) {
//             lease.setNoticePeriodDays(
//                 dto.getNoticePeriodDays());
//         } else {
//             lease.setNoticePeriodDays(30);
//         }
//         lease.setStatus(LeaseAgreement.LeaseStatus.Draft);
//         repo.save(lease);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Lease created successfully");
//         return response;
//     }

//     // GET - getAllLeases
//     public Map<String, Object> getAllLeases(
//                                String status) {
//         List<LeaseAgreement> list;
//         if (status != null && !status.isEmpty()) {
//             list = repo.findByStatus(
//                 LeaseAgreement.LeaseStatus.valueOf(status));
//         } else {
//             list = repo.findAll();
//         }
//         if (list.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "No leases found");
//             return error;
//         }
//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Leases retrieved successfully");
//         response.put("data", list);
//         response.put("totalElements", list.size());
//         return response;
//     }

//     // GET - getLeaseById
//     public Map<String, Object> getLeaseById(
//                                Long leaseId) {
//         Optional<LeaseAgreement> optional =
//             repo.findById(leaseId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "Lease not found");
//             return error;
//         }
//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Lease retrieved successfully");
//         response.put("data", optional.get());
//         return response;
//     }

//     // PUT - activateLease
//     public Map<String, Object> activateLease(
//                                Long leaseId) {
//         Optional<LeaseAgreement> optional =
//             repo.findById(leaseId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "Lease not found");
//             return error;
//         }
//         LeaseAgreement lease = optional.get();
//         if (lease.getStatus() !=
//             LeaseAgreement.LeaseStatus.Draft) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message",
//                 "Lease not in Draft status");
//             return error;
//         }
//         lease.setStatus(
//             LeaseAgreement.LeaseStatus.Active);
//         repo.save(lease);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Lease activated successfully");
//         return response;
//     }

//     // PUT - renewLease
//     public Map<String, Object> renewLease(
//                                Long leaseId,
//                                Map<String, Object> body) {
//         Optional<LeaseAgreement> optional =
//             repo.findById(leaseId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "Lease not found");
//             return error;
//         }
//         LeaseAgreement lease = optional.get();
//         if (lease.getStatus() !=
//             LeaseAgreement.LeaseStatus.Active) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "Lease is not active");
//             return error;
//         }

//         Double escalation = Double.valueOf(
//             body.get("escalationPercent").toString());
//         Double newRent = lease.getMonthlyRent() *
//             (1 + escalation / 100);

//         lease.setStatus(
//             LeaseAgreement.LeaseStatus.Renewed);
//         lease.setEscalationPercent(escalation);
//         repo.save(lease);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Lease renewed successfully");
//         response.put("newMonthlyRent", newRent);
//         return response;
//     }

//     // PUT - terminateLease
//     public Map<String, Object> terminateLease(Long leaseId,Map<String, String> body) {
//         Optional<LeaseAgreement> optional =
//             repo.findById(leaseId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "Lease not found");
//             return error;
//         }
//         LeaseAgreement lease = optional.get();
//         if (lease.getStatus() !=
//             LeaseAgreement.LeaseStatus.Active) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message",
//                 "Lease is not active");
//             return error;
//         }
//         lease.setStatus(
//             LeaseAgreement.LeaseStatus.Terminated);
//         repo.save(lease);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "Lease terminated successfully");
//         return response;
//     }
// }

package com.cog.propNest.module.tenantOnboardingLease.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cog.propNest.module.tenantOnboardingLease.dto.LeaseAgreementDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.LeaseAgreement;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.repository.LeaseAgreementRepository;

@Service
public class LeaseAgreementService {

    private static final Logger log =
        LoggerFactory.getLogger(LeaseAgreementService.class);

    @Autowired
    private LeaseAgreementRepository repo;

    // POST - createLease
    public Map<String, Object> createLease(LeaseAgreementDTO dto) {
        LeaseAgreement lease = new LeaseAgreement();
        lease.setUnitId(dto.getUnitId());
        lease.setTenantId(dto.getTenantId());
        lease.setOwnerId(dto.getOwnerId());
        lease.setStartDate(dto.getStartDate());
        lease.setEndDate(dto.getEndDate());
        lease.setMonthlyRent(dto.getMonthlyRent());
        lease.setSecurityDeposit(dto.getSecurityDeposit());
        if (dto.getEscalationPercent() != null) {
            lease.setEscalationPercent(
                dto.getEscalationPercent());
        } else {
            lease.setEscalationPercent(0.0);
        }
        if (dto.getNoticePeriodDays() != null) {
            lease.setNoticePeriodDays(
                dto.getNoticePeriodDays());
        } else {
            lease.setNoticePeriodDays(30);
        }
        lease.setStatus(LeaseAgreement.LeaseStatus.D);
        repo.save(lease);
        log.info("Lease created (leaseId={}, unitId={}, tenantId={})",
            lease.getLeaseId(), lease.getUnitId(), lease.getTenantId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Lease created successfully");
        return response;
    }

    // GET - getAllLeases
    public Map<String, Object> getAllLeases(String status) {
        List<LeaseAgreement> list;
        if (status != null && !status.isEmpty()) {
            list = repo.findByStatus(
                LeaseAgreement.LeaseStatus.valueOf(status));
        } else {
            list = repo.findAll();
        }
        if (list.isEmpty()) {
            throw new LeaseAgreementNotFoundException("No leases found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("totalElements", list.size());
        return response;
    }

    // GET - getLeaseById
    public Map<String, Object> getLeaseById(Long leaseId) {
        Optional<LeaseAgreement> optional =
            repo.findById(leaseId);
        if (optional.isEmpty()) {
            throw new LeaseAgreementNotFoundException("Lease not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", optional.get());
        return response;
    }

    // PUT - activateLease
    public Map<String, Object> activateLease(Long leaseId) {
        Optional<LeaseAgreement> optional =
            repo.findById(leaseId);
        if (optional.isEmpty()) {
            throw new LeaseAgreementNotFoundException("Lease not found");
        }
        LeaseAgreement lease = optional.get();
        if (lease.getStatus() !=
            LeaseAgreement.LeaseStatus.D) {
            throw new InvalidStatusTransitionException("Lease not in Draft status");
        }
        lease.setStatus(LeaseAgreement.LeaseStatus.A);
        repo.save(lease);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "Lease activated successfully");
        return response;
    }

    // PUT - renewLease
    public Map<String, Object> renewLease(Long leaseId,Map<String, Object> body) {
        Optional<LeaseAgreement> optional =
            repo.findById(leaseId);
        if (optional.isEmpty()) {
            throw new LeaseAgreementNotFoundException("Lease not found");
        }
        LeaseAgreement lease = optional.get();
        if (lease.getStatus() !=
            LeaseAgreement.LeaseStatus.A) {
            throw new InvalidStatusTransitionException("Lease is not active");
        }
        Double escalation = Double.valueOf(
            body.get("escalationPercent").toString());
        lease.setStatus(LeaseAgreement.LeaseStatus.R);
        lease.setEscalationPercent(escalation);
        repo.save(lease);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "Lease renewed successfully");
        return response;
    }

    // PUT - terminateLease
    public Map<String, Object> terminateLease(Long leaseId,Map<String, String> body) {
        Optional<LeaseAgreement> optional =
            repo.findById(leaseId);
        if (optional.isEmpty()) {
            throw new LeaseAgreementNotFoundException("Lease not found");
        }
        LeaseAgreement lease = optional.get();
        if (lease.getStatus() !=
            LeaseAgreement.LeaseStatus.A) {
            throw new InvalidStatusTransitionException("Lease is not active");
        }
        lease.setStatus(
            LeaseAgreement.LeaseStatus.T);
        repo.save(lease);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "Lease terminated successfully");
        return response;
    }
}