package com.abchina.core.server;

import com.abchina.core.conf.Configuration;
import com.abchina.http.utils.EqualsUtils;

public class ServerSource {
    private final String jarName;
    private final Configuration config;

    public ServerSource(String jarName, Configuration config) {
        this.jarName = jarName;
        this.config = config;
    }

    public String getJarName() {
        return jarName;
    }

    public Configuration getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ServerSource)) {
            return false;
        }

        ServerSource that = (ServerSource) obj;
        try {
            return EqualsUtils.OneDepthContentEquals(this, that);
        } catch (IllegalAccessException e) {
            // TODO use logging
            e.printStackTrace();
        }
        return false;
    }
}
