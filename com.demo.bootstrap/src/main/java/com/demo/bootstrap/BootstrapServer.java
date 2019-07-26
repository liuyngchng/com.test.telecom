package com.demo.bootstrap;


import com.demo.client.connect.bootstrap.WsServer;
import com.demo.cluster.manager.bootstrap.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by richard on 26/07/2019.
 */
public class BootstrapServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapServer.class);

    public static void main(String[] args) {
        LOGGER.info("BootstrapServer start.");
        Thread thread1 = new Thread(()->{
            WsServer.start();
        });
        thread1.start();
        Thread thread2 = new Thread(()->{
            ClusterManager.start();
        });
        thread2.start();
        LOGGER.info("all service started.");

    }
}
