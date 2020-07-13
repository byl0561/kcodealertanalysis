package com.kuaishou.kcode.common;

import com.kuaishou.kcode.utils.IPAddressConverter;
import com.kuaishou.kcode.utils.StringPool;

import java.io.IOException;
import java.io.InputStream;

public class ReadBuffer {
    public ReadBuffer(InputStream inputStream, byte[] buffer){
        stream = inputStream;
        this.buffer = buffer;
    }

    private InputStream stream;
    private byte[] buffer;
    private int start = 0;
    private int offset;
    private int end;
    private Record record = new Record();

    public void reset(){
        start = 0;
    }

    public int read(){
        try {
            offset = stream.read(buffer, start, buffer.length - start);
        } catch (IOException e) {
            throw new RuntimeException("read from inputStream failed, e: " + e);
        }
        offset += start;
        return offset;
    }

    public void invalid(){
        start = 0;
        end = -1;
    }

    public void trim(){
        end = offset;
        start = 0;
        while(buffer[--end] != '\n');
    }

    public boolean hasNext(){
        return start < end;
    }

    public Record next() {
        if (!hasNext()){
            return null;
        }
        // 主调服务名
        String fromService = StringPool.getCachedString(buffer, start);
        for (; buffer[start] != ','; start++);
        start++;
        record.setFromService(fromService);

        // 主调方IP
        int fromIp = IPAddressConverter.convertByteArrayToInt(buffer, start);
        for (; buffer[start] != ','; start++);
        start++;
        record.setFromIP(fromIp);

        // 被调服务名
        String toService = StringPool.getCachedString(buffer, start);
        for (; buffer[start] != ','; start++);
        start++;
        record.setToService(toService);

        // 被调方IP
        int toIP = IPAddressConverter.convertByteArrayToInt(buffer, start);
        for (; buffer[start] != ','; start++);
        start++;
        record.setToIP(toIP);

        // 结果
        if (buffer[start] == 't'){
            record.setState(true);
        }
        else if (buffer[start] == 'f'){
            record.setState(false);
        }
        else {
            throw new RuntimeException("cannot convert to boolean, status: " + StringPool.getCachedString(buffer, start));
        }
        for (; buffer[start] != ','; start++);
        start++;

        // 延时
        short delay = 0;
        for (; buffer[start] != ','; start++){
            delay = (short) (delay * 10 + buffer[start] - '0');
        }
        start++;
        record.setDelay(delay);

        // 时间戳
        int idx = start;
        start += 4;
        int timeStamp = 0;
        for (; buffer[start] != '\n'; start++){
            timeStamp = timeStamp * 10 + buffer[idx++] - '0';
        }
        timeStamp /= 6;
        record.setTimeStamp(timeStamp);
        start++;

        return record;
    }

    public void prepare(ReadBuffer remain){
        start = remain.getOffset() - remain.getEnd() - 1;
        System.arraycopy(remain.getBuffer(), remain.getEnd()+1, buffer, 0, start);
    }

    private byte[] getBuffer() {
        return buffer;
    }

    private int getOffset() {
        return offset;
    }

    private int getEnd() {
        return end;
    }

}
