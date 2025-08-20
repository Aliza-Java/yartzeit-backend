package com.aliza.shul.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class Yartzeit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	@JsonIgnore
	private Member member;

	@Override
	public String toString() {
		return "Yartzeit{" +
				"id=" + id +
				"memberId: " + member.id +
				", date=" + date +
				", hName='" + hName + '\'' +
				", relationship='" + relationship + '\'' +
				'}';
	}

	@Embedded
	private Hdate date;
	private String hName;
	private String relationship;
}
