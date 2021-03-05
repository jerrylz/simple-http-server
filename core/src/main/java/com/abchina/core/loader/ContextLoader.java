package com.abchina.core.loader;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.abchina.core.conf.Configuration;
import com.abchina.core.server.Context;
import com.abchina.core.server.HttpServer;
import com.abchina.core.server.ServerSource;
import com.abchina.core.utils.JarResourceParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContextLoader {

    private Log log = LogFactory.get(ContextLoader.class);

    private String rootPath = System.getProperty("user.dir");
    private String jarPath;
    private HttpServer httpServer;
    List<ServerSource> sources = new ArrayList<>();
    List<Context> contexts = new ArrayList<>();

    public ContextLoader(String buildPath, HttpServer httpServer) {
        jarPath = rootPath + buildPath;
        this.httpServer = httpServer;
    }

    public List<ServerSource> getSources() {
        return sources;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    public void load() throws Exception {
        loadSources();
        loadContexts();
        httpServer.setSources(sources);
        httpServer.setContexts(contexts);
    }

    void loadSources() throws IOException {
        File buildDir = new File(jarPath);
        File[] files;
        if (buildDir.isDirectory() && (files = buildDir.listFiles()) != null) {
            for (File file : files) {
                Configuration configuration = JarResourceParser.parseConfigFromJar(file);
                if (configuration == null) continue;
                ServerSource serverSource = new ServerSource(file.getName(), configuration);
                sources.add(serverSource);
                log.info("加载jar包{}中的资源", file.getName());
            }
        }
    }

    void loadContexts() throws Exception {
        for (ServerSource serverSource : sources) {
            Configuration config = serverSource.getConfig();
            String jarName = serverSource.getJarName();
            Context context = new Context(jarName, config);
            contexts.add(context);
        }
    }

    private void clear() throws IOException {
        httpServer.clear();
    }

    private void start() throws Exception {
        for (Context context : contexts) {
            context.init();
            context.start();
        }
    }

    public void reload() throws Exception {
        clear();
        load();
        start();
    }
}
