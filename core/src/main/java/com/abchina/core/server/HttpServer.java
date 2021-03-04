package com.abchina.core.server;

import com.abchina.core.lifecycle.LifeCycle;
import com.abchina.core.loader.ContextLoader;
import com.abchina.core.scanner.FileScanner;
import com.abchina.core.scanner.listener.FileModifyListener;
import com.abchina.core.scanner.listener.Listener;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HttpServer implements LifeCycle {
    private final FileScanner fileScanner;
    private List<Context> contexts;
    private List<ServerSource> sources;
    private final ContextLoader contextLoader;

    public HttpServer() throws Exception {
        this("/build");
    }

    public HttpServer(String buildPath) throws Exception {
        contextLoader = new ContextLoader(buildPath, this);
        File file = new File(buildPath.replaceFirst("/", ""));
        fileScanner = new FileScanner(file,100);
        init();
    }

    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
    }

    public void setSources(List<ServerSource> sources) {
        this.sources = sources;
    }

    @Override
    public void init() throws Exception {
        //加载应用上下文
        contextLoader.load();
        //初始化文件监听器
        Listener listener = new FileModifyListener(contextLoader);
        //注册监听器
        fileScanner.registerListener(listener);
    }

    @Override
    public void start() throws Exception {
        for (Context context : contexts) {
            context.init();
            context.start();
        }
        new Thread(fileScanner).start();

    }

    @Override
    public void destroy() throws Exception {
        for (Context context : contexts) {
            context.destroy();
        }
    }


    List<ServerSource> getServerSources() {
        return Collections.unmodifiableList(sources);
    }


    public List<Context> getContexts() {
        return contextLoader.getContexts();
    }


    public void clear() throws IOException {

        for (Context context : contexts) {
            context.destroy();
        }
        contexts.clear();
        sources.clear();
    }
}
