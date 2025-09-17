package com.aliza.shul.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnniversaryDto {
    String firstName;
    String lastName;
    int day;
    String month;
    String spouse;
}
