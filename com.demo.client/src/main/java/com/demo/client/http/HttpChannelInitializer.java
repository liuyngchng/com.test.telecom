package com.demo.client.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ch.pipeline().addLast(new HttpResponseEncoder());
		ch.pipeline().addLast(new HttpRequestDecoder());
		ch.pipeline().addLast(new HttpServerInboundHandler());

	}
}
