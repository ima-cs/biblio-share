package com.biblioshare.catalogue;


import com.biblioshare.catalogue.entity.Book;
import com.biblioshare.catalogue.entity.Copy;
import com.biblioshare.catalogue.service.BookService;
import com.biblioshare.catalogue.service.CopyService;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceCatalogueApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired BookService books;
    @Autowired CopyService copies;

    @Test
    void validatesInputsAndReportsMissingCopies() throws Exception {
        mvc.perform(post("/books").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/copies/999999")).andExpect(status().isNotFound());
    }

    @Test
    void onlyOneConcurrentClaimSucceedsAndOnlyItsOwnerCanRelease() throws Exception {
        Book book = books.createBook(new Book());
        Copy copy = copies.createCopy(new Copy(book.getId(), "EX-1", "BON", true));
        String first = UUID.randomUUID().toString();
        String second = UUID.randomUUID().toString();
        CountDownLatch start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> { start.await(); return claim(copy.getId(), first); });
            var b = pool.submit(() -> { start.await(); return claim(copy.getId(), second); });
            start.countDown();
            assertThat(java.util.List.of(a.get(), b.get())).containsExactlyInAnyOrder(true, false);
            String owner = a.get() ? first : second;
            copies.release(copy.getId(), a.get() ? second : first);
            assertThat(copies.getCopyById(copy.getId()).isDisponible()).isFalse();
            assertThat(copies.claim(copy.getId(), owner).isDisponible()).isFalse();
            copies.release(copy.getId(), owner);
            assertThat(copies.getCopyById(copy.getId()).isDisponible()).isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    private boolean claim(Long id, String token) {
        try {
            copies.claim(id, token);
            return true;
        } catch (ResponseStatusException failure) {
            assertThat(failure.getStatusCode().value()).isEqualTo(409);
            return false;
        }
    }
}
