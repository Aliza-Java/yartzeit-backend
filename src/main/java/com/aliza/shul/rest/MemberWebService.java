package com.aliza.shul.rest;

import java.util.List;

import com.aliza.shul.entities.MemberType;
import com.aliza.shul.entities.VerifyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aliza.shul.entities.Member;

import com.aliza.shul.services.MemberService;

@RestController
@RequestMapping("/member")
@CrossOrigin(origins = ("${client.origin}"), allowCredentials = "true")
public class MemberWebService {

    @Autowired
    MemberService memberService; // TODO - if chat agrees its recommended, always return response entities.

    @RequestMapping("{id}")
    public Member getMemberSettings(@PathVariable Long id) {
        try {
            return memberService.getMember(id); // TODO: return response object
        } catch (Exception e) {
            // TODO return response
        }
        return null;
    }

    @PostMapping("/{type}")
    public boolean addMember(@RequestBody Member member, @PathVariable String type) {
        MemberType memberType;
        try {
            // Convert case-insensitive path variable to enum
            memberType = MemberType.valueOf(type.toUpperCase());
            member.setType(memberType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(STR."Invalid member type: \{type}");
        }

        return memberService.saveMember(member, true);
    }

    @PutMapping("/{type}")
    public boolean updateMember(@RequestBody Member member, @PathVariable String type) {
        MemberType memberType;
        try {
            // Convert case-insensitive path variable to enum
            memberType = MemberType.valueOf(type.toUpperCase());
            member.setType(memberType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(STR."Invalid member type: \{type}");
        }
        memberService.saveMember(member, false);
        return true;
    }

    @GetMapping("/{type}")
    public List<Member> getMembersByType(@PathVariable String type) {
        MemberType memberType;
        try {
            // Convert case-insensitive path variable to enum
            memberType = MemberType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(STR."Invalid member type: \{type}");
        }

        return memberService.getMembersByType(memberType);
    }

    //currently not in use (user wants link static - not expirable
    @PostMapping("/generate")
    public String generateLink() {
        return memberService.generateLink();
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody VerifyCode verifyCode) {
        String code = verifyCode.getCode();

        boolean result = memberService.verifyCode(code);

        if (result) {
            return ResponseEntity.ok("Link is valid");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired link.");
        }
    }
}
