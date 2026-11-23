package com.biblioshare.pret.client;


import com.biblioshare.pret.dto.RemoteData.*;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "service-catalogue")
public interface CatalogueClient {
    @GetMapping("/copies/{id}")
    Copy copy(@PathVariable("id") Long id);

    @GetMapping("/books/{id}")
    Book book(@PathVariable("id") Long id);

    @PutMapping("/internal/copies/{id}/claims/{token}")
    Copy claim(@PathVariable("id") Long id, @PathVariable("token") UUID token);

    @DeleteMapping("/internal/copies/{id}/claims/{token}")
    void release(@PathVariable("id") Long id, @PathVariable("token") UUID token);
}
