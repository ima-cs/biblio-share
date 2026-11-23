package com.biblioshare.pret.controller;


import com.biblioshare.pret.client.RemoteUnavailable;
import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {
    @ExceptionHandler({RemoteUnavailable.class, CallNotPermittedException.class, BulkheadFullException.class})
    public ProblemDetail unavailable() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Verification ou operation distante indisponible");
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail remoteError(FeignException failure) {
        return switch (failure.status()) {
            case 404 -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Ressource distante introuvable");
            case 409 -> ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Operation distante refusee");
            default -> unavailable();
        };
    }
}
