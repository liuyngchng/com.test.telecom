package com.demo.file.util;

/**
 * Created by richard on 05/08/2019.
 */
public class ConfigUtil {

    public static String localIP = "192.168.0.1";

    public static int port = 8082;

    /**
     * 获取本机IP.
     * @return
     */
    public static String getLocalIp() {
        return localIP;
    }
}
