package com.kuaishou.kcode.common;

public class StatisticalIndicators {
    public StatisticalIndicators (short P99, short successRate){
        this.P99 = P99;
        this.successRate = successRate;
    }

    private short P99;
    private short successRate;

    public short getP99() {
        return P99;
    }

    public short getSuccessRate() {
        return successRate;
    }
}
