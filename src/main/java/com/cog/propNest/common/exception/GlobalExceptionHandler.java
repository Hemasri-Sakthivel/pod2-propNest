package com.cog.propNest.common.exception;

import com.cog.propNest.common.response.ErrorResponse;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicatePropertyException;
import com.cog.propNest.module.propertyListingPortfolio.exception.DuplicateUnitException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidPropertyDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.InvalidUnitDataException;
import com.cog.propNest.module.propertyListingPortfolio.exception.PropertyNotFoundException;
import com.cog.propNest.module.propertyListingPortfolio.exception.UnitNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // ── propertyListingPortfolio module exceptions ────────────────────────

    @ExceptionHandler({PropertyNotFoundException.class, UnitNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleModuleNotFound(RuntimeException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({DuplicatePropertyException.class, DuplicateUnitException.class,
            com.cog.propNest.module.propertyListingPortfolio.exception
                    .InvalidStatusTransitionException.class})
    public ResponseEntity<ErrorResponse> handleModuleConflict(RuntimeException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidPropertyDataException.class, InvalidUnitDataException.class})
    public ResponseEntity<ErrorResponse> handleModuleBadRequest(RuntimeException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(status.value(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
