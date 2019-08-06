package com.demo.file;

import com.demo.file.util.ConfigUtil;
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
 * Created by richard on 05/08/2019.
 */
public class FileServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServer.class);

    public static void start(String ip, int port) {
        ConfigUtil.port = port;
        ConfigUtil.localIP = ip;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //传递路由类
                .childHandler(new ServerInitializer());
            ChannelFuture f = b.bind(port).sync();
            LOGGER.info("fileServer listening for {}", port);
            f.channel().closeFuture().sync();
            LOGGER.info("fileServer stop listening for {}", port);
        } catch (Exception ex) {
            LOGGER.error("error", ex);
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        FileServer.start("192.168.0.1", 8999);
    }
}