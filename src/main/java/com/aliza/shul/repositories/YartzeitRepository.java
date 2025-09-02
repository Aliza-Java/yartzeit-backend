package com.aliza.shul.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Yartzeit;

import java.util.List;

@Repository
public interface YartzeitRepository extends JpaRepository<Yartzeit, Long> {
    List<Yartzeit> findAllByMemberId(long id);
    List<Yartzeit> deleteAllByMemberId(long id);
}