package com.aliza.shul.entities;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public class Hdate {

	String day = "";
	String month = "";
	LocalDate engDate;

	public boolean isPartial() {
		return (!(day.isEmpty() && month.isEmpty())   //not a situation where all are empty
				&& (day.isEmpty() || month.isEmpty())); //but there is at least one empty
	}
	
	public boolean isEmpty() {
		return (day.isEmpty() && month.isEmpty());
	}

	public boolean isNull() {return (day == null && month == null);}

}
