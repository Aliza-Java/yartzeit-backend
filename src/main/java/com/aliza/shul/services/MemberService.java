package com.aliza.shul.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.aliza.shul.repositories.YartzeitRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliza.shul.entities.Member;

import com.aliza.shul.repositories.MemberRepository;

@Service("memberService")
public class MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    YartzeitRepository yartzeitRepository;

    @Value("${full.client}")
    private String fullClient;

    public Member getMember(Long id) throws Exception {
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isEmpty()) throw new Exception(String.format("Member with id %s not found", id));

        return optionalMember.get();
    }

    @Transactional
    public boolean saveMember(Member member, boolean newMember) {
        //get original member if in DB
        boolean originalHasRelative = false;
        Optional<Member> optionalMember = memberRepository.findById(member.getId());
        if (optionalMember.isPresent()) {
            originalHasRelative = optionalMember.get().getRelative() != null;
        }

        System.out.println("So far member is: " + member);

        //first save relative and yartzeits (relationships)
        Member relative = member.getRelative();

        if (relative != null) {
            System.out.println("relative is: " + relative);
            relative = memberRepository.save(relative);
            member.setRelative(relative); //two-way relationship - first side of member-relative relationship
        }

        if (!newMember) {
            yartzeitRepository.deleteAllByMemberId(member.getId()); //if any chance that editing yartzeits, start fresh
            System.out.println("deleted existing yartzeits for member with id " + member.getId());
        }

        //this will only happen in edited member
        member.getYartzeits().forEach(y -> {
            y.setMember(member); //oneToMany so only need to save on yartzeit's side
            y.setId(null);// if id exists from earlier, look like new so that all persisted
        });

        //now can save main member without worries of detached entities
        Member mainMember = memberRepository.save(member);
        System.out.println("mainMember is: " + mainMember);

        //new member or new relative - update mainMember id in the relative and persist - second side of member-relative relationship
        System.out.printf("relative is null? %s%n", relative == null);
        System.out.printf("original has relative? %s%n", originalHasRelative);
        if (relative != null && !originalHasRelative) {
            Member existingMember = newMember ? memberRepository.findById(mainMember.getId()).orElseThrow() : member;
            relative.setMainMemberId(existingMember.getId());
            memberRepository.save(relative);
            System.out.println("relative saved");
        }

        return true;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findByMainMemberIdLessThan(1L);
    } //i.e. all 0's = not secondary members

    public String generateLink(String email) {
        // Step 1: Compute expiry (72 hours from now)
        Instant expiry = Instant.now().plus(72, ChronoUnit.HOURS);
        long expiryEpoch = expiry.getEpochSecond(); // seconds since epoch

        // Step 2: Build the payload string
        String payload = email + ":" + expiryEpoch;

        // Step 3: Encode to Base64 (URL-safe)
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        // Step 4: Build the full URL
        return fullClient + "/invite/" + encoded;
    }

    public String verifyCode(String encoded) {
        String email = "";
        try {
            System.out.println(encoded);
            // Step 1: Base64 URL-safe decode
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
            String payload = new String(decodedBytes, StandardCharsets.UTF_8);

            // Step 2: Split into email and expiry
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid code format");
            }

            email = parts[0];
            long expiryEpoch = Long.parseLong(parts[1]);

            // Step 3: Convert expiry to Instant
            Instant expiry = Instant.ofEpochSecond(expiryEpoch);

            // Step 4: Check if expired
            if (Instant.now().isAfter(expiry)) {
                System.out.println("Link expired");
            } else {
                System.out.println("Email: " + email);
                System.out.println("Expires at: " + expiry);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid or malformed code");
        }
        return email;
    }
}
