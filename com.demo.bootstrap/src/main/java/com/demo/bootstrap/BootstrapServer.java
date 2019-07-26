package com.demo.bootstrap;


import com.demo.client.connect.bootstrap.WsServer;
import com.demo.cluster.manager.bootstrap.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by richard on 26/07/2019.
 */
public class BootstrapServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapServer.class);

    public static void main(String[] args) {
        LOGGER.info("BootstrapServer start.");
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            WsServer.start();
        });
        executorService.submit(() -> {
            ClusterManager.start();
        });
        LOGGER.info("all service started.");

    }
}
