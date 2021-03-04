package com.abchina.core;

import com.abchina.core.server.HttpServer;

/**
 * @Author: xiantang
 * @Date: 2020/4/25 20:57
 */
public class BootStrap {
    public static void main(String[] args) throws Exception {
        HttpServer httpServer = new HttpServer();
        httpServer.start();
    }
}
