package com.biblioshare.catalogue.service;

import com.biblioshare.catalogue.entity.Copy;
import com.biblioshare.catalogue.repository.CopyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CopyService {

    private final CopyRepository copyRepository;
    private final BookService books;

    public CopyService(CopyRepository copyRepository, BookService books) {
        this.copyRepository = copyRepository;
        this.books = books;
    }

    public List<Copy> getAllCopies() {
        return copyRepository.findAll();
    }

    public Copy getCopyById(Long id) {
        return copyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exemplaire introuvable"));
    }

    @Transactional
    public Copy createCopy(Copy copy) {
        books.lockBook(copy.getBookId());
        return copyRepository.save(copy);
    }

    @Transactional
    public Copy updateCopy(Long id, Copy copy) {
        Copy existingCopy = lockCopy(id);
        requireUnassigned(existingCopy);

        books.lockBook(copy.getBookId());
        existingCopy.setBookId(copy.getBookId());
        existingCopy.setCodeExemplaire(copy.getCodeExemplaire());
        existingCopy.setEtat(copy.getEtat());
        existingCopy.setDisponible(copy.isDisponible());

        return copyRepository.save(existingCopy);
    }

    @Transactional
    public void deleteCopy(Long id) {
        Copy copy = lockCopy(id);
        requireUnassigned(copy);
        copyRepository.delete(copy);
    }

    @Transactional
    public Copy claim(Long id, String token) {
        Copy copy = lockCopy(id);
        if (copy.estAttribueA(token)) {
            return copy;
        }
        if (copy.isAttribue() || !copy.isDisponible()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exemplaire indisponible");
        }
        copy.attribuer(token);
        return copy;
    }

    @Transactional
    public void release(Long id, String token) {
        Copy copy = lockCopy(id);
        if (copy.estAttribueA(token)) {
            copy.liberer();
        }
    }

    private Copy lockCopy(Long id) {
        return copyRepository.lockById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Exemplaire introuvable"));
    }

    private void requireUnassigned(Copy copy) {
        if (copy.isAttribue()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exemplaire attribue a un emprunt");
        }
    }
}
