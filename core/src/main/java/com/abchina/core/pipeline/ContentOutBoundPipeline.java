package com.abchina.core.pipeline;

import com.abchina.core.handler.ServletContext;
import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;

public class ContentOutBoundPipeline extends BoundPipeline {
    public ContentOutBoundPipeline(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) {
        int length = response.getBodyBytes().length;
        response.addHeader("Content-Length",length+"");
    }
}
