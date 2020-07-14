package com.kuaishou.kcode.monitor;

import com.kuaishou.kcode.common.AlertHandler;
import com.kuaishou.kcode.common.PathHandler;

import java.io.InputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

public interface Monitor {
    void init(BlockingQueue<Runnable> bq);

    void prepare(InputStream inputStream, AlertHandler alertHandler, PathHandler pathHandler);
}
