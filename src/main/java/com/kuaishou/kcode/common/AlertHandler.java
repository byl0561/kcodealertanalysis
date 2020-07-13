package com.kuaishou.kcode.common;

@FunctionalInterface
public interface AlertHandler {
    void handle(int time, ServicePairWithIP servicePairWithIP, StatisticalIndicators indicators);
}
