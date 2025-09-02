package com.aliza.shul.services;

import java.util.List;
import java.util.Optional;

import com.aliza.shul.entities.Yartzeit;
import com.aliza.shul.repositories.YartzeitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliza.shul.entities.Member;

import com.aliza.shul.repositories.MemberRepository;

@Service("memberService")
public class MemberService {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	YartzeitRepository yartzeitRepository;

	public Member getMember(Long id) throws Exception {
		Optional<Member> optionalMember = memberRepository.findById(id);
		if (optionalMember.isEmpty())
			throw new Exception(String.format("Member with id %s not found", id));

		return optionalMember.get();
	}
	
	public boolean addMember(Member member) {

		Member relative = member.getRelative();

		if (relative != null) {
			relative = memberRepository.save(relative);
            member.setRelative(relative);
		}

		member.getYartzeits().forEach(y -> y.setMember(member));

		Member mainMember = memberRepository.save(member);

		if (relative != null) {
			Member existingMember = memberRepository.findById(mainMember.getId()).orElseThrow();
			relative.setMainMemberId(existingMember.getId());
			memberRepository.save(relative);
		}

		return true;
	}
	
	public boolean editMember(Member member) {
		yartzeitRepository.deleteAllByMemberId(member.getId());

		member.getYartzeits().forEach(y -> y.setMember(member));
		memberRepository.save(member);
		return true;
	}
	
	public List<Member> getAllMembers(){
		return memberRepository.findByMainMemberIdLessThan(1L);
	} //i.e. all 0's = not secondary members
}
