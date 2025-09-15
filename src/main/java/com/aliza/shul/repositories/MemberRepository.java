package com.aliza.shul.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Member;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByMainMemberIdLessThan(Long value); //finding main members

    @Query("SELECT m FROM Member m WHERE m.bmparasha IS NOT NULL AND m.bmparasha <> ''")
    List<Member> findAllWithBmParasha();
}