package com.abchina.core.reactor;

import com.abchina.core.event.Event;
import com.abchina.core.handler.BaseHandler;
import com.abchina.core.handler.ServletContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiReactor implements Runnable {

    private final int subReactorCount;
    private final Reactor[] subReactors;
    private final MainReactorImpl mainReactor;
    private final AtomicInteger loadBalancingInteger = new AtomicInteger();
    private final ServletContext servletContext;

    private MultiReactor(String mainReactorName, int port, Class<? extends BaseHandler> handlerClass, int subReactorCount, ServletContext context) throws IOException {
        this.subReactorCount = subReactorCount;
        this.mainReactor = new MainReactorImpl(mainReactorName, port, handlerClass);
        this.subReactors = new Reactor[subReactorCount];
        this.loadBalancingInteger.set(1);
        this.servletContext = context == null ? ServletContext.emptyContext() : context;
        for (int i = 0; i < subReactorCount; i++) {
            this.subReactors[i] = new SubReactorImpl("subReactor-" + i, Selector.open());
        }

        Acceptor acceptor = mainReactor.getAcceptor();
        servletContext.setAcceptor(acceptor);

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void run() {
        for (int i = 0; i < subReactorCount; i++) {
            new Thread(subReactors[i]).start();
        }
        new Thread(mainReactor).start();
    }

    public void stop() throws IOException {
        mainReactor.stop();
        for (int i = 0; i < subReactorCount; i++) {
            subReactors[i].stop();
        }
    }


    public static class Builder {
        private int port;
        private Class<? extends BaseHandler> handlerClass;
        private String mainReactorName = "DefaultMainReactor";
        private int subReactorCount;
        private ServletContext servletContext;

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setHandlerClass(Class<? extends BaseHandler> handlerClass) {
            this.handlerClass = handlerClass;
            return this;
        }

        public Builder setMainReactorName(String name) {
            this.mainReactorName = name;
            return this;
        }

        public MultiReactor build() throws IOException {
            return new MultiReactor(mainReactorName, port, handlerClass, subReactorCount, servletContext);
        }


        public Builder setSubReactorCount(int count) {
            this.subReactorCount = count;
            return this;
        }

        public Builder setServletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }
    }

    /**
     * this is the endpoint of the reactor
     * if acceptor got a connection form the
     * client will create a certain handler and
     * will gain a instance by reflect.
     */
    public class Acceptor implements Runnable {
        private final ServerSocketChannel serverSocket;
        private final Class<? extends BaseHandler> handlerClass;

        Acceptor(ServerSocketChannel serverSocket, Class<? extends BaseHandler> handlerClass) {
            this.serverSocket = serverSocket;
            this.handlerClass = handlerClass;
        }

        @Override
        public void run() {
            SocketChannel channel;
            try {
                channel = serverSocket.accept();
                if (channel != null) {
                    Reactor subReactor = subReactors[loadBalancingInteger.incrementAndGet() % subReactorCount];
                    newInstanceOfHandler(channel,subReactor);
                }
            } catch (Exception e) {
                //TODO log error
                e.printStackTrace();
            }

        }

        void stop() throws IOException {
            serverSocket.close();
        }

        public void newInstanceOfHandler(SocketChannel channel, Reactor reactor) throws Exception {
            Constructor<? extends BaseHandler> handler
                    = handlerClass.getConstructor(ServletContext.class);
            servletContext.setChannel(channel);
            servletContext.setReactor(reactor);
            BaseHandler baseHandler = handler.newInstance(servletContext);
            BaseHandler.HandlerEvent event = baseHandler.getEvent();
            reactor.register(event);
        }
    }


    public class MainReactorImpl implements Reactor {
        private final String mainReactorName;
        private final Selector selector;
        private volatile boolean isRunning = true;
        private final Acceptor acceptor;



        MainReactorImpl(String mainReactorName, int port, Class<? extends BaseHandler> handlerClass) throws IOException {
            this.mainReactorName = mainReactorName;
            this.selector = Selector.open();
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);
            SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            acceptor = new Acceptor(serverSocket, handlerClass);
            sk.attach(acceptor);

        }

        public String getName() {
            return mainReactorName;
        }

        @Override
        public Selector getSelector() {
            return selector;
        }

        @Override
        public void register(Event event) {
            throw new NoSuchMethodError("MainReactor not implement register method.");
        }

        public void run() {
            try {
                while (!Thread.interrupted() && isRunning) {
                    selector.select();
                    Set<SelectionKey>  selected = selector.selectedKeys();
                    HashSet<SelectionKey> selectionKeys = new HashSet<>(selected);
                    selected.clear();
                    for (SelectionKey key : selectionKeys) dispatch(key);
                    selectionKeys.clear();
                }
            } catch (IOException e) {
                //TODO log error
                e.printStackTrace();
            }
        }

        private void dispatch(SelectionKey k) {
            Runnable acceptor = (Runnable) (k.attachment());
            if (acceptor != null)
                acceptor.run();
        }

        public void stop() throws IOException {
            isRunning = false;
            Set<SelectionKey> keys = selector.keys();
            for (SelectionKey key : keys) {
                SelectableChannel channel = key.channel();
                channel.close();
            }
            acceptor.stop();
        }

        public Acceptor getAcceptor() {
            return acceptor;
        }
    }


    public static class SubReactorImpl implements Reactor {
        private final Queue<Event> events = new ConcurrentLinkedQueue<>();
        private final Selector subSelector;
        private final String name;
        private volatile boolean isRunning = true;


        SubReactorImpl(String name, Selector subSelector) {
            this.name = name;
            this.subSelector = subSelector;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted() && isRunning) {
                    Event event;
                    while ((event = events.poll()) != null) {
                        event.event();
                    }
                    subSelector.select();
                    Set<SelectionKey> selected = subSelector.selectedKeys();
                    for (SelectionKey key : selected)
                        dispatch(key);
                    selected.clear();
                }
            } catch (IOException e) {
                //TODO log error
                e.printStackTrace();
            }
        }

        private void dispatch(SelectionKey key) {
            Event event = (Event) (key.attachment());
            BaseHandler handler = event.getHandler();
            if (handler != null)
                handler.run();
        }

        @Override
        public void register(Event event) {
            events.offer(event);
            subSelector.wakeup();
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public Selector getSelector() {
            return subSelector;
        }


        @Override
        public void stop() throws IOException {
            isRunning = false;
            Set<SelectionKey> keys = subSelector.keys();
            for (SelectionKey key : keys) {
                SelectableChannel channel = key.channel();
                channel.close();
            }
        }
    }

}

