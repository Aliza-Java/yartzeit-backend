package com.aliza.shul.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Yartzeit;

@Repository
public interface YartzeitRepository extends JpaRepository<Yartzeit, Long> {
    // Optional: custom query methods like findByEmail, etc.
}