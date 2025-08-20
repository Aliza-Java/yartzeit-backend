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

	public boolean isPartial() {
		return (!(day == -1 && month.isEmpty())   //not a situation where all are empty
				&& (day == -1 || month.isEmpty())); //but there is at least one empty
	}
	
	public boolean isEmpty() {
		return (day == -1 && month == null);
	}

	public boolean isNull() {return (day == -1 && month == null);}

}
