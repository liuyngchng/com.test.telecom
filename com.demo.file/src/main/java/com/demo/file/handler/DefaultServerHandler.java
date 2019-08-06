package com.demo.file.handler;

import com.demo.file.util.GeneralResponse;
import com.demo.file.util.ResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by richard on 05/08/2019.
 */
public class DefaultServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerHandler.class);


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        LOGGER.info("url is {}", request.uri());
        GeneralResponse generalResponse = new GeneralResponse(HttpResponseStatus.BAD_REQUEST, "请检查你的请求方法及url", null);
        ResponseUtil.response(ctx, request, generalResponse);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        LOGGER.warn("{}", e);
        ctx.close();
    }
}
