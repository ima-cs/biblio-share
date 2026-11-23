package com.biblioshare.pret.client;


import com.biblioshare.pret.dto.RemoteData.*;
import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class RemoteReads {
    private final CatalogueClient catalogue;
    private final AdherentsClient adherents;
    private final CircuitBreakerRegistry breakers;
    private final RetryRegistry retries;
    private final BulkheadRegistry bulkheads;

    public RemoteReads(CatalogueClient catalogue, AdherentsClient adherents,
                       CircuitBreakerRegistry breakers, RetryRegistry retries, BulkheadRegistry bulkheads) {
        this.catalogue = catalogue;
        this.adherents = adherents;
        this.breakers = breakers;
        this.retries = retries;
        this.bulkheads = bulkheads;
    }

    public Copy copy(Long id) {
        return read("catalogue", () -> catalogue.copy(id));
    }

    public Book book(Long id) {
        return read("catalogue", () -> catalogue.book(id));
    }

    public Member member(Long id) {
        return read("adherents", () -> adherents.member(id));
    }

    private <T> T read(String service, Supplier<T> call) {
        // Treat a domain 404 as a successful transport exchange.
        record Result<T>(T data, FeignException missing) {}
        Supplier<Result<T>> attempt = () -> {
            try {
                return new Result<>(call.get(), null);
            } catch (FeignException.NotFound missing) {
                return new Result<>(null, missing);
            } catch (FeignException failure) {
                throw new RemoteUnavailable(failure);
            }
        };
        Result<T> result = retries.retry(service).executeSupplier(() ->
                bulkheads.bulkhead(service).executeSupplier(() ->
                        breakers.circuitBreaker(service).executeSupplier(attempt)));
        if (result.missing() != null) {
            throw result.missing();
        }
        return result.data();
    }
}
