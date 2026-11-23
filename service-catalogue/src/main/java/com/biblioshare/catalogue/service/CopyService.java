package com.biblioshare.catalogue.service;

import com.biblioshare.catalogue.entity.Copy;
import com.biblioshare.catalogue.repository.CopyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopyService {

    private final CopyRepository copyRepository;

    public CopyService(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    public List<Copy> getAllCopies() {
        return copyRepository.findAll();
    }

    public Copy getCopyById(Long id) {
        return copyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaire introuvable"));
    }

    public Copy createCopy(Copy copy) {
        return copyRepository.save(copy);
    }

    public Copy updateCopy(Long id, Copy copy) {
        Copy existingCopy = getCopyById(id);

        existingCopy.setBookId(copy.getBookId());
        existingCopy.setCodeExemplaire(copy.getCodeExemplaire());
        existingCopy.setEtat(copy.getEtat());
        existingCopy.setDisponible(copy.isDisponible());

        return copyRepository.save(existingCopy);
    }

    public void deleteCopy(Long id) {
        Copy copy = getCopyById(id);
        copyRepository.delete(copy);
    }
}
