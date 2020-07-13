package com.kuaishou.kcode;

import com.kuaishou.kcode.alert.AlertAnalyser;
import com.kuaishou.kcode.alert.DefaultAnalyserImpl;
import com.kuaishou.kcode.alert.domain.RuleTypeEnum;
import com.kuaishou.kcode.common.ServicePairWithoutIP;
import com.kuaishou.kcode.monitor.DefaultMonitorImpl;
import com.kuaishou.kcode.monitor.Monitor;
import com.kuaishou.kcode.path.DefaultPathImpl;
import com.kuaishou.kcode.path.Path;
import com.kuaishou.kcode.utils.ReadBufferPool;
import com.kuaishou.kcode.utils.ServiceRecorderPool;
import com.kuaishou.kcode.utils.StringPool;
import com.kuaishou.kcode.utils.TimeConverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * @author KCODE
 * Created on 2020-07-04
 */
public class KcodeAlertAnalysisImpl implements KcodeAlertAnalysis {
    private AlertAnalyser alertAnalyser;
    private Monitor monitor;
    private Path path;
    private ExecutorService executor;

    @Override
    public Collection<String> alarmMonitor(String filePath, Collection<String> alertRules) {
        init(alertRules);
        try (InputStream stream = new FileInputStream(filePath)){
            monitor.prepare(stream, alertAnalyser::analyser, path::addPoint);
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return alertAnalyser.getAlert();
    }

    private void init(Collection<String> alertRules){
        ReadBufferPool.init();
        ServiceRecorderPool.init();
        StringPool.init();
        executor = Executors.newSingleThreadExecutor();
        alertAnalyser = new DefaultAnalyserImpl();
        alertAnalyser.init(alertRules);
        monitor = new DefaultMonitorImpl(executor);
        path = new DefaultPathImpl();
    }

    @Override
    public Collection<String> getLongestPath(String caller, String responder, String time, String type) {
        RuleTypeEnum ruleType;
        if (type.equals(ALERT_TYPE_P99)){
            ruleType = RuleTypeEnum.P99;
        }
        else if (type.equals(ALERT_TYPE_SR)){
            ruleType = RuleTypeEnum.SUCCESS_RATE;
        }
        else {
            return Collections.emptyList();
        }
        return path.getPath(new ServicePairWithoutIP(caller, responder), TimeConverter.convertStringToInt(time), ruleType);
    }
}