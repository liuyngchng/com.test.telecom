package com.demo.client.connect.bootstrap;

import com.demo.client.connect.ws.WebSocketChannelInitializer;
import com.sun.org.omg.CORBA.ExcDescriptionSeqHelper;
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


    public static void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketChannelInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            LOGGER.info("webSocket server listening for {}", port);
            channelFuture.channel().closeFuture().sync();
            LOGGER.info("webSocket server stop listening for {}", port);
        } catch (Exception ex) {
            LOGGER.error("error", ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        WsServer.start(8081);
    }
}
