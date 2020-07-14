package com.kuaishou.kcode.common;

import com.kuaishou.kcode.Configuration;
import com.kuaishou.kcode.utils.MathUtil;
import com.kuaishou.kcode.utils.ServiceRecorderPool;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRecorder extends Configuration {
    public ServiceRecorder(){
        ip2Records = new ConcurrentHashMap<>();
        allRecords = new Records(-1, -1);
    }
    private Map<Long, Records> ip2Records;
    private Pool<Records> recordsHolder = new Pool<>();
    private Records allRecords;
    private ServicePairWithIP servicePairWithIP = new ServicePairWithIP("", "", -1, -1);

    public void init(){
        ip2Records.values().forEach(records -> {
            recordsHolder.put(records);
        });
        ip2Records.clear();
        allRecords.init(-1, -1);
    }

    public void calculate(){
        ip2Records.values().forEach(Records::calculate);
        allRecords.calculate();
    }

    public void collectResult(AlertHandler alertHandler, PathHandler pathHandler, int time, String fromService, String toService){
        ip2Records.values().forEach(records -> {
            servicePairWithIP.init(fromService, toService, records.fromIP, records.toIP);
            alertHandler.handle(time, servicePairWithIP, new StatisticalIndicators(records.p99, records.successRate));
        });
        pathHandler.handle(time, new ServicePairWithoutIP(servicePairWithIP.getFromService(), servicePairWithIP.getToService()), new StatisticalIndicators(allRecords.p99, allRecords.successRate));
        ServiceRecorderPool.put(this);
    }

    private synchronized Records getRecords(int fromIP, int toIP){
        if (recordsHolder.isEmpty()){
            return new Records(fromIP, toIP);
        }
        else {
            Records records = recordsHolder.get();
            records.init(fromIP, toIP);
            return records;
        }
    }

    public void addRecord(Record record){
        Long key = (((long) record.getFromIP()) << 32) + record.getToIP();
        Records records = ip2Records.get(key);
        if (Objects.isNull(records)){
            ip2Records.putIfAbsent(key, getRecords(record.getFromIP(), record.getToIP()));
            records = ip2Records.get(key);
        }
        records.addRecord(record);
        allRecords.addRecord(record);
    }

    private static class Records{
        public Records(int fromIP, int toIP){
            this.fromIP = fromIP;
            this.toIP = toIP;
            delays = new short[RECORDS_SIZE];
            successTimes = 0;
            totalTimes = 0;
        }

        int fromIP;
        int toIP;
        int successTimes;
        int totalTimes;
        short[] delays;

        short successRate;
        short p99;

        public void init(int fromIP, int toIP){
            this.fromIP = fromIP;
            this.toIP = toIP;
            successTimes = 0;
            totalTimes = 0;
        }

        synchronized void addRecord(Record record){
            // 扩容1.5倍
            if (totalTimes == delays.length){
                short[] target = new short[delays.length + (delays.length >> 1)];
                System.arraycopy(delays, 0, target, 0, delays.length);
                delays = target;
            }

            delays[totalTimes++] = record.getDelay();
            if (record.isState()){
                successTimes++;
            }
        }

        void calculate(){
            successRate = (short) ((long) successTimes * 10000 / totalTimes);
            p99 = MathUtil.get99th(delays, totalTimes);
        }
    }
}
