package com.abchina.core.handler;

import com.abchina.core.event.Event;
import com.abchina.core.reactor.MultiReactor;
import com.abchina.core.reactor.Reactor;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.abchina.core.reactor.Constants.BUFFER_MAX_IN;
import static com.abchina.core.reactor.Constants.BUFFER_MAX_OUT;
import static com.abchina.core.reactor.Constants.CLOSED;
import static com.abchina.core.reactor.Constants.CORE_NUM;
import static com.abchina.core.reactor.Constants.PROCESSING;
import static com.abchina.core.reactor.Constants.READING;
import static com.abchina.core.reactor.Constants.SENDING;


public abstract class BaseHandler implements Runnable {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(CORE_NUM);
    final SocketChannel socketChannel;
    protected final Selector selector;
    private final ServletContext context;
    private final HandlerEvent event;
    SelectionKey sk;
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_MAX_IN);
    ByteBuffer outputBuffer = ByteBuffer.allocate(BUFFER_MAX_OUT);
    ByteArrayBuffer rawRequest = new ByteArrayBuffer(0);
    private final Reactor reactor;
    private int state = READING;

    public HandlerEvent getEvent() {
        return event;
    }

    public Reactor getReactor() {
        return reactor;
    }

    /**
     * the constructor of handler.
     * we will register channel to selector and  wakeup it
     * and attach the this object prepare to use.
     */
    BaseHandler(ServletContext context) {
        this.context = context;
        this.reactor = context.getReactor();
        this.socketChannel = context.getChannel();
        this.selector = reactor.getSelector();
        this.event = new HandlerEvent(this);
    }

    protected int getState() {
        return state;
    }

    public ServletContext getContext() {
        return context;
    }

    void setState(int state) {
        this.state = state;
    }

    protected String getReactorName() {
        return reactor.getName();
    }

    public void run() {
        try {
            if (state == READING)
                read();
            else if (state == SENDING) {
                send();
            }

        } catch (Exception e) {
            // TODO use log
            e.printStackTrace();
        }
    }


    protected synchronized void read() throws Exception {
        inputBuffer.clear();
        int n = socketChannel.read(inputBuffer);
        if (inputIsComplete(inputBuffer, rawRequest, n)) {
            if (state != CLOSED) {
                state = PROCESSING;
                threadPool.execute(new Processor());
            } else {
                System.out.println("服务端关闭连接");
                sk.cancel();
                socketChannel.close();
            }

        }
    }

    protected void send() throws Exception {
        outputBuffer.flip();
        socketChannel.write(outputBuffer);
        MultiReactor.Acceptor acceptor = context.getAcceptor();
        acceptor.newInstanceOfHandler(socketChannel,reactor);

    }

    public abstract boolean inputIsComplete(ByteBuffer input, ByteArrayBuffer rawRequest, int bytes) throws IOException;

    private synchronized void processAndHandOff() throws Exception {
        state = SENDING;
        process(outputBuffer, rawRequest);
        sk.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();

    }

    public abstract void process(ByteBuffer output, ByteArrayBuffer request) throws Exception;

    class Processor implements Runnable {

        @Override
        public void run() {
            try {
                processAndHandOff();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  class HandlerEvent implements Event {
        private BaseHandler handler;

        public HandlerEvent(BaseHandler handler) {
            this.handler = handler;
        }

        @Override
        public void event() throws IOException {
            socketChannel.configureBlocking(false);
            handler.sk = socketChannel.register(selector, 0);
            sk.interestOps(SelectionKey.OP_READ);
            sk.attach(this);
        }

        public BaseHandler getHandler() {
            return handler;
        }
    }
}
