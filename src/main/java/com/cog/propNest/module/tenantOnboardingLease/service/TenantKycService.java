

// package com.cog.propNest.module.tenantOnboardingLease.service;

// import com.cog.propNest.module.tenantOnboardingLease.dto.TenantKycDTO;
// import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
// import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import java.time.LocalDate;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// @Service
// public class TenantKycService {

//     @Autowired
//     private TenantKycRepository repo;

//     // POST - createKyc
//     public Map<String, Object> createKyc(
//                                TenantKycDTO dto) {
//         if (dto.getTenantId() == null) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "tenantId is required");
//             return error;
//         }
//         if (dto.getDocumentType() == null ||
//             dto.getDocumentType().isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "documentType is required");
//             return error;
//         }
//         try {
//             TenantKyc.DocumentType.valueOf(
//                 dto.getDocumentType());
//         } catch (IllegalArgumentException e) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message",
//                 "Invalid documentType. Allowed: " +
//                 "NationalID, Passport, " +
//                 "EmploymentLetter, BankStatement");
//             return error;
//         }
//         if (dto.getDocumentRef() == null ||
//             dto.getDocumentRef().isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "documentRef is required");
//             return error;
//         }
//         TenantKyc kyc = new TenantKyc();
//         kyc.setTenantId(dto.getTenantId());
//         kyc.setDocumentType(
//             TenantKyc.DocumentType.valueOf(
//                 dto.getDocumentType()));
//         kyc.setDocumentRef(dto.getDocumentRef());
//         kyc.setStatus(TenantKyc.KycStatus.Pending);
//         repo.save(kyc);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "KYC document uploaded successfully");
//         return response;
//     }

//     // GET - getAllKyc
//     public Map<String, Object> getAllKyc(String status) {
//         List<TenantKyc> list;
//         if (status != null && !status.isEmpty()) {
//             list = repo.findByStatus(
//                 TenantKyc.KycStatus.valueOf(status));
//         } else {
//             list = repo.findAll();
//         }
//         if (list.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "No KYC records found");
//             return error;
//         }
//         Map<String, Object> response = new HashMap<>();
//         // response.put("message",
//         //     "KYC records retrieved successfully");
//         response.put("data", list);
//         response.put("totalElements", list.size());
//         return response;
//     }

//     // GET - getKycById
//     public Map<String, Object> getKycById(Long kycId) {
//         Optional<TenantKyc> optional =
//             repo.findById(kycId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "KYC not found");
//             return error;
//         }
//         Map<String, Object> response = new HashMap<>();
//         // response.put("message",
//         //     "KYC record retrieved successfully");
//         response.put("data", optional.get());
//         return response;
//     }
//     // PUT - verifyKyc
//     public Map<String, Object> verifyKyc(Long kycId,Map<String, String> body) {
//         Optional<TenantKyc> optional =
//             repo.findById(kycId);
//         if (optional.isEmpty()) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message", "KYC not found");
//             return error;
//         }
//         TenantKyc kyc = optional.get();
//         String newStatus = body.get("status");

//         if (kyc.getStatus() !=
//             TenantKyc.KycStatus.Pending) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message",
//                 "Status transition not allowed");
//             return error;
//         }
//         if (!newStatus.equals("Verified") &&
//             !newStatus.equals("Rejected")) {
//             Map<String, Object> error = new HashMap<>();
//             error.put("message",
//                 "Invalid status. Use Verified or Rejected");
//             return error;
//         }

//         kyc.setStatus(
//             TenantKyc.KycStatus.valueOf(newStatus));
//         if (newStatus.equals("Verified")) {
//             kyc.setCerifiedDate(LocalDate.now());
//         }
//         repo.save(kyc);

//         Map<String, Object> response = new HashMap<>();
//         response.put("message",
//             "KYC verified successfully");
//         if (newStatus.equals("Verified")) {
//             response.put("cerifiedDate",
//                 LocalDate.now().toString());
//         }
//         return response;
//     }
// }

package com.cog.propNest.module.tenantOnboardingLease.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cog.propNest.module.tenantOnboardingLease.dto.TenantKycDTO;
import com.cog.propNest.module.tenantOnboardingLease.entity.TenantKyc;
import com.cog.propNest.module.tenantOnboardingLease.exception.DocumentStorageException;
import com.cog.propNest.module.tenantOnboardingLease.exception.FileSizeLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidDocumentException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.StorageCapacityExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.UploadLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.repository.TenantKycRepository;

