package com.entropylab.proxy;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ProxyServerManager {

    private static final ProxyServerManager INSTANCE = new ProxyServerManager();

    private HttpServer server;

    public ProxyServerManager() {
    }

    public static ProxyServerManager getInstance() {
        return INSTANCE;
    }

    public synchronized void start(int port) throws IOException {
        HttpServer newServer = HttpServer.create(new InetSocketAddress(port), 0);
        newServer.setExecutor(Executors.newFixedThreadPool(10));
        newServer.createContext("/", new ProxyRequestHandler());
        newServer.start();
        this.server = newServer;
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public synchronized boolean isRunning() {
        return server != null;
    }
}
