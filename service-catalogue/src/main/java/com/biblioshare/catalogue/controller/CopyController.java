
package com.biblioshare.catalogue.controller;

import com.biblioshare.catalogue.entity.Copy;
import com.biblioshare.catalogue.service.CopyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/copies")
public class CopyController {

    private final CopyService copyService;

    public CopyController(CopyService copyService) {
        this.copyService = copyService;
    }

    @GetMapping
    public List<Copy> getAllCopies() {
        return copyService.getAllCopies();
    }

    @GetMapping("/{id}")
    public Copy getCopyById(@PathVariable Long id) {
        return copyService.getCopyById(id);
    }

    @PostMapping
    public ResponseEntity<Copy> createCopy(@RequestBody Copy copy) {
        Copy created = copyService.createCopy(copy);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Copy updateCopy(@PathVariable Long id, @RequestBody Copy copy) {
        return copyService.updateCopy(id, copy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCopy(@PathVariable Long id) {
        copyService.deleteCopy(id);
        return ResponseEntity.noContent().build();
    }
}
