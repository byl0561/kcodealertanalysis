package com.kuaishou.kcode.monitor;

import com.kuaishou.kcode.common.AlertHandler;
import com.kuaishou.kcode.common.PathHandler;

import java.io.InputStream;

public interface Monitor {
    void prepare(InputStream inputStream, AlertHandler alertHandler, PathHandler pathHandler);
}
