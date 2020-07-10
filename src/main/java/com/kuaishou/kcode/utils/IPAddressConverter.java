package com.kuaishou.kcode.utils;

public class IPAddressConverter {
    private IPAddressConverter(){}

    public static int convertByteArrayToInt(byte[] source, int start){
        int IP = 0;
        int num;
        for (int i = 0; i < 3; i++){
            num = 0;
            for (; source[start] != '.'; start++){
                num = num * 10 + source[start] - '0';
            }
            IP = (IP << 8) | num;
            start++;
        }
        num = 0;
        for (; source[start] != ','; start++){
            num = num * 10 + source[start] - '0';
        }
        IP = (IP << 8) | num;
        return IP;
    }

    public static void addInStringBuilder(int IP, StringBuilder builder){
        builder.append((IP >> 24) & 0xFF).append('.').
                append((IP >> 16) & 0xFF).append('.').
                append((IP >> 8) & 0xFF).append('.').
                append(IP & 0xFF);
    }
}
