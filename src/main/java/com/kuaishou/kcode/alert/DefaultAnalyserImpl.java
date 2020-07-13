package com.kuaishou.kcode.alert;

import com.kuaishou.kcode.alert.domain.RuleTypeEnum;
import com.kuaishou.kcode.alert.domain.RangeEnum;
import com.kuaishou.kcode.common.ServicePairWithIP;
import com.kuaishou.kcode.common.StatisticalIndicators;
import com.kuaishou.kcode.utils.*;

import java.util.*;

public class DefaultAnalyserImpl implements AlertAnalyser {
    private int lastTime;
    private Map<ServicePairWithIP, Counter> records = new HashMap<>();
    private Collection<String> alertList;
    private Rule rules = null;

    @Override
    public void init(Collection<String> alertRules) {
        lastTime = -2;
        alertRules.forEach(rule -> {
            rules = new Rule(rules, rule);
        });
        alertList = new LinkedList<>();
    }

    // WARN: 要求请求到来严格有序,单线程处理
    @Override
    public void analyser(int nowTime, ServicePairWithIP servicePairWithIP, StatisticalIndicators indicators) {
        if (lastTime + 1 != nowTime){
            records.clear();
        }
        if (Objects.nonNull(rules)){
            rules.filter(servicePairWithIP, alertList, indicators, nowTime);
        }
        lastTime = nowTime;
    }

    @Override
    public Collection<String> getAlert() {
        return alertList;
    }

    private static class Counter{
        private byte count;
        private int lastTmp;

        public Counter(byte count, int lastTmp) {
            this.count = count;
            this.lastTmp = lastTmp;
        }
    }

    private class Rule{
        public Rule(Rule nextRule, String rule){
            this.nextRule = nextRule;
            generateRule(rule);
        }

        private Rule nextRule;
        private int uniqueKey = 0;
        private String fromService;
        private String toService;
        private RuleTypeEnum ruleType;
        private byte timeLimit = 0;
        private RangeEnum range;
        private short valueLimit = 0;

        private void generateRule(String rule){
            int idx = 0;
            while (rule.charAt(idx) != ','){
                uniqueKey = uniqueKey * 10 + rule.charAt(idx++) - '0';
            }
            idx++;
            fromService = StringPool.getCachedString(rule, idx);
            while (rule.charAt(idx++) != ',');
            toService = StringPool.getCachedString(rule, idx);
            while (rule.charAt(idx++) != ',');
            ruleType = RuleTypeEnum.valueBySymbol(rule, idx);
            while (rule.charAt(idx++) != ',');
            while (rule.charAt(idx) >= '0' && rule.charAt(idx) <= '9'){
                timeLimit = (byte) (timeLimit * 10 + rule.charAt(idx++) - '0');
            }
            range = RangeEnum.valueOfSymbol(rule.charAt(idx++));
            idx++;
            if (ruleType == RuleTypeEnum.SUCCESS_RATE){
                valueLimit = (short) (100 * Float.parseFloat(rule.substring(idx, rule.length()-1)));
            }
            else if (ruleType == RuleTypeEnum.P99){
                while (rule.charAt(idx) >= '0' && rule.charAt(idx) <= '9'){
                    valueLimit = (short) (valueLimit * 10 + rule.charAt(idx++) - '0');
                }
            }
        }

        public void filter(ServicePairWithIP servicePairWithIP, Collection<String> collection, StatisticalIndicators indicators, int time){
            if ((fromService.equals("ALL") || fromService.equals(servicePairWithIP.getFromService())) &&
                    (toService.equals("ALL")) || toService.equals(servicePairWithIP.getToService())){
                if (match(indicators)){
                    Counter counter = records.get(servicePairWithIP);
                    if (Objects.isNull(counter)){
                        counter = new Counter((byte) 0, time - 1);
                        records.put(ServicePairFactory.clone(servicePairWithIP), counter);
                    }
                    if (counter.lastTmp == time - 1){
                        counter.count = (byte) Math.min(timeLimit, counter.count + 1);
                    }
                    else{
                        counter.count = 1;
                    }
                    if (counter.count == timeLimit){
                        alert(collection, servicePairWithIP, time, indicators);
                    }
                    counter.lastTmp = time;
                }
            }
            if (Objects.nonNull(nextRule)){
                nextRule.filter(servicePairWithIP, collection, indicators, time);
            }
        }

        private void alert(Collection<String> collection, ServicePairWithIP servicePairWithIP, int time, StatisticalIndicators indicators){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(uniqueKey).append(',');
            TimeConverter.addInStringyBuilder(time, stringBuilder);
            stringBuilder.append(',');
            stringBuilder.append(servicePairWithIP.getFromService()).append(',');
            IPAddressConverter.addInStringBuilder(servicePairWithIP.getFromIP(), stringBuilder);
            stringBuilder.append(',');
            stringBuilder.append(servicePairWithIP.getToService()).append(',');
            IPAddressConverter.addInStringBuilder(servicePairWithIP.getToIP(), stringBuilder);
            stringBuilder.append(',');
            if (ruleType == RuleTypeEnum.P99){
                stringBuilder.append(indicators.getP99()).append("ms");
            }
            else if (ruleType == RuleTypeEnum.SUCCESS_RATE){
                MathConverter.addPercentageInStringBuilder(indicators.getSuccessRate(), stringBuilder);
            }
            collection.add(stringBuilder.toString());
        }

        private boolean match(StatisticalIndicators statisticalIndicators){
            short target;
            switch (ruleType){
                case P99:
                    target = statisticalIndicators.getP99();
                    break;
                case SUCCESS_RATE:
                    target = statisticalIndicators.getSuccessRate();
                    break;
                default:
                    throw new RuntimeException("不支持的类型：" + ruleType);
            }
            switch (range){
                case ABOVE:
                    return target > valueLimit;
                case BELOW:
                    return target < valueLimit;
                default:
                    return false;
            }
        }
    }
}
