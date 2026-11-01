package com.jobtrack.api.error;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;

import com.jobtrack.application.service.ApiExceptions;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiExceptions.NotFoundException.class)
    ProblemDetail notFound(ApiExceptions.NotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ApiExceptions.ForbiddenException.class)
    ProblemDetail forbidden(ApiExceptions.ForbiddenException exception) {
        return problem(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(ApiExceptions.ConflictException.class)
    ProblemDetail conflict(ApiExceptions.ConflictException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ApiExceptions.UnauthorizedException.class)
    ProblemDetail unauthorized(ApiExceptions.UnauthorizedException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(ApiExceptions.QuotaExceededException.class)
    ProblemDetail quota(ApiExceptions.QuotaExceededException exception) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
    }

    @ExceptionHandler(ApiExceptions.BadRequestException.class)
    ProblemDetail badRequest(ApiExceptions.BadRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail dataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Violation de contrainte en base", exception);
        return problem(HttpStatus.CONFLICT,
                "Cette opération entre en conflit avec une donnée existante.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "La requête est invalide.");
        List<FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        detail.setProperty("errors", errors);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraint(ConstraintViolationException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "La requête est invalide.");
        detail.setProperty("errors", exception.getConstraintViolations().stream()
                .map(violation -> new FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList());
        return detail;
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    ProblemDetail invalid(RuntimeException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Requête invalide : " + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        log.error("Erreur non gérée", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.");
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }

    public record FieldError(String field, String message) {}
}
