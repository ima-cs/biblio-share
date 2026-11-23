package com.biblioshare.pret.controller;


import com.biblioshare.pret.dto.*;
import com.biblioshare.pret.entity.*;
import com.biblioshare.pret.service.LoanService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoanController {
    private final LoanService loans;

    public LoanController(LoanService loans) {
        this.loans = loans;
    }

    @PostMapping("/loans")
    public ResponseEntity<Loan> create(@RequestHeader("Idempotency-Key") String key,
                                       @Valid @RequestBody LoanRequest request) {
        LoanService.Creation result = loans.create(key, request);
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(result.loan());
    }

    @GetMapping("/loans")
    public List<Loan> all() {
        return loans.all();
    }

    @GetMapping("/loans/{id}")
    public Loan get(@PathVariable long id) {
        return loans.get(id);
    }

    @GetMapping("/loans/{id}/details")
    public LoanDetails details(@PathVariable long id) {
        return loans.details(id);
    }

    @PutMapping("/loans/{id}/return")
    public Loan returnCopy(@PathVariable long id) {
        return loans.returnCopy(id);
    }

    @PutMapping("/loans/{id}/cancel")
    public Loan cancel(@PathVariable long id) {
        return loans.cancel(id);
    }

    @PutMapping("/loans/{id}/lost")
    public Loan lost(@PathVariable long id) {
        return loans.markLost(id);
    }

    @PostMapping("/loans/detect-overdue")
    public Map<String, Integer> detectLateLoans() {
        return Map.of("retardsDetectes", loans.detectLateLoans());
    }

    @GetMapping("/fines")
    public List<Fine> fines() {
        return loans.fines();
    }

    @PutMapping("/fines/{id}/pay")
    public Fine pay(@PathVariable long id) {
        return loans.pay(id);
    }
}
