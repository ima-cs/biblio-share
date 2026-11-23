package com.biblioshare.adherents.controller;


import com.biblioshare.adherents.dto.ReservationRequest;
import com.biblioshare.adherents.entity.BookReservation;
import com.biblioshare.adherents.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReservationController {
    private final ReservationService reservations;

    public ReservationController(ReservationService reservations) {
        this.reservations = reservations;
    }

    @GetMapping("/reservations")
    public List<BookReservation> all() {
        return reservations.all();
    }

    @GetMapping("/reservations/{id}")
    public BookReservation get(@PathVariable long id) {
        return reservations.get(id);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public BookReservation create(@Valid @RequestBody ReservationRequest request) {
        return reservations.create(request);
    }

    @PutMapping("/reservations/{id}")
    public BookReservation update(@PathVariable long id, @Valid @RequestBody ReservationRequest request) {
        return reservations.update(id, request);
    }

    @PutMapping("/reservations/{id}/cancel")
    public BookReservation cancel(@PathVariable long id) {
        return reservations.cancel(id);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        reservations.delete(id);
    }

    @PutMapping("/internal/reservations/availability/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void available(@PathVariable UUID token, @RequestParam Long bookId) {
        reservations.available(token, bookId);
    }
}
