package com.aliza.shul.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}