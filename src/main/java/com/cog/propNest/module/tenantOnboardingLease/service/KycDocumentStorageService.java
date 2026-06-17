package com.cog.propNest.module.tenantOnboardingLease.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Handles the physical storage of KYC documents on the server.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Build a deterministic, per-tenant folder path under a configurable
 *       base directory (the folder is created on the Tomcat server at runtime).</li>
 *   <li>Generate a unique {@code documentId} and store the file named after it
 *       so the right document of the respective tenant can always be located.</li>
 *   <li>Check whether a document actually exists on disk for a given reference.</li>
 * </ul>
 *
 * <p>It performs <b>no</b> business validation (type / size / count) — that lives
 * in {@code TenantKycService}. This class only knows about the filesystem.
 */
@Service
public class KycDocumentStorageService {

    @Value("${propnest.kyc.storage.base-dir}")
    private String baseDir;

    /** Relative folder name for a tenant, e.g. {@code tenant_42}. */
    private String tenantFolderName(Long tenantId) {
        return "tenant_" + tenantId;
    }

    /**
     * Generates a unique, human-traceable document id for the given tenant,
     * e.g. {@code KYC-42-3f9a1c8b}.
     */
    public String generateDocumentId(Long tenantId) {
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return "KYC-" + tenantId + "-" + shortUuid;
    }

    /**
     * Resolves the absolute base directory, creating it (and parents) on the
     * server if it does not yet exist.
     */
    private Path ensureBaseDir() {
        Path base = Paths.get(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(base);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Could not create the KYC storage folder at " + base, e);
        }
        return base;
    }

    /**
     * The relative reference stored in the DB ({@code documentRef}), e.g.
     * {@code tenant_42/KYC-42-3f9a1c8b.pdf}. Storing a relative path keeps the
     * record portable across environments; it is resolved against the
     * configured base dir whenever the file is fetched.
     */
    public String buildRelativeRef(Long tenantId, String documentId) {
        return tenantFolderName(tenantId) + "/" + documentId + ".pdf";
    }

    /** Resolves a stored relative reference to an absolute path on disk. */
    public Path resolve(String documentRef) {
        return ensureBaseDir().resolve(documentRef).normalize();
    }

    /**
     * Stores the uploaded file under {@code <base>/tenant_<id>/<documentId>.pdf},
     * creating the per-tenant folder dynamically if needed.
     *
     * @return the relative reference persisted in the DB
     */
    public String store(Long tenantId, String documentId, MultipartFile file) {
        Path base = ensureBaseDir();
        Path tenantDir = base.resolve(tenantFolderName(tenantId));
        try {
            Files.createDirectories(tenantDir);
            Path target = tenantDir.resolve(documentId + ".pdf");
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Failed to store the document on the server for tenant " + tenantId, e);
        }
        return buildRelativeRef(tenantId, documentId);
    }

    /**
     * Returns {@code true} if a real, readable file exists at the given
     * reference. Used to detect the case where a path was recorded but the
     * file is missing on disk.
     */
    public boolean exists(String documentRef) {
        if (documentRef == null || documentRef.isBlank()) {
            return false;
        }
        Path path = resolve(documentRef);
        return Files.exists(path) && Files.isRegularFile(path);
    }
}
