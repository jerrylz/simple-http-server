package com.abchina.core.pipeline;

import com.abchina.core.handler.ServletContext;
import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;

public class RootBoundPipeline extends BoundPipeline {
    private ServletContext context;

    public RootBoundPipeline(ServletContext context) {
        super(context);
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) {
        Pipeline nextPipeLine = getNext();
        if (nextPipeLine != null) {
            nextPipeLine.doHandle(request, response);
        }
    }
}
