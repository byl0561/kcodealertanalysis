package com.kuaishou.kcode.monitor;

import com.kuaishou.kcode.Configuration;
import com.kuaishou.kcode.common.AlertHandler;
import com.kuaishou.kcode.common.PathHandler;
import com.kuaishou.kcode.common.ReadBuffer;
import com.kuaishou.kcode.common.Record;
import com.kuaishou.kcode.path.Path;
import com.kuaishou.kcode.utils.ReadBufferPool;

import java.io.InputStream;

public abstract class AbstractMonitorImpl extends Configuration implements Monitor {
    protected int baseTimeStamp;

    @Override
    public void prepare(InputStream inputStream, AlertHandler alertHandler, PathHandler pathHandler) {
        ReadBuffer nowBuffer = ReadBufferPool.get(inputStream);
        ReadBuffer nextBuffer;
        // 获取开始时间
        nowBuffer.read();
        nextBuffer = ReadBufferPool.get(inputStream);
        nowBuffer.trim();
        nextBuffer.prepare(nowBuffer);
        setBaseTimeStamp(nowBuffer);

        beforePrepare();

        dealReadBuffer(nowBuffer, alertHandler, pathHandler);
        nowBuffer = nextBuffer;

        while (nowBuffer.read() != -1){
            nextBuffer = ReadBufferPool.get(inputStream);
            nowBuffer.trim();
            nextBuffer.prepare(nowBuffer);
            dealReadBuffer(nowBuffer, alertHandler, pathHandler);
            nowBuffer = nextBuffer;
        }
        afterPrepare(alertHandler, pathHandler);
    }

    private void setBaseTimeStamp(ReadBuffer buffer){
        Record firstRecord = buffer.next();
        baseTimeStamp = firstRecord.getTimeStamp() - MAX_TIME_GAP;
        buffer.reset();
    }

    protected void afterPrepare(AlertHandler alertHandler, PathHandler pathHandler){}

    protected void beforePrepare(){}

    protected abstract void dealReadBuffer(ReadBuffer buffer, AlertHandler alertHandler, PathHandler pathHandler);
}
