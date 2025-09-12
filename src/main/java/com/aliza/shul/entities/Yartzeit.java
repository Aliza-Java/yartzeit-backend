package com.aliza.shul.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Yartzeit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "member_id")
	@JsonIgnore
	private Member member;

	@Embedded
	private Hdate date;
	private String name;
	private String relationship;


	@Override
	public String toString() {
		return "Yartzeit{" +
				"id=" + id +
				"memberId: " + (member == null ? "null" : member.id) +
				", date=" + date +
				", hebrew Name='" + name + '\'' +
				", relationship='" + relationship + '\'' +
				'}';
	}
}
