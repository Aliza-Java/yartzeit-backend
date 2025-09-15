package com.aliza.shul.rest;

import com.aliza.shul.entities.Member;
import com.aliza.shul.entities.Yartzeit;
import com.aliza.shul.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/print")
public class PrintWebService {

    @Autowired
    MemberService memberService;

    @GetMapping("/yartzeits")
    public List<Yartzeit> getYearlyYartzeits() {
        return memberService.getYearlyYartzeits();
    }

    @GetMapping("/parashot")
    public List<Member> getMembersByBmParasha() {
        return memberService.getMemberByBarMitzva();
    }

}
