package com.kuaishou.kcode.utils;

import com.kuaishou.kcode.common.ServicePairWithIP;
import com.kuaishou.kcode.common.ServicePairWithoutIP;

public class ServicePairFactory {
    private ServicePairFactory(){};

    public static ServicePairWithIP clone(ServicePairWithIP pair) {
        return new ServicePairWithIP(pair);
    }

    public static ServicePairWithoutIP clone(ServicePairWithoutIP pair) {
        return new ServicePairWithoutIP(pair);
    }
}
