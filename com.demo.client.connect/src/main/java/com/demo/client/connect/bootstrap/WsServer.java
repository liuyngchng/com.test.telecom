package com.demo.client.connect.bootstrap;

import com.demo.client.connect.ws.WebSocketChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket server.
 * @author Richard Liu
 * @since 2019.07.18
 */
public class WsServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsServer.class);

    /**
     * Listening port
     */
    private static final int PORT = 8080;

    public static void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketChannelInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            LOGGER.info("webSocket server listening for {}", PORT);
            channelFuture.channel().closeFuture().sync();
            LOGGER.info("webSocket server stop listening for {}", PORT);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        WsServer.start();
    }
}
