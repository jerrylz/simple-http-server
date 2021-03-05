package com.abchina.http.core;

import com.abchina.http.io.ResponseOutPutStream;
import com.abchina.http.servlet.Response;
import com.google.common.primitives.Bytes;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponse implements Response {
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private static final String server = "SimpleHttpServer";
    private ByteBuffer bodyBuffer = ByteBuffer.allocate(1024*1024);
    private ByteBuffer outputBuffer;

    public HttpResponse(ByteBuffer outputBuffer) {
        this.outputBuffer = outputBuffer;
        this.statusCode = 200;
        this.headers.put("Server", server);
        this.headers.put("Content", "Keep-Alive");
        this.headers.put("Content-Type", "text/html");
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }


    @Override
    public OutputStream getResponseOutputStream() {
        return new ResponseOutPutStream(bodyBuffer);
    }

    public void putOutputBuffer() {
        String headLine = "HTTP/1.1 " + statusCode + " OK\r\n";
        StringBuilder headersBuilder = new StringBuilder();
        for (Map.Entry<String, String> element : headers.entrySet()) {
            String key = element.getKey();
            String value = element.getValue();
            headersBuilder.append(key);
            headersBuilder.append(":");
            headersBuilder.append(" ");
            headersBuilder.append(value);
            headersBuilder.append("\r\n");
        }
        headersBuilder.append("\r\n");
        outputBuffer.clear();
        int capacity = outputBuffer.capacity();
        outputBuffer.limit(capacity);
        outputBuffer.put(headLine.getBytes());
        outputBuffer.put(headersBuilder.toString().getBytes());
        bodyBuffer.flip();
        outputBuffer.put(bodyBuffer);
    }

    public byte[] getBodyBytes() {
        int capacity = bodyBuffer.capacity();
        bodyBuffer.flip();
        List<Byte> bytes = new ArrayList<>();
        while (bodyBuffer.hasRemaining()) {
            bytes.add(bodyBuffer.get());
        }
        bodyBuffer.limit(capacity);
        return Bytes.toArray(bytes);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }
}
