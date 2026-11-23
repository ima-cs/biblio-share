package com.biblioshare.adherents;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberApiTests {
    @Autowired MockMvc mvc;

    @Test
    void validatesEmailAndReportsMissingMember() throws Exception {
        mvc.perform(post("/members").contentType("application/json")
                        .content("""
                                {"nom":"Salma","email":"invalide","type":"ETUDIANT"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/members/999999")).andExpect(status().isNotFound());
    }
}
