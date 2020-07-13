package com.kuaishou.kcode.path;

import com.kuaishou.kcode.alert.domain.RuleTypeEnum;
import com.kuaishou.kcode.common.ServicePairWithoutIP;
import com.kuaishou.kcode.common.StatisticalIndicators;

import java.util.Collection;

public interface Path {
    void addPoint(int time, ServicePairWithoutIP servicePair, StatisticalIndicators indicators);

    Collection<String> getPath(ServicePairWithoutIP servicePair, int time, RuleTypeEnum ruleType);
}
