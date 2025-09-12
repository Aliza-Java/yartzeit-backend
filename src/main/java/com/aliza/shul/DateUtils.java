package com.aliza.shul;

import com.ibm.icu.util.HebrewCalendar;
import com.ibm.icu.util.ULocale;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class DateUtils {

    // Leap year mapping
    public static final Map<String, Integer> LEAP_YEAR_MONTHS = Map.ofEntries(
            Map.entry("Tishrei", 1),
            Map.entry("Cheshvan", 2),
            Map.entry("Kislev", 3),
            Map.entry("Tevet", 4),
            Map.entry("Sh'vat", 5),
            Map.entry("Adar 1", 6),
            Map.entry("Adar 2", 7),
            Map.entry("Adar", 7),
            Map.entry("Nisan", 8),
            Map.entry("Iyyar", 9),
            Map.entry("Sivan", 10),
            Map.entry("Tamuz", 11),
            Map.entry("Av", 12),
            Map.entry("Elul", 13)
    );

    // Non-leap year mapping
    public static final Map<String, Integer> REGULAR_YEAR_MONTHS = Map.ofEntries(
            Map.entry("Tishrei", 1),
            Map.entry("Cheshvan", 2),
            Map.entry("Kislev", 3),
            Map.entry("Tevet", 4),
            Map.entry("Sh'vat", 5),
            Map.entry("Adar", 6),
            Map.entry("Adar 1", 6),
            Map.entry("Adar 2", 6),
            Map.entry("Nisan", 7),
            Map.entry("Iyyar", 8),
            Map.entry("Sivan", 9),
            Map.entry("Tamuz", 10),
            Map.entry("Av", 11),
            Map.entry("Elul", 12)
    );

    public static Integer hebrewMonthToInt(String month, boolean isLeapYear) {
        Integer value = isLeapYear ? LEAP_YEAR_MONTHS.get(month) : REGULAR_YEAR_MONTHS.get(month);
        if (value == null) {
            throw new IllegalArgumentException("Unknown month: " + month);
        }
        return value;
    }

    public LocalDate convertHebrewToEnglish(String month, int day, boolean isLeapYear, int hebrewYear) {
        //the year will be turning before yartzeit (yartzeit is in tishrei, and now is Elul)
        if ("Tishrei".equals(month) && hebrewMonthToInt("Elul", isLeapYear).equals(getHebrewDate(LocalDate.now()).get(Calendar.MONTH)))

            hebrewYear++;

        HebrewCalendar hebrewCal = new HebrewCalendar(ULocale.ROOT);
        hebrewCal.set(hebrewYear, hebrewMonthToInt(month, isLeapYear), day); //put in Hebrew date
        Date engDate = hebrewCal.getTime();                                  //and get English date

        return Instant.ofEpochMilli(engDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public HebrewCalendar getHebrewDate(LocalDate localDate) {
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        HebrewCalendar hebCal = new HebrewCalendar();
        hebCal.setTime(date);

        return hebCal;
    }

    public static String formatEnglishWithOrdinal(LocalDate date) {
        int day = date.getDayOfMonth();
        String daySuffix = getDayOfMonthSuffix(day);
        String monthYear = date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        return day + daySuffix + " " + monthYear;
    }

    public static String formatHebrewWithOrdinal(int year, String month, int day){
        String daySuffix = getDayOfMonthSuffix(day);
        return day + daySuffix + " " + month + " " + year;
    }

    public static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
