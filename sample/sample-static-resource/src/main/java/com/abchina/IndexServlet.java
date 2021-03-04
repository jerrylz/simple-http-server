package com.abchina;

import com.abchina.http.servlet.Request;
import com.abchina.http.servlet.Response;
import com.abchina.http.servlet.Servlet;

import java.io.IOException;
import java.io.OutputStream;

public class IndexServlet implements Servlet {
    @Override
    public void service(Request request, Response response) throws IOException {
        OutputStream responseOutputStream = response.getResponseOutputStream();
        responseOutputStream.write("/".getBytes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof IndexServlet;
    }
}
