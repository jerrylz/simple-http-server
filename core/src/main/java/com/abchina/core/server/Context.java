package com.abchina.core.server;

import com.abchina.core.conf.Configuration;
import com.abchina.core.handler.HttpHandler;
import com.abchina.core.handler.ServletContext;
import com.abchina.core.lifecycle.LifeCycle;
import com.abchina.core.loader.JarClassLoader;
import com.abchina.core.loader.WebAppLoader;
import com.abchina.core.reactor.MultiReactor;

import java.io.IOException;
import java.util.Map;

public class Context implements LifeCycle {
    private final int subReactorNum;
    private int port;
    private Map<String, ServletWrapper> mapper;
    private String jarName;
    private Configuration configuration;
    private MultiReactor reactor;
    private WebAppLoader webAppLoader;
    private ServletContext servletContext;

    @Override
    public void init() throws Exception {
        loadOnStartUpServlet();
        servletContext = ServletContext.contextWithMapperAndClassLoader(mapper, webAppLoader, jarName);
        this.reactor = MultiReactor.newBuilder()
                .setPort(port)
                .setHandlerClass(HttpHandler.class)
                .setMainReactorName("MainReactor")
                .setServletContext(servletContext)
                .setSubReactorCount(subReactorNum)
                .build();
    }


    @Override
    public void start() {
        new Thread(reactor).start();
    }


    @Override
    public void destroy() throws IOException {
        this.reactor.stop();
    }


    public Context(String jarName, Configuration configuration) throws Exception {
        this.jarName = jarName;
        this.configuration = configuration;
        this.mapper = configuration.getRouter();
        this.port = configuration.getPort();
        subReactorNum = configuration.getSubReactorNum();
        JarClassLoader jarClassLoader = new JarClassLoader(jarName);
        this.webAppLoader = new WebAppLoader(mapper, jarClassLoader);


    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    Map<String, ServletWrapper> getMapper() {
        return mapper;
    }

    private void loadOnStartUpServlet() throws Exception {
        webAppLoader.loadOnStartUp();
    }

    public void setPort(int port) {
        this.port = port;
    }


}
