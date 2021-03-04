package com.abchina.http.servlet;

import com.abchina.http.session.Session;

import java.util.Map;

public interface Request {
    String getMethod();

    Map<String, String> getHeaders();

    String getPath();

    String getBody();

    Session getSession();
}