@Service
public class TenantKycService {

    private static final Logger log =
        LoggerFactory.getLogger(TenantKycService.class);

    @Autowired
    private TenantKycRepository repo;

    @Autowired
    private KycDocumentStorageService storage;

    @Value("${propnest.kyc.max-file-size-bytes}")
    private long maxFileSizeBytes;

    @Value("${propnest.kyc.max-documents-per-tenant}")
    private long maxDocumentsPerTenant;

    @Value("${propnest.kyc.max-total-capacity-bytes}")
    private long maxTotalCapacityBytes;

    // POST - createKyc
    public Map<String, Object> createKyc(TenantKycDTO dto) {
        if (dto.getTenantId() == null) {
            throw new InvalidRequestException("tenantId is required");
        }
        if (dto.getDocumentType() == null ||
            dto.getDocumentType().isEmpty()) {
            throw new InvalidRequestException("documentType is required");
        }
        try {
            TenantKyc.DocumentType.valueOf(
                dto.getDocumentType());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(
                "Invalid documentType. Allowed: " +
                "NationalID, Passport, " +
                "EmploymentLetter, BankStatement");
        }
        if (dto.getDocumentRef() == null ||
            dto.getDocumentRef().isEmpty()) {
            throw new InvalidRequestException("documentRef is required");
        }
        TenantKyc kyc = new TenantKyc();
        kyc.setTenantId(dto.getTenantId());
        kyc.setDocumentType(
            TenantKyc.DocumentType.valueOf(
                dto.getDocumentType()));
        kyc.setDocumentRef(dto.getDocumentRef());
        kyc.setStatus(TenantKyc.KycStatus.P);
        repo.save(kyc);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
            "KYC document uploaded successfully");
        return response;
    }

    // GET - getAllKyc
    public Map<String, Object> getAllKyc(String status) {
        List<TenantKyc> list;
        if (status != null && !status.isEmpty()) {
            try {
                list = repo.findByStatus(
                    TenantKyc.KycStatus.valueOf(status));
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException(
                    "Invalid status. Use P, V or R");
            }
        } else {
            list = repo.findAll();
        }
        if (list.isEmpty()) {
            throw new KycNotFoundException("No KYC records found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", list);
        response.put("totalElements", list.size());
        return response;
    }

    // GET - getKycById
    public Map<String, Object> getKycById(Long kycId) {
        Optional<TenantKyc> optional =
            repo.findById(kycId);
        if (optional.isEmpty()) {
            throw new KycNotFoundException("KYC not found");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", optional.get());
        return response;
    }

    // PUT - verifyKyc
    public Map<String, Object> verifyKyc(Long kycId,Map<String, String> body) {
        Optional<TenantKyc> optional =
            repo.findById(kycId);
        if (optional.isEmpty()) {
            throw new KycNotFoundException("KYC not found");
        }
        TenantKyc kyc = optional.get();
        String newStatus = body.get("status");

        if (kyc.getStatus() !=
            TenantKyc.KycStatus.P) {
            throw new InvalidStatusTransitionException(
                "Status transition not allowed");
        }
        if (newStatus == null ||
            (!newStatus.equals("V") && !newStatus.equals("R"))) {
            throw new InvalidRequestException(
                "Invalid status. Use V or R");
        }
        kyc.setStatus(
            TenantKyc.KycStatus.valueOf(newStatus));

        // cerifiedDate saved to DB but NOT shown in response
        if (newStatus.equals("V")) {
            kyc.setCerifiedDate(LocalDate.now());
        }
        repo.save(kyc);
        log.info("KYC {} status updated to {}", kycId, newStatus);

        // Return only message — no cerifiedDate
        Map<String, Object> response = new HashMap<>();
        response.put("message", "KYC verified successfully");
        return response;
    }

    // POST - uploadDocument
    // Accepts the actual PDF file, validates it, stores it on the server under
    // a dynamically built per-tenant path, and records the documentId against
    // the tenantId in the DB. Every failure returns a clear message for the user.
    public Map<String, Object> uploadDocument(Long tenantId,String documentType,MultipartFile file) {
        // --- basic field checks ---
        if (tenantId == null) {
            throw new InvalidRequestException("tenantId is required");
        }
        if (documentType == null || documentType.isEmpty()) {
            throw new InvalidRequestException("documentType is required");
        }
        try {
            TenantKyc.DocumentType.valueOf(documentType);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid documentType. Allowed: "
                + "NationalID, Passport, EmploymentLetter, BankStatement");
        }

        // --- is a document actually present? ---
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException(
                "No document was uploaded. Please attach a file.");
        }

        // --- allow PDF only ---
        if (!isPdf(file)) {
            throw new InvalidDocumentException("Only PDF documents are allowed. "
                + "Please upload a file in .pdf format.");
        }

        // --- single-file size limit ---
        if (file.getSize() > maxFileSizeBytes) {
            throw new FileSizeLimitExceededException("Document is too large ("
                + toMb(file.getSize()) + " MB). Maximum allowed size is "
                + toMb(maxFileSizeBytes) + " MB.");
        }

        // --- per-tenant document count limit ---
        long existingCount = repo.countByTenantId(tenantId);
        if (existingCount >= maxDocumentsPerTenant) {
            throw new UploadLimitExceededException(
                "Upload limit reached. You can upload at most "
                + maxDocumentsPerTenant + " documents.");
        }

        // --- portal-wide storage capacity limit ---
        long currentTotal = repo.sumTotalFileSize();
        if (currentTotal + file.getSize() > maxTotalCapacityBytes) {
            throw new StorageCapacityExceededException(
                "The portal storage capacity has been reached. "
                + "Your document could not be stored. Please contact support.");
        }

        // --- store on disk under a dynamic, documentId-based path ---
        String documentId = storage.generateDocumentId(tenantId);
        String documentRef;
        try {
            documentRef = storage.store(tenantId, documentId, file);
        } catch (RuntimeException e) {
            throw new DocumentStorageException(
                "Could not save the document on the server. "
                + "Please try again later.", e);
        }

        // --- persist the record (documentId kept against tenantId) ---
        TenantKyc kyc = new TenantKyc();
        kyc.setTenantId(tenantId);
        kyc.setDocumentId(documentId);
        kyc.setDocumentType(TenantKyc.DocumentType.valueOf(documentType));
        kyc.setDocumentRef(documentRef);
        kyc.setFileSize(file.getSize());
        kyc.setStatus(TenantKyc.KycStatus.P);
        repo.save(kyc);
        log.info("KYC document stored (documentId={}, tenantId={}, size={} bytes)",
            documentId, tenantId, file.getSize());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "KYC document uploaded successfully");
        response.put("documentId", documentId);
        response.put("documentRef", documentRef);
        return response;
    }

    // GET - verifyDocument
    // Confirms that the file recorded for a KYC entry is actually present on
    // disk. Covers the case where a documentRef path was saved (e.g. typed in
    // during createKyc) but the file was never placed at that path.
    public Map<String, Object> verifyDocument(Long kycId) {
        Optional<TenantKyc> optional = repo.findById(kycId);
        if (optional.isEmpty()) {
            throw new KycNotFoundException("KYC not found");
        }
        TenantKyc kyc = optional.get();

        if (storage.exists(kyc.getDocumentRef())) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document is available at the referenced path.");
            response.put("documentId", kyc.getDocumentId());
            response.put("documentRef", kyc.getDocumentRef());
            response.put("available", true);
            return response;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "The path '" + kyc.getDocumentRef()
            + "' is recorded, but no file was found there. "
            + "Please make sure the document has been placed at this path.");
        response.put("documentRef", kyc.getDocumentRef());
        response.put("available", false);
        return response;
    }

    // ── helpers ───────────────────────────────────────
    // A file is accepted only if its name, declared type AND real content all
    // say PDF. The content (magic-byte) check stops a non-PDF being slipped
    // through by simply renaming it to ".pdf".
    private boolean isPdf(MultipartFile file) {
        String name = file.getOriginalFilename();
        boolean extOk = name != null && name.toLowerCase().endsWith(".pdf");
        String type = file.getContentType();
        boolean typeOk = type != null && type.equalsIgnoreCase("application/pdf");
        return extOk && typeOk && hasPdfSignature(file);
    }

    // Every real PDF begins with the bytes "%PDF-".
    private boolean hasPdfSignature(MultipartFile file) {
        try {
            byte[] header = new byte[5];
            int read;
            try (var in = file.getInputStream()) {
                read = in.read(header);
            }
            return read == 5
                && header[0] == '%' && header[1] == 'P' && header[2] == 'D'
                && header[3] == 'F' && header[4] == '-';
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private String toMb(long bytes) {
        return String.format("%.2f", bytes / (1024.0 * 1024.0));
    }
}