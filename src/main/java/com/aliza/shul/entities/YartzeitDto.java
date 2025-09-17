package com.aliza.shul.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YartzeitDto {
    String firstName;
    String lastName;
    String relationship;
    int day;
    String month;
    String yname;
}
