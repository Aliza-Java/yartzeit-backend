package com.aliza.shul.entities;

import lombok.Getter;

@Getter
public enum MemberType {
	MEM("member"),
	SUP("supporter");

	private String full;

	MemberType(String full) {
		this.full = full;
	}
}