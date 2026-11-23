package com.biblioshare.catalogue.controller;


import com.biblioshare.catalogue.entity.Copy;
import com.biblioshare.catalogue.service.CopyService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/copies/{id}/claims/{token}")
public class CopyAllocationController {
    private final CopyService copies;

    public CopyAllocationController(CopyService copies) {
        this.copies = copies;
    }

    @PutMapping
    public Copy claim(@PathVariable Long id, @PathVariable UUID token) {
        return copies.claim(id, token.toString());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable Long id, @PathVariable UUID token) {
        copies.release(id, token.toString());
    }
}
