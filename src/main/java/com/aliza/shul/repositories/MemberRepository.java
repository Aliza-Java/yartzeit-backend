package com.aliza.shul.repositories;

import com.aliza.shul.entities.AnniversaryDto;
import com.aliza.shul.entities.BmparashaDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Member;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
//    List<Member> findByMainMemberIdLessThan(Long value); //finding main members
//
//    List<Member> findByMainMemberIdGreaterThan(Long value); //finding relatives

    @Query("SELECT new com.aliza.shul.entities.BmparashaDto(m.firstName, m.lastName, m.bmparasha, m.hebrewName, m.fatherName) " +
            "FROM Member m WHERE m.bmparasha IS NOT NULL AND m.bmparasha <> ''")
    List<BmparashaDto> findAllWithBmParasha();

    @Query("SELECT new com.aliza.shul.entities.AnniversaryDto(m.firstName, m.lastName, m.anniversary.day, m.anniversary.month, spouse) FROM Member m WHERE m.anniversary.day > 0 and m.anniversary.month is not null and m.anniversary.month <> ''")
    List<AnniversaryDto> findAllWithAnniversary();
}