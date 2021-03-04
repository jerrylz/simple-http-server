package com.abchina.core.server.impl;

import com.abchina.http.servlet.Request;
import com.abchina.http.servlet.Response;
import com.abchina.http.servlet.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class NotFoundServlet implements Servlet {


    @Override
    public void service(Request request, Response response) throws IOException {
        OutputStream responseOutputStream = response.getResponseOutputStream();
        responseOutputStream.write("404-NOT-FOUND".getBytes());
    }
}
