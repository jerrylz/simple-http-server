package com.abchina.core.handler.processor;

import com.abchina.core.handler.ServletContext;
import com.abchina.core.loader.WebAppLoader;
import com.abchina.core.server.ServletWrapper;
import com.abchina.core.server.impl.NotFoundServlet;
import com.abchina.core.utils.CastUtils;
import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;
import com.abchina.http.servlet.Servlet;

import java.lang.reflect.Constructor;
import java.util.Map;

public class HttpServletProcessor implements Processor{
    private ServletContext context;

    HttpServletProcessor(ServletContext context) {
        this.context = context;
    }

    @Override
    public void process(HttpRequest request, HttpResponse response) throws Exception {
        Map<String, ServletWrapper> mapper = context.getMapper();
        ServletWrapper wrapper = mapper.get(request.getPath());
        Servlet servlet;
        if (wrapper == null) {
            servlet = new NotFoundServlet();
        }else{
            servlet = wrapper.getServlet();
            if (servlet == null) {
                String className = wrapper.getClassName();
                WebAppLoader loader = context.getLoader();
                Class<? extends Servlet> servletClass = CastUtils.cast(loader.loadClass(className));
                Constructor constructor = servletClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object obj = constructor.newInstance();
                servlet = (Servlet)obj;
                wrapper.setServlet(servlet);
                wrapper.setServletClass(servletClass);
            }
        }
        servlet.service(request, response);
    }
}
