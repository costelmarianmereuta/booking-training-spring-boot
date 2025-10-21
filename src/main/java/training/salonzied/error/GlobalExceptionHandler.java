package training.salonzied.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Domeniu ---
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail notFound(EntityNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), ErrorCode.ENTITY_NOT_FOUND, req);
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), ErrorCode.CONFLICT, req);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail unprocessable(BusinessRuleException ex, HttpServletRequest req) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ErrorCode.UNPROCESSABLE_ENTITY, req);
    }

    // --- 400 (validări / input tehnic) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail badRequestBody(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST,
                "Validation failed for request body", ErrorCode.VALIDATION_ERROR, req);
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError f : ex.getBindingResult().getFieldErrors()) {
            errors.put(f.getField(), f.getDefaultMessage());
        }
        p.setProperty("errors", errors);
        return p;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail badRequestParams(ConstraintViolationException ex, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.BAD_REQUEST,
                "Constraint violations in request", ErrorCode.CONSTRAINT_VIOLATION, req);
        p.setProperty("violations", ex.getConstraintViolations().stream()
                .map(v -> Map.of("property", v.getPropertyPath().toString(), "message", v.getMessage()))
                .toList());
        return p;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST,
                "Malformed or unreadable JSON payload", ErrorCode.BAD_REQUEST, req);
    }

    // --- HTTP framework ---
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not allowed", ErrorCode.METHOD_NOT_ALLOWED, req);
        p.setProperty("supportedMethods", ex.getSupportedHttpMethods());
        return p;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail unsupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        ProblemDetail p = problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type", ErrorCode.UNSUPPORTED_MEDIA_TYPE, req);
        p.setProperty("supported", ex.getSupportedMediaTypes());
        return p;
    }

    // --- 500 ---
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail dataAccess(DataAccessException ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "A data access error occurred", ErrorCode.DATA_ACCESS_ERROR, req);
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail unexpected(Throwable ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                ErrorCode.UNEXPECTED_ERROR, req);
    }

    // --- util comun ---
    private ProblemDetail problem(HttpStatus status, String detail, ErrorCode code, HttpServletRequest req) {
        ProblemDetail p = ProblemDetail.forStatus(status);
        p.setTitle(status.getReasonPhrase());
        p.setDetail(detail);

        // RFC 7807 fields
        p.setType(URI.create("about:blank"));
        p.setInstance(URI.create(req.getRequestURI()));

        // extra properties
        p.setProperty("errorCode", code.name());
        p.setProperty("timestamp", OffsetDateTime.now().toString());
        // dacă vrei, poți renunța la 'path' (e redundant cu 'instance')
        p.setProperty("path", req.getRequestURI());

        return p; // Spring setează automat application/problem+json
    }
}
