package com.biblioshare.adherents.controller;


import com.biblioshare.adherents.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class FineSuspensionController {
    private final MemberService members;

    public FineSuspensionController(MemberService members) {
        this.members = members;
    }

    @PutMapping("/internal/members/{id}/fine-suspension")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable long id, @RequestParam boolean suspended) {
        members.suspendForFines(id, suspended);
    }
}
