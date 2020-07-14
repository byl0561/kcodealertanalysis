package com.kuaishou.kcode;

public class Configuration {
    // READ_BUFFER长度
    protected static final int BYTE_BUFFER_LENGTH = 16 * 1024 * 1024;
    // 按IP聚合后的结果集初始容量(可自适应扩容)
    protected static final int RECORDS_SIZE = 512;
    // 最大乱序时间段
    protected static final int MAX_TIME_GAP = 2;

    // 处理buffer最大线程数量
    protected static final int MAX_BUFFER_HANDLER_COUNT = 2;

    // Q1终止信号
    protected static final Runnable POX = new Runnable() {
        @Override
        public void run() {

        }
    };

}
