package com.aliza.shul.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Embeddable
public class Hdate {

	int day = -1;
	String month = "";
	LocalDate engDate;
}
