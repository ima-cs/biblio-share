package com.biblioshare.catalogue.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.biblioshare.catalogue.entity.Book;
import com.biblioshare.catalogue.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioshare.catalogue.repository.CopyRepository;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    private final CopyRepository copies;

    public BookService(BookRepository bookRepository, CopyRepository copies) {
        this.bookRepository = bookRepository;
        this.copies = copies;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livre introuvable"
                ));
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book book) {
        Book existingBook = getBookById(id);

        existingBook.setTitre(book.getTitre());
        existingBook.setAuteur(book.getAuteur());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setCategorie(book.getCategorie());

        return bookRepository.save(existingBook);
    }

    Book lockBook(Long id) {
        return bookRepository.lockById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Livre introuvable"));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = lockBook(id);
        if (copies.existsByBookId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le livre contient des exemplaires");
        }
        bookRepository.delete(book);
    }
}
