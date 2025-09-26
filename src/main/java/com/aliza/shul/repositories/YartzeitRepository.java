package com.aliza.shul.repositories;

import com.aliza.shul.entities.YartzeitDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aliza.shul.entities.Yartzeit;

import java.util.List;

@Repository
public interface YartzeitRepository extends JpaRepository<Yartzeit, Long> {
    List<Yartzeit> findAllByMemberId(long id);
    List<Yartzeit> deleteAllByMemberId(long id);

    @Query("SELECT new com.aliza.shul.entities.YartzeitDto(m.firstName, m.lastName, y.relationship, y.date.day, y.date.month, y.name) " +
            "FROM Yartzeit y " +
            "JOIN y.member m")
    List<YartzeitDto> findAllYartzeitDtos();
}