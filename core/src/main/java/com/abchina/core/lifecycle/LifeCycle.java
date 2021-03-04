package com.abchina.core.lifecycle;

public interface LifeCycle {
    void init() throws Exception;

    void start() throws Exception;

    void destroy() throws Exception;
}
