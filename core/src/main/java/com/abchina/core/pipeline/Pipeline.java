package com.abchina.core.pipeline;

import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;

/**
 * @Author: xiantang
 * @Date: 2020/4/26 23:04
 */
public interface Pipeline {


    void doHandle(HttpRequest request, HttpResponse response);
}
