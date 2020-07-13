package com.kuaishou.kcode.common;

public class Record {
    private String fromService;
    private String toService;
    private int fromIP;
    private int toIP;
    private boolean state;
    private int timeStamp;
    private short delay;

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public String getToService() {
        return toService;
    }

    public void setToService(String toService) {
        this.toService = toService;
    }

    public int getFromIP() {
        return fromIP;
    }

    public void setFromIP(int fromIP) {
        this.fromIP = fromIP;
    }

    public int getToIP() {
        return toIP;
    }

    public void setToIP(int toIP) {
        this.toIP = toIP;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public short getDelay() {
        return delay;
    }

    public void setDelay(short delay) {
        this.delay = delay;
    }
}
