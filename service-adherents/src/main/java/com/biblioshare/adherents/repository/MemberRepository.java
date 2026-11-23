package com.biblioshare.adherents.repository;


import com.biblioshare.adherents.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
