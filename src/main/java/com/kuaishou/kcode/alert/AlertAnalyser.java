package com.kuaishou.kcode.alert;

import com.kuaishou.kcode.common.ServicePair;
import com.kuaishou.kcode.common.StatisticalIndicators;

import java.util.Collection;
import java.util.Map;

public interface AlertAnalyser {

    int init(Collection<String> alertRules);

    Collection<String> analyser(ServicePair servicePair, StatisticalIndicators indicators, int nowTime);
}
