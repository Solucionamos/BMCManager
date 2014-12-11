package com.solucionamos.bmcmanager;

public interface AsyncResponse<Type> {
    void processFinish(BMCResponse param, Exception ex);
}
