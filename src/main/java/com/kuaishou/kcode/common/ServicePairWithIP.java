package com.kuaishou.kcode.common;

import java.util.Objects;

public class ServicePairWithIP {
    public ServicePairWithIP(String fromService, String toService, int fromIP, int toIP){
        init(fromService, toService, fromIP, toIP);
    }

    public ServicePairWithIP(ServicePairWithIP pair){
        init(pair);
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

    public void init(ServicePairWithIP pair){
        this.fromService = pair.fromService;
        this.toService = pair.toService;
        this.fromIP = pair.fromIP;
        this.toIP = pair.toIP;
        this.hashCode = pair.hashCode;
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
        ServicePairWithIP servicePairWithIP = (ServicePairWithIP) o;
        return Objects.equals(fromService, servicePairWithIP.fromService) &&
                Objects.equals(toService, servicePairWithIP.toService) &&
                fromIP == servicePairWithIP.fromIP &&
                toIP == servicePairWithIP.toIP;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
