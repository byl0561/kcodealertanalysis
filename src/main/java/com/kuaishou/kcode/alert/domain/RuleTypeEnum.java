package com.kuaishou.kcode.alert.domain;

import java.util.Objects;

public enum RuleTypeEnum {
    P99("P99", "99位延时"),
    SUCCESS_RATE("SR", "成功率");

    private String symbol;
    private String describe;

    RuleTypeEnum(String symbol, String describe){
        this.symbol = symbol;
        this.describe = describe;
    }

    public static RuleTypeEnum valueBySymbol(String symbol, int offset){
        if (symbol.startsWith(P99.symbol, offset)){
            return P99;
        }
        if (symbol.startsWith(SUCCESS_RATE.symbol, offset)){
            return SUCCESS_RATE;
        }
        throw new RuntimeException("非法的字符：" + symbol);
    }
}
