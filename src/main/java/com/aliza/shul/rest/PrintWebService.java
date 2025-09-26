package com.aliza.shul.rest;

import com.aliza.shul.entities.*;
import com.aliza.shul.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/print")
public class PrintWebService {

    @Autowired
    MemberService memberService;

    @GetMapping("/yartzeits")
    public List<YartzeitDto> getYearlyYartzeits() {
        return memberService.getYearlyYartzeits();
    }

    @GetMapping("/parashot")
    public List<BmparashaDto> getMembersByBmParasha() {
        return memberService.getMemberByBarMitzva();
    }

    @GetMapping("/ann")
    public List<AnniversaryDto> getMembersAnniversaries() {
        return memberService.getMembersAnniversaries();
    }

}
