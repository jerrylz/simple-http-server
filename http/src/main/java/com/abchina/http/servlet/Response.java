package com.abchina.http.servlet;

import java.io.OutputStream;

public interface Response {

    OutputStream getResponseOutputStream();
}
