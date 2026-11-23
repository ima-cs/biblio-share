package com.biblioshare.pret.client;


import com.biblioshare.pret.dto.RemoteData.Member;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "service-adherents")
public interface AdherentsClient {
    @GetMapping("/members/{id}")
    Member member(@PathVariable("id") Long id);

    @PutMapping("/internal/members/{id}/fine-suspension")
    void suspend(@PathVariable("id") Long id, @RequestParam("suspended") boolean suspended);

    @PutMapping("/internal/reservations/availability/{token}")
    void available(@PathVariable("token") UUID token, @RequestParam("bookId") Long bookId);
}
