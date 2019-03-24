package com.base;

import java.net.UnknownHostException;


/**
 * Demo class
 *
 * @author pengbo3
 */
public interface HttpServer {

    void start() throws UnknownHostException, InterruptedException;

    void stop();
}
