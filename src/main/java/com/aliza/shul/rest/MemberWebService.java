package com.aliza.shul.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aliza.shul.entities.Member;

import com.aliza.shul.services.MemberService;

@RestController
@RequestMapping("member")
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

	@PostMapping
	public boolean addMember(@RequestBody Member member) {
		return memberService.addMember(member);
	}

	@PutMapping
	public boolean updateMember(@RequestBody Member member) {
		memberService.editMember(member);
		return true;
	}
	
	@RequestMapping
	public List<Member> getAllMembers() {
		return memberService.getAllMembers();
	}

}
