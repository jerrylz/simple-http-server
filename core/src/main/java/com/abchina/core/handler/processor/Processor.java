package com.abchina.core.handler.processor;

import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;

public interface Processor {
    void process(HttpRequest request, HttpResponse response) throws Exception;
}
