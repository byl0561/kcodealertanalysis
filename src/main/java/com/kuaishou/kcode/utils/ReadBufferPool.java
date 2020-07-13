package com.kuaishou.kcode.utils;

import com.kuaishou.kcode.Configuration;
import com.kuaishou.kcode.common.Pool;
import com.kuaishou.kcode.common.ReadBuffer;

import java.io.InputStream;

public class ReadBufferPool extends Configuration {
    private ReadBufferPool(){}

    private static Pool<ReadBuffer> readBufferHolder;

    public synchronized static ReadBuffer get(InputStream inputStream) {
        if (readBufferHolder.isEmpty()){
            return new ReadBuffer(inputStream, new byte[BYTE_BUFFER_LENGTH]);
        }
        else{
            return readBufferHolder.get();
        }
    }

    public synchronized static void put(ReadBuffer readBuffer){
        readBufferHolder.put(readBuffer);
    }

    public static void init(){
        readBufferHolder = new Pool<>();
    }
}
