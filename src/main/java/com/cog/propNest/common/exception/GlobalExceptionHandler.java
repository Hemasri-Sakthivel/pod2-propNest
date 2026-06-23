package com.cog.propNest.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.cog.propNest.common.response.ErrorResponse;
import com.cog.propNest.module.tenantOnboardingLease.exception.DocumentStorageException;
import com.cog.propNest.module.tenantOnboardingLease.exception.FileSizeLimitExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidDocumentException;
import com.cog.propNest.module.tenantOnboardingLease.exception.InvalidRequestException;
import com.cog.propNest.module.tenantOnboardingLease.exception.KycNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.LeaseAgreementNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.StorageCapacityExceededException;
import com.cog.propNest.module.tenantOnboardingLease.exception.TenantApplicationNotFoundException;
import com.cog.propNest.module.tenantOnboardingLease.exception.UploadLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Catches exceptions thrown across all modules and maps them to a consistent
 * {@link ErrorResponse} body with the appropriate HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex,HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadTooLarge(MaxUploadSizeExceededException ex,HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
            "The uploaded document exceeds the maximum allowed size.", request);
    }

    // ── Tenant Onboarding & Lease module exceptions ──────────────────────

    @ExceptionHandler({KycNotFoundException.class,
                       TenantApplicationNotFoundException.class,
                       LeaseAgreementNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleModuleNotFound(RuntimeException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidRequestException.class,
                       InvalidDocumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(FileSizeLimitExceededException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, ex.getMessage(), request);
    }

    @ExceptionHandler({UploadLimitExceededException.class,
                       com.cog.propNest.module.tenantOnboardingLease.exception.InvalidStatusTransitionException.class})
    public ResponseEntity<ErrorResponse> handleModuleConflict(RuntimeException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(StorageCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleStorageFull(StorageCapacityExceededException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.INSUFFICIENT_STORAGE, ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentStorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageFailure(DocumentStorageException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
