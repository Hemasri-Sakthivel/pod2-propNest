package com.cog.propNest.module.identityAccessManagement.exception;

import com.cog.propNest.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates every {@link IamException} into the shared {@link ErrorResponse}
 * body using the status carried by the exception. Ordered ahead of the generic
 * {@code GlobalExceptionHandler} so module exceptions are handled here first.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IamExceptionHandler {

    @ExceptionHandler(IamException.class)
    public ResponseEntity<ErrorResponse> handleIamException(IamException ex,
                                                            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                ex.getStatus().value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }
}
