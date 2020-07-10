package com.kuaishou.kcode.utils;

public class MathConverter {
    private MathConverter(){}

    public static void addPercentageInStringBuilder(int num, StringBuilder builder){
        int point = num % 100;
        builder.append(num/100).
                append('.').
                append(point/10).
                append(point%10).
                append('%');
    }
}
