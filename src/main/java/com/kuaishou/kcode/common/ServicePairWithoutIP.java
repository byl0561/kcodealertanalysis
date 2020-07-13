package com.kuaishou.kcode.common;

import java.util.Objects;

public class ServicePairWithoutIP {
    public ServicePairWithoutIP(String fromService, String toService){
        this.fromService = fromService;
        this.toService = toService;
        hashCode = fromService.hashCode() ^ toService.hashCode();
    }

    public ServicePairWithoutIP(ServicePairWithoutIP pair){
        this.fromService = pair.fromService;
        this.toService = pair.toService;
        this.hashCode = pair.hashCode;
    }

    private int hashCode;
    private String fromService;
    private String toService;

    public String getFromService() {
        return fromService;
    }

    public String getToService() {
        return toService;
    }

    @Override
    public boolean equals(Object o) {
        ServicePairWithoutIP servicePairWithoutIP = (ServicePairWithoutIP) o;
        return Objects.equals(fromService, servicePairWithoutIP.fromService) &&
                Objects.equals(toService, servicePairWithoutIP.toService);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
