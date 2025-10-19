package com.aliza.shul.rest;

import com.aliza.shul.entities.Member;
import com.aliza.shul.entities.Yartzeit;
import com.aliza.shul.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/access")
public class AccessWebService {

    @Autowired
    MemberService memberService;

    @DeleteMapping("yartzeit/{id}")
    public List<Yartzeit> deleteYartzeitById(@PathVariable Long id) {
        return memberService.deleteYartzeit(id);
    }

    @DeleteMapping("member/{id}")
    public List<Member> deleteMemberById(@PathVariable Long id) {
        return memberService.deleteMember(id);
    }

//    @GetMapping("relatives")
//    public List<Member> getAllRelatives() {
//        return memberService.getAllRelatives();
//    }

    @GetMapping("members")
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

}
