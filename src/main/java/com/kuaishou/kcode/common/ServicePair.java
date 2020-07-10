package com.kuaishou.kcode.common;

import java.util.Objects;

public class ServicePair {
    public ServicePair(String fromService, String toService, int fromIP, int toIP){
        init(fromService, toService, fromIP, toIP);
    }

    private int hashCode;
    private String fromService;
    private String toService;
    private int fromIP;
    private int toIP;

    public void init(String fromService, String toService, int fromIP, int toIP){
        this.fromService = fromService;
        this.toService = toService;
        this.fromIP = fromIP;
        this.toIP = toIP;
        hashCode = fromService.hashCode() ^ toService.hashCode() ^ fromIP ^ toIP;
    }

    public String getFromService() {
        return fromService;
    }

    public String getToService() {
        return toService;
    }

    public int getFromIP() {
        return fromIP;
    }

    public int getToIP() {
        return toIP;
    }

    @Override
    public boolean equals(Object o) {
        ServicePair servicePair = (ServicePair) o;
        return Objects.equals(fromService, servicePair.fromService) &&
                Objects.equals(toService, servicePair.toService) &&
                fromIP == servicePair.fromIP &&
                toIP == servicePair.toIP;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
