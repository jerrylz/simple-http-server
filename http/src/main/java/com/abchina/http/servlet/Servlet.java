package com.abchina.http.servlet;

public interface Servlet {
    void service(Request request, Response response) throws Exception;
}
