package com.abchina.core.conf;

import com.abchina.core.server.ServletWrapper;
import com.abchina.http.utils.EqualsUtils;

import java.util.Map;

public class Configuration {
    private int port;
    private int subReactorNum;
    private Map<String, ServletWrapper> router;

    public Configuration(int port, int subReactorNum, Map<String, ServletWrapper> router) {
        this.port = port;
        this.subReactorNum = subReactorNum;
        this.router = router;
    }

    public int getSubReactorNum() {
        return subReactorNum;
    }

    public Map<String, ServletWrapper> getRouter() {
        return router;
    }

    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Configuration)) {
            return false;
        }

        Configuration that = (Configuration) obj;
        try {
            return EqualsUtils.OneDepthContentEquals(this, that);
        } catch (IllegalAccessException e) {
            // TODO use logging
            e.printStackTrace();
        }
        return false;
    }
}
