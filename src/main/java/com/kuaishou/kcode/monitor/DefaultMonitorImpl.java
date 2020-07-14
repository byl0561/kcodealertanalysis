package com.kuaishou.kcode.monitor;

import com.kuaishou.kcode.common.*;
import com.kuaishou.kcode.utils.ReadBufferPool;
import com.kuaishou.kcode.utils.ServiceRecorderPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMonitorImpl extends AbstractMonitorImpl {
    private BlockingQueue<ReadBuffer> readBufferBlockingQueue;
    private Map<Integer, Map<ServicePairWithoutIP, ServiceRecorder>> tmp;
    private int[] bufferHandlerRecorder;
    private AtomicInteger threadCounter;

    private Semaphore semaphore;
    private BlockingQueue<Runnable> blockingQueue;

    @Override
    protected void dealReadBuffer(ReadBuffer buffer, AlertHandler alertHandler, PathHandler pathHandler) {
        if (!readBufferBlockingQueue.offer(buffer)){
            if (threadCounter.get() < MAX_BUFFER_HANDLER_COUNT){
                new Worker(threadCounter.getAndIncrement(), alertHandler, pathHandler).start();
            }
            try {
                readBufferBlockingQueue.put(buffer);
            } catch (InterruptedException e) {
                throw new RuntimeException("主线程不能被中断");
            }
        }
    }

    @Override
    protected void beforePrepare() {
        Arrays.fill(bufferHandlerRecorder, Integer.MAX_VALUE);
    }

    @Override
    protected void afterPrepare(AlertHandler alertHandler, PathHandler pathHandler) {
        ReadBuffer buffer = ReadBufferPool.get(null);
        buffer.invalid();
        int count = threadCounter.get();
        for (int i = 0; i < count; i++) {
            try {
                readBufferBlockingQueue.put(buffer);
            } catch (InterruptedException e) {
                throw new RuntimeException("main process has been interrupted");
            }
        }
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException("main process has been interrupted");
        }
        // 按序提交
        while (!tmp.isEmpty()){
            Map<ServicePairWithoutIP, ServiceRecorder> job = tmp.remove(baseTimeStamp);
            submit(job, alertHandler, pathHandler);
            baseTimeStamp++;
        }

        try {
            blockingQueue.put(POX);
        }
        catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        // todo：资源释放
        tmp = null;
    }

    private void submit(Map<ServicePairWithoutIP, ServiceRecorder> job, AlertHandler alertHandler, PathHandler pathHandler){
        if (Objects.nonNull(job)){
            job.forEach((servicePair, recorder) -> {
                recorder.calculate();
                try {
                    blockingQueue.put(() -> {
                        recorder.collectResult(alertHandler, pathHandler, baseTimeStamp, servicePair.getFromService(), servicePair.getToService());
                    });
                }
                catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void init(BlockingQueue<Runnable> bq) {
        this.blockingQueue = bq;
        tmp = new ConcurrentHashMap<>();
        readBufferBlockingQueue = new SynchronousQueue<>();
        bufferHandlerRecorder = new int[MAX_BUFFER_HANDLER_COUNT];
        threadCounter = new AtomicInteger(0);
        semaphore = new Semaphore(0);
    }

    private class Worker extends Thread {
        public Worker(int name, AlertHandler alertHandler, PathHandler pathHandler){
            this.name = name;
            this.alertHandler = alertHandler;
            this.pathHandler = pathHandler;
            synchronized (bufferHandlerRecorder){
                lastTime = baseTimeStamp;
            }
        }

        private int name;
        private ReadBuffer readBuffer;
        private int lastTime;

        private AlertHandler alertHandler;
        private PathHandler pathHandler;

        @Override
        public void run() {
            while (true){
                fetchReadBuffer();
                // 毒药
                if (!readBuffer.hasNext()){
                    if (threadCounter.decrementAndGet() == 0){
                        semaphore.release();
                    }
                    return;
                }
                Record record;
                synchronized (bufferHandlerRecorder){
                    bufferHandlerRecorder[name] = lastTime;
                }
                while ((record = readBuffer.next()) != null){
                    dealRecord(record);
                }
                synchronized (bufferHandlerRecorder){
                    bufferHandlerRecorder[name] = Integer.MAX_VALUE;
                }
                ReadBufferPool.put(readBuffer);
            }
        }

        private void fetchReadBuffer(){
            readBuffer = readBufferBlockingQueue.poll();
            if (Objects.isNull(readBuffer)){
                try {
                    readBuffer = readBufferBlockingQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException("工作线程被中断");
                }
            }
        }

        private void dealRecord(Record record){
            int nowTime = record.getTimeStamp();
            int lt = nowTime - MAX_TIME_GAP;
            if (lt > lastTime){
                lunchJob(lt);
                lastTime = lt;
            }

            Map<ServicePairWithoutIP, ServiceRecorder> servicePair2recorder = tmp.get(nowTime);
            if (Objects.isNull(servicePair2recorder)){
                tmp.putIfAbsent(nowTime, new ConcurrentHashMap<>());
                servicePair2recorder = tmp.get(nowTime);
            }
            addRecordToRecorder(record, servicePair2recorder);
        }

        private void lunchJob(int nowTime){
            int min = Integer.MAX_VALUE;

            synchronized (bufferHandlerRecorder){
                bufferHandlerRecorder[name] = nowTime;
                for (int i : bufferHandlerRecorder){
                    min = Math.min(min, i);
                }
                for (; baseTimeStamp < min; baseTimeStamp++){
                    Map<ServicePairWithoutIP, ServiceRecorder> job = tmp.remove(baseTimeStamp);
                    submit(job, alertHandler, pathHandler);
                }
            }
        }

        private void addRecordToRecorder(Record record, Map<ServicePairWithoutIP, ServiceRecorder> servicePair2recorder){
            ServicePairWithoutIP servicePair = new ServicePairWithoutIP(record.getFromService(), record.getToService());
            ServiceRecorder recorder = servicePair2recorder.get(servicePair);
            if (Objects.isNull(recorder)){
                servicePair2recorder.putIfAbsent(servicePair, ServiceRecorderPool.get());
                recorder = servicePair2recorder.get(servicePair);
            }
            recorder.addRecord(record);
        }
    }
}
