package com.abchina.core.scanner.listener;

import com.abchina.core.scanner.Event;

public interface Listener {
    void onEvent(Event event) throws Exception;
}
