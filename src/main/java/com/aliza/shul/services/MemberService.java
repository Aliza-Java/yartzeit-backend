package com.aliza.shul.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.aliza.shul.entities.*;
import com.aliza.shul.repositories.YartzeitRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aliza.shul.repositories.MemberRepository;

@Service("memberService")
public class MemberService {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    YartzeitRepository yartzeitRepository;

    @Value("${full.client}")
    private String fullClient;

    public static final List<String> months = List.of(
            "Tishrei", "Cheshvan", "Kislev", "Tevet", "Sh'vat", "Adar 1", "Adar 2", "Adar", "Nisan", "Iyyar", "Sivan", "Tamuz", "Av", "Elul");

    public static final List<String> parashot = List.of(
            "בראשית", "נח", "לך לך", "וירא", "חיי שרה", "תולדות", "ויצא", "וישלח", "וישב", "מקץ", "ויגש", "ויחי",
            "שמות", "וארא", "בא", "בשלח", "יתרו", "משפטים", "תרומה", "תצוה", "כי תשא", "ויקהל", "פקודי", "ויקהל - פקודי",
            "ויקרא", "צו", "שמיני", "תזריע", "מצורע", "תזריע - מצורע", "אחרי מות", "קדושים", "אחרי מות - קדושים", "אמור", "בהר", "בחוקותי", "בהר - בחוקותי",
            "במדבר", "נשא", "בהעלותך", "שלח", "קורח", "חקת", "בלק", "פנחס", "מטות", "מסעי", "מטות - מסעי",
            "דברים", "ואתחנן", "עקב", "ראה", "שופטים", "כי תצא", "כי תבוא", "נצבים", "וילך", "נצבים - וילך", "האזינו", "וזאת הברכה");
//todo - maybe can use only the map, and adjust method that use it, accordingly

    public static final Map<String, String> PARASHA_TRANSLATION = Map.ofEntries(
            Map.entry("בראשית", "Bereshit"),
            Map.entry("נח", "Noach"),
            Map.entry("לך לך", "Lech Lecha"),
            Map.entry("וירא", "Vayeira"),
            Map.entry("חיי שרה", "Chayei Sara"),
            Map.entry("תולדות", "Toldot"),
            Map.entry("ויצא", "Vayetzei"),
            Map.entry("וישלח", "Vayeshev"),
            Map.entry("וישב", "Vayishlach"),
            Map.entry("מקץ", "Mikeitz"),
            Map.entry("ויגש", "Vayigash"),
            Map.entry("ויחי", "Vayechi"),
            Map.entry("שמות", "Shemot"),
            Map.entry("וארא", "Vaera"),
            Map.entry("בא", "Bo"),
            Map.entry("בשלח", "Beshalach"),
            Map.entry("יתרו", "Yitro"),
            Map.entry("משפטים", "Mishpatim"),
            Map.entry("תרומה", "Terumah"),
            Map.entry("תצוה", "Tetzaveh"),
            Map.entry("כי תשא", "Ki Tisa"),
            Map.entry("ויקהל", "Vayakhel"),
            Map.entry("פקודי", "Pekudei"),
            Map.entry("ויקהל - פקודי", "Vayakhel-Pekudei"),
            Map.entry("ויקרא", "Vayikra"),
            Map.entry("צו", "Tzav"),
            Map.entry("שמיני", "Shemini"),
            Map.entry("תזריע", "Tazria"),
            Map.entry("מצורע", "Metzora"),
            Map.entry("תזריע - מצורע", "Tazria-Metzora"),
            Map.entry("אחרי מות", "Achrei Mot"),
            Map.entry("קדושים", "Kedoshim"),
            Map.entry("אחרי מות - קדושים", "Achrei Mot-Kedoshim"),
            Map.entry("אמור", "Emor"),
            Map.entry("בהר", "Behar"),
            Map.entry("בחוקותי", "Bechukotai"),
            Map.entry("בהר - בחוקותי", "Behar-Bechukotai"),
            Map.entry("במדבר", "Bamidbar"),
            Map.entry("נשא", "Nasso"),
            Map.entry("בהעלותך", "Behaalotecha"),
            Map.entry("שלח", "Shlach"),
            Map.entry("קורח", "Korach"),
            Map.entry("חקת", "Chukat"),
            Map.entry("בלק", "Balak"),
            Map.entry("פנחס", "Pinchas"),
            Map.entry("מטות", "Matot"),
            Map.entry("מסעי", "Masei"),
            Map.entry("מטות - מסעי", "Matot-Masei"),
            Map.entry("דברים", "Devarim"),
            Map.entry("ואתחנן", "Vaetchanan"),
            Map.entry("עקב", "Eikev"),
            Map.entry("ראה", "Reeh"),
            Map.entry("שופטים", "Shoftim"),
            Map.entry("כי תצא", "Ki Tetze"),
            Map.entry("כי תבוא", "Ki Tavo"),
            Map.entry("נצבים", "Nitzavim"),
            Map.entry("וילך", "Vayeilech"),
            Map.entry("נצבים - וילך", "Nitzavim-Vayeilech"),
            Map.entry("האזינו", "Haazinu"),
            Map.entry("וזאת הברכה", "Vezot Haberachah")
    );

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

    public List<YartzeitDto> getYearlyYartzeits() {
        //TODO - make dto so that gets only member-id, first name, last name, parasha
        List<YartzeitDto> allYartzeits = yartzeitRepository.findAllYartzeitDtos();
        return allYartzeits.stream().sorted(Comparator
                .comparingInt((YartzeitDto y) -> months.indexOf(y.getMonth()))
                .thenComparingInt(y -> y.getDay())).collect(Collectors.toList());
    }

    public List<BmparashaDto> getMemberByBarMitzva() {
        List<BmparashaDto> parashotOfMembers = memberRepository.findAllWithBmParasha();

        //because of the relatively large size, it's more efficient to map first as it uses a quicker algorithm.
        Map<String, Integer> parashaOrderMap = new HashMap<>();
        for (int i = 0; i < parashot.size(); i++) {
            parashaOrderMap.put(parashot.get(i), i);
        }

        List<BmparashaDto> sortedParashotOfMembers = parashotOfMembers.stream()
                .sorted(Comparator.comparingInt(p -> parashaOrderMap.get(p.getParasha())))
                .collect(Collectors.toList());

        sortedParashotOfMembers.stream().forEach(p -> p.setParasha(PARASHA_TRANSLATION.getOrDefault(p.getParasha(), p.getParasha())));

        return sortedParashotOfMembers;
    }

    public List<AnniversaryDto> getMembersAnniversaries() {
        List<AnniversaryDto> membersWithAnn = memberRepository.findAllWithAnniversary();

        return membersWithAnn.stream().sorted(Comparator
                .comparingInt((AnniversaryDto m) -> months.indexOf(m.getMonth()))
                .thenComparingInt(m -> m.getDay())).collect(Collectors.toList());
    }

}
