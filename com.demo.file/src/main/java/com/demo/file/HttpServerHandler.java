package com.demo.file;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerHandler.class);

    private ByteBufToBytes reader;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
        throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            httpRequest.headers().forEach(item -> LOGGER.info("headers {} = {}", item.getKey(), item.getValue()));
            ByteBuf content = httpRequest.content();
            if (HttpUtil.isContentLengthSet(httpRequest)) {
                reader = new ByteBufToBytes(
                    (int) HttpUtil.getContentLength(httpRequest));
            } else {
                reader = new ByteBufToBytes(1024);
            }
            if (content.isReadable()) {
                reader.reading(content);
                content.release();
                String resultStr = new String(reader.readFull());
                LOGGER.info("Client said: {}", resultStr);
                ctx.write(resultStr);
            }
            FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.wrappedBuffer("I am ok".getBytes()));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//            response.setDecoderResult(DecoderResult.SUCCESS);
            LOGGER.info("response is {}", response);
            ctx.write(response);

            ctx.flush();
        }
//        if (msg instanceof HttpRequest) {
//            HttpRequest request = (HttpRequest) msg;
//            LOGGER.info("{} {}", request.method(), request.uri());
//            request.headers().forEach(
//                item -> LOGGER.info("{} -> {}", item.getKey(), item.getValue())
//            );
//            if (HttpUtil.isContentLengthSet(request)) {
//                reader = new ByteBufToBytes(
//                    (int) HttpUtil.getContentLength(request));
//            } else {
//                reader = new ByteBufToBytes(1024);
//            }
//        } else if (msg instanceof HttpContent) {
//            HttpContent httpContent = (HttpContent) msg;
//            ByteBuf content = httpContent.content();
//            if (content.isReadable()) {
//                reader.reading(content);
//                content.release();
//                String resultStr = new String(reader.readFull());
//                LOGGER.info("Client said: {}", resultStr);
//                ctx.write(resultStr);
//            }
//            FullHttpResponse response = new DefaultFullHttpResponse(
//                HTTP_1_1, OK, Unpooled.wrappedBuffer("I am ok"
//                .getBytes()));
//            response.headers().set(CONTENT_TYPE, "text/plain");
//            response.headers().set(CONTENT_LENGTH,
//                response.content().readableBytes());
//            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
////            response.setDecoderResult(DecoderResult.SUCCESS);
//            ctx.write(response);
//
//            ctx.flush();
//        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
        throws Exception {
        LOGGER.error("error", cause);
        ctx.writeAndFlush(cause);
    }
}