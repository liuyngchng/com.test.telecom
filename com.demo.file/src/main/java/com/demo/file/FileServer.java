package com.demo.file;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by richard on 05/08/2019.
 */
public class FileServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServer.class);

    public static void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                        throws Exception {

                        ch.pipeline().addLast(new HttpResponseEncoder());
                        ch.pipeline().addLast(new HttpRequestDecoder());
                        ch.pipeline().addLast(new HttpObjectAggregator(65535));
                        ch.pipeline().addLast(new HttpServerHandler());
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
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
        FileServer.start(8999);
    }
}