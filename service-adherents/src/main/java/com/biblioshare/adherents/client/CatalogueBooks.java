package com.biblioshare.adherents.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-catalogue")
public interface CatalogueBooks {
    record BookReference(Long id) {}

    @GetMapping("/books/{id}")
    BookReference book(@PathVariable("id") Long id);
}
