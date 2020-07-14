package com.kuaishou.kcode.utils;

import com.kuaishou.kcode.common.Pool;
import com.kuaishou.kcode.common.ServiceRecorder;

public class ServiceRecorderPool {
    private ServiceRecorderPool(){};

    private static Pool<ServiceRecorder> serviceRecorderHolder = new Pool<>();

    public synchronized static ServiceRecorder get(){
        if (serviceRecorderHolder.isEmpty()){
            return new ServiceRecorder();
        }
        else {
            ServiceRecorder serviceRecorder = serviceRecorderHolder.get();
            serviceRecorder.init();
            return serviceRecorder;
        }
    }

    public synchronized static void put(ServiceRecorder serviceRecorder){
        serviceRecorderHolder.put(serviceRecorder);
    }
}
