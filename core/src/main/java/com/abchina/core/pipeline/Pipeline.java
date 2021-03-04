package com.abchina.core.pipeline;

import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;

public interface Pipeline {


    void doHandle(HttpRequest request, HttpResponse response);
}
