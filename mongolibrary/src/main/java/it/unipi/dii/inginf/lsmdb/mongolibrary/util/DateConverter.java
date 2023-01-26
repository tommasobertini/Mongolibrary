package it.unipi.dii.inginf.lsmdb.mongolibrary.util;

import java.time.*;

public class DateConverter {

    public static Integer toLong(String date)
    {
        return (int) LocalDate.parse(date).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC);
    }

    public static LocalDate toLocalDate(Integer date)
    {
        return LocalDate.ofInstant(Instant.ofEpochSecond(date), ZoneId.systemDefault());
    }
}
