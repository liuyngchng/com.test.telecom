package com.test.telecom.ws;

import com.test.telecom.endpoint.Connection;
import com.test.telecom.enums.MailState;
import com.test.telecom.enums.Signal;
import com.test.telecom.inbox.InboxInfo;
import com.test.telecom.model.Mail;
import com.test.telecom.outbox.OutboxInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
        final String[] data = msg.text().split(":");
        if (null == data || data.length < 2) {
            LOGGER.info("illegal data");
            ctx.channel().writeAndFlush(new TextWebSocketFrame("数据非法"));
            return;
        }
        final Signal sig = Signal.getByName(data[0].trim());
        switch (sig) {
            case LOGIN:
                this.login(ctx, data[1]);
                break;
            case SEND_MSG:
                this.sendMsg(ctx, data);
                break;
            case PULL_MSG:
                this.pullMsg(ctx, data);
                break;
            default:
                break;
        }
    }

    private void pullMsg(ChannelHandlerContext ctx, String[] data) {
        final String to  = ctx.channel().attr(Connection.uidKey).get();
        if (null == to) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("info:用户未登录"));
            return;
        }
        StringBuffer sb = new StringBuffer(16);
        sb.append("mail:");
        InboxInfo.mail.get(to).forEach((k, v) -> {
            if (v.getState() == MailState.DELIVERED.getValue()) {
                return;
            }
            sb.append("time: " + v.getDate() + ", from:" + v.getFrom() + ", msg:" + v.getText() + "\r\n");
            v.setDelivered();
            ctx.channel().writeAndFlush(new TextWebSocketFrame(sb.toString()));
        });
    }

    private void sendMsg(ChannelHandlerContext ctx, String[] data) {
        if (data.length < 3) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("info:数据非法"));
            return;
        }
        final String from  = ctx.channel().attr(Connection.uidKey).get();
        if (null == from) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("info:用户未登录"));
            return;
        }
        final Mail mail = this.buildMail(from, data);
        saveMail(mail);
        LOGGER.info(
            "address:{} | from: {} | to:{} | msg:{} ",
            ctx.channel().remoteAddress(),
            mail.getFrom(),
            mail.getTo(),
            mail.getText()
        );
        if (null == Connection.clients.get(mail.getTo())) {
            LOGGER.info("user {} is offline", mail.getTo());
            ctx.channel().writeAndFlush(new TextWebSocketFrame(mail.getTo() + "当前不在线,稍后投递"));
        } else {
            Connection.clients.get(mail.getTo()).channel().writeAndFlush(
                new TextWebSocketFrame("time: " + mail.getDate() + ", from:" + mail.getFrom() + ", msg:" + mail.getText())
            );
            InboxInfo.mail.get(mail.getTo()).get(mail.getId()).setState(MailState.DELIVERED.getValue());
            ctx.channel().writeAndFlush(new TextWebSocketFrame("给 " + mail.getTo() + "的mail发送成功"));
        }
    }

    private void login(ChannelHandlerContext ctx, String s) {
        final String uid = s;
        LOGGER.info("{} login", uid);
        Connection.clients.put(uid, ctx);

        ctx.channel().attr(Connection.uidKey).set(uid);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(uid + "登录成功"));
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

    private void saveMail(Mail mail) {
        if (null == OutboxInfo.mail.get(mail.getFrom())) {
            OutboxInfo.mail.put(mail.getFrom(), new HashMap<>(8));
        }
        OutboxInfo.mail.get(mail.getFrom()).put(mail.getId(),mail);
        if (null == InboxInfo.mail.get(mail.getTo())) {
            InboxInfo.mail.put(mail.getTo(), new HashMap<>(8));
        }
        InboxInfo.mail.get(mail.getTo()).put(mail.getId(),mail);
    }

    private Mail buildMail(final String from, final String[] data) {
        final Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTo(data[1]);
        mail.setDate(new Date());
        mail.setText(data[2]);
        return mail;
    }
}
