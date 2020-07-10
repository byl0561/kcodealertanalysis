package com.kuaishou.kcode.alert.domain;

public enum RangeEnum {
    ABOVE('>', "大于特定值"),
    BELOW('<', "小于特定值");

    private char symbol;
    private String describe;

    RangeEnum(char symbol, String describe){
        this.symbol = symbol;
        this.describe = describe;
    }

    public static RangeEnum valueOfSymbol(char symbol){
        if (symbol == ABOVE.symbol){
            return ABOVE;
        }
        if (symbol == BELOW.symbol){
            return BELOW;
        }
        throw new RuntimeException("非法的字符：" + symbol);
    }
}
