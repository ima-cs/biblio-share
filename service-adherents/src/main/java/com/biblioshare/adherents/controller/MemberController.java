package com.biblioshare.adherents.controller;


import com.biblioshare.adherents.dto.MemberRequest;
import com.biblioshare.adherents.entity.Member;
import com.biblioshare.adherents.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService members;

    public MemberController(MemberService members) {
        this.members = members;
    }

    @GetMapping
    public List<Member> all() {
        return members.all();
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable long id) {
        return members.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Member create(@Valid @RequestBody MemberRequest request) {
        return members.create(request);
    }

    @PutMapping("/{id}")
    public Member update(@PathVariable long id, @Valid @RequestBody MemberRequest request) {
        return members.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        members.delete(id);
    }

}
