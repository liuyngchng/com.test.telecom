package com.demo.file;

import com.demo.file.handler.HttUploadHandler;
import com.demo.file.handler.HttpDownloadHandler;
import com.demo.file.handler.DefaultServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * Created by richard on 05/08/2019.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    public void initChannel(SocketChannel ch) {
        EventExecutorGroup executors = new DefaultEventExecutorGroup(1);
        //HTTP 服务的解码器
        ch.pipeline().addLast(new HttpServerCodec(4096, 8192, 1024 * 1024 * 10));
        // 用于上传文件
        ch.pipeline().addLast(executors, new HttUploadHandler());
        //HTTP 消息的合并处理
        ch.pipeline().addLast(new HttpObjectAggregator(10 * 1024));
        // 新增ChunkedHandler，主要作用是支持异步发送大的码流（例如大文件传输），但是不占用过多的内存，防止发生java内存溢出错误
        ch.pipeline().addLast(new ChunkedWriteHandler());
        // 用于下载文件
        ch.pipeline().addLast(new HttpDownloadHandler());
        //服务器端逻辑处理
        ch.pipeline().addLast(new DefaultServerHandler());
    }
}