package com.abchina.core.handler;

import com.abchina.core.handler.processor.HttpDispatchProcessor;
import com.abchina.http.core.HttpRequest;
import com.abchina.http.core.HttpResponse;
import com.abchina.http.parser.HttpRequestParser;
import org.apache.http.util.ByteArrayBuffer;

import java.io.EOFException;
import java.nio.ByteBuffer;

import static com.abchina.core.reactor.Constants.CLOSED;
import static com.abchina.core.reactor.Constants.SENDING;

public class HttpHandler extends BaseHandler {
    /**
     * the constructor of handler.
     * we will register channel to selector and  wakeup it
     * and attach the this object prepare to use.
     *
     * @param context
     */
    public HttpHandler(ServletContext context) {
        super(context);
    }

    @Override
    public boolean inputIsComplete(ByteBuffer input, ByteArrayBuffer rawRequest, int bytes) {
        if (bytes > 0) {
            input.flip();
            while (input.hasRemaining()) {
                byte ch = input.get();
                if (ch == 3) {
                    setState(CLOSED);
                    return true;
                } else {
                    rawRequest.append((char) ch);
                }
            }
        } else if (bytes == -1) {
            setState(CLOSED);
            return true;
        }

        return true;
    }

    @Override
    public void process(ByteBuffer output, ByteArrayBuffer rawRequest) throws Exception {
        int state = getState();
        if (state == CLOSED) {
            throw new EOFException();
        } else if (state == SENDING) {
            HttpRequestParser parser = new HttpRequestParser(rawRequest.toByteArray(), HttpRequest.newBuilder());
            HttpRequest request = parser.parse();
            HttpResponse response = new HttpResponse(outputBuffer);
            HttpDispatchProcessor processor = new HttpDispatchProcessor(getContext());
            processor.process(request, response);
            response.putOutputBuffer();
        }
    }


}
