package com.kh.totalproject.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class WeeklyTimeCalculator {
    public static LocalDateTime getStartOfLastWeek() {
        // 저번주 월요일의 00시 반환
        return LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY).atStartOfDay();
    }

    public static LocalDateTime getEndOfLastWeek() {
        // 저번주 일요일의 23:59 반환
        return LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
    }
}
