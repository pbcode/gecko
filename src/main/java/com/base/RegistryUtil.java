package com.base;

import java.net.UnknownHostException;


/**
 * Demo class
 *
 * @author pengbo3
 */
public class RegistryUtil {

    public final static String PROVIDER = "provider";

    public final static String CONSUMER = "consumer";
    public final static Integer PORT = 63914;

    public final static String IP = "127.0.0.1";

    public static String getChildPath(String remoteServiceName, String type) {
        return "/pbrpc/" + remoteServiceName + "/" + type;
    }

    public static String getLocalIp() throws UnknownHostException {
        return IP;

    }

    public static Integer getPort() {
        return PORT;
    }

}
