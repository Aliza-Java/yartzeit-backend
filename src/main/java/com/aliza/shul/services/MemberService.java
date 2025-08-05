package com.aliza.shul.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.shul.entities.Member;

import com.aliza.shul.repositories.MemberRepository;

@Service("memberService")
public class MemberService {

	@Autowired
	MemberRepository memberRepository;

	public Member getMember(Long id) throws Exception {
		Optional<Member> optionalMember = memberRepository.findById(id);
		if (optionalMember.isEmpty())
			throw new Exception(String.format("Member with id %s not found", id));

		return optionalMember.get();
	}
	
	public boolean addMember(Member member) {

		memberChecks(member);

		Member relative = member.getRelative();

		if (relative != null) {
			memberChecks(relative);
			relative = memberRepository.save(relative);
		}

		Member mainMember = memberRepository.save(member);

		if (relative != null) {

			Member existingMember = memberRepository.findById(mainMember.getId()).orElseThrow();
			relative.setMainMemberId(existingMember.getId());
			memberRepository.save(relative);
		}

		return true;
	}
	
	public boolean editMember(Member member) {
		memberRepository.save(member);
		return true;

	}
	
	public List<Member> getAllMembers(){
		return memberRepository.findAll();
	}

	private void memberChecks(Member member) {
		if (member.getAnniversary() == null || member.getAnniversary().isNull() || member.getAnniversary().isEmpty())
			if (member.getSpouse() == null || member.getSpouse().isEmpty()) {
				System.out.println("Spouse name cannot be empty.  Anniversary date deleted.");
				member.setAnniversary(null);
			}
	}

}
