package com.biblioshare.adherents.dto;


import com.biblioshare.adherents.entity.Member;
import jakarta.validation.constraints.*;

public record MemberRequest(@NotBlank String nom, @NotBlank @Email String email,
                            @NotBlank String type, boolean suspendu) {
    public Member applyTo(Member member) {
        member.nom = nom;
        member.email = email;
        member.type = type;
        member.suspendu = suspendu;
        return member;
    }
}
