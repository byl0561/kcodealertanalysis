package com.kuaishou.kcode.alert;

import com.kuaishou.kcode.common.ServicePairWithIP;
import com.kuaishou.kcode.common.StatisticalIndicators;

import java.util.Collection;

public interface AlertAnalyser {

    void init(Collection<String> alertRules);

    void analyser(int nowTime, ServicePairWithIP servicePairWithIP, StatisticalIndicators indicators);

    Collection<String> getAlert();
}
