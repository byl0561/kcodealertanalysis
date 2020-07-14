package com.kuaishou.kcode.utils;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;

public class TimeConverter {
    private TimeConverter(){}

    private static final int[] _365M = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int zeroPoint = calculateDays(1970, 1, 1);
    private static final int offset  = ZonedDateTime.now().getOffset().getTotalSeconds() / 60;

    private static SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static int convertStringToInt(String date){
        int[] dat = parseDate(date);
        int days = calculateDays(dat[0], dat[1], dat[2]) - zeroPoint;
        return (days * 24 + dat[3]) * 60 + dat[4] - offset;
    }

    public static void addInStringyBuilder(int minutes, StringBuilder sb){
        String timeText = format.format(minutes * 60000L);
        sb.append(timeText);


        /*
        minutes += offset;
        int minute = minutes % 60;
        minutes /= 60;
        int hour = minutes % 24;
        int days = minutes / 24 + zeroPoint;
        int[] y = calculateYears(days);
        sb.append(y[0]).append('-').
                append(y[1] / 10).append(y[1] % 10).append('-').
                append(y[2] / 10).append(y[2] % 10).append(' ').
                append(hour / 10).append(hour % 10).append(':').
                append(minute / 10).append(minute % 10);
                */
    }

    private static int[] calculateYears(int days){

        int year = 0;
        year += 400 * (days / (365*400 + 24 * 4 + 1));
        days %= 365*400 + 24 * 4 + 1;
        if (days == 365*400 + 24 * 4){
            return new int[]{year+399+1, 12, 31};
        }
        year += 100 * (days / (365*100 + 24));
        days %= 365*100 + 24;
        year += 4 * (days / (365*4 + 1));
        days %= 365*4 + 1;
        if (days == 365*4){
            return new int[]{year+3+1, 12, 31};
        }
        year += days / 365;
        days %= 365;

        year++;

        if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)){
            if (days == 31 + 28){
                return new int[]{year, 2, 29};
            }
            if (days > 31 + 28){
                days--;
            }
        }
        days++;
        int month = 1;
        while (days > _365M[month-1]){
            days -= _365M[month-1];
            month++;
        }

        return new int[]{year, month, days};
    }

    private static int calculateDays(int year, int month, int day) {
        int dayC1 = day - 1;
        for (int i = month; i > 1; i--) dayC1 += _365M[i - 1 - 1];
        if ((year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) && month > 2) dayC1++;
        dayC1 += (365 * (year - 1) + (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400);
        return dayC1;
    }

    private static int[] parseDate(String date){
        return new int[]{
                parseString(date, 0, 4),
                parseString(date, 5, 7),
                parseString(date, 8, 10),
                parseString(date, 11, 13),
                parseString(date, 14,16)
        };
    }

    private static int parseString(String str, int start, int end){
        int result = 0;
        for (; start < end; start++){
            result = result * 10 + str.charAt(start) - '0';
        }
        return result;
    }
}
