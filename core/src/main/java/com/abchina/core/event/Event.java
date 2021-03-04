package com.abchina.core.event;

import com.abchina.core.handler.BaseHandler;

import java.io.IOException;

public interface Event {
    void event() throws IOException;
    BaseHandler getHandler();
}
