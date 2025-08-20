package com.aliza.shul.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Member {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;
	
	String firstName;
	String lastName;
	String phone;
	String email;
	String hebrewName;
	String fatherName;
	String motherName;
	
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "day", column = @Column(name = "dob_day")),
			@AttributeOverride(name = "month", column = @Column(name = "dob_month")),
			@AttributeOverride(name = "engDate", column = @Column(name = "dob_eng"))
	})
	Hdate dob;
	
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "day", column = @Column(name = "ann_day")),
			@AttributeOverride(name = "month", column = @Column(name = "ann_month")),
			@AttributeOverride(name = "engDate", column = @Column(name = "ann_eng"))
	})
	Hdate anniversary;
	String spouse; //mandatory only if anniversary was entered
	
	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "day", column = @Column(name = "aliya_day")),
			@AttributeOverride(name = "month", column = @Column(name = "aliya_month")),
			@AttributeOverride(name = "engDate", column = @Column(name = "aliya_eng"))
	})
	Hdate aliya;
	
	String bmParasha;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	Gender gender = Gender.MALE;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "relative_id")
	Member relative; //second adult, e.g. wife or son

	private long mainMemberId;
	
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	List<Yartzeit> yartzeits = new ArrayList<>();

	LocalDateTime since = LocalDateTime.now();

	public void addYartzeit(Yartzeit y) {
		y.setMember(this);
		yartzeits.add(y);
	}
}
