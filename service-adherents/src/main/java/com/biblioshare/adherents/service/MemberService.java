package com.biblioshare.adherents.service;


import com.biblioshare.adherents.dto.MemberRequest;
import com.biblioshare.adherents.entity.Member;
import com.biblioshare.adherents.repository.MemberRepository;
import com.biblioshare.adherents.repository.ReservationRepository;
import com.biblioshare.adherents.entity.BookReservation;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class MemberService {
    private final MemberRepository members;

    private final ReservationRepository reservations;

    public MemberService(MemberRepository members, ReservationRepository reservations) {
        this.members = members;
        this.reservations = reservations;
    }

    public List<Member> all() {
        return members.findAll();
    }

    public Member get(long id) {
        return members.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Adherent introuvable"));
    }

    public Member create(MemberRequest request) {
        return members.save(request.applyTo(new Member()));
    }

    public Member update(long id, MemberRequest request) {
        return request.applyTo(get(id));
    }

    public void delete(long id) {
        Member member = get(id);
        if (reservations.existsByMemberIdAndStatut(id, BookReservation.Status.EN_ATTENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Adherent avec reservation en attente");
        }
        members.delete(member);
    }

    public void suspendForFines(long id, boolean suspended) {
        get(id).suspendrePourAmendes(suspended);
    }
}
