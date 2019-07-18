package com.test.telecom.ws;

import com.test.telecom.endpoint.Connection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * @author Richard Liu
 * @since 2019.07.18
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        if (null != msg  && msg instanceof FullHttpRequest) {
//            FullHttpRequest request = (FullHttpRequest) msg;
//            LOGGER.info("url is {}", request.uri());
//            String uid = request.uri().split("\\?")[1].split("=")[1];
//            LOGGER.info("user id is {}", uid);
//            Connection.clients.put(uid, ctx);
//        }
//        super.channelRead(ctx, msg);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Channel channel = ctx.channel();
        final String[] data = msg.text().split(":");
        if (null != data && data[0].equals("id")) {
            LOGGER.info("{} login", data[1]);
            Connection.clients.put(data[1], ctx);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(data[1] + "登录成功"));
        } else if (data.length < 2) {
            LOGGER.info("illegal data");
            ctx.channel().writeAndFlush(new TextWebSocketFrame("数据非法"));
        } else {
            LOGGER.info("address:{} | to:{} | msg:{} ", channel.remoteAddress(), data[0], data[1]);
            if (null == Connection.clients.get(data[0])) {
                LOGGER.info("user {} is offline", data[0]);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(data[0] + "当前不在线"));
            } else {
                Connection.clients.get(data[0]).channel().writeAndFlush(
                    new TextWebSocketFrame("time: " + LocalDateTime.now() + ", msg:" + data[1])
                );
                ctx.channel().writeAndFlush(new TextWebSocketFrame(data[0] + "发送成功"));
            }
//            ctx.channel().writeAndFlush(new TextWebSocketFrame("from: " + data[0] + ", time: " + LocalDateTime.now() + ", msg:" + data[1]));
        }
    }


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ChannelId" + ctx.channel().id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("用户下线: " + ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
