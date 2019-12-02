package com.demo.client.connect.ws;import com.demo.client.connect.endpoint.Connection;import com.demo.common.enums.MailStatus;import com.demo.common.enums.Signal;import com.demo.common.model.Mail;import com.demo.common.model.Msg;import com.demo.msg.outbox.OutboxInfo;import com.demo.msg.inbox.InboxInfo;import com.google.common.base.Strings;import com.google.common.collect.Lists;import com.google.gson.Gson;import io.netty.channel.ChannelHandlerContext;import io.netty.channel.SimpleChannelInboundHandler;import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import java.util.*;/** * @author Richard Liu * @since 2019.07.18 */public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);    @Override    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {        super.channelRead(ctx, msg);    }    @Override    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {        if (null == frame || null == frame.text()) {            LOGGER.info("illegal data");            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("数据非法")));            return;        }        LOGGER.info("msg is {}", frame.text());        final Gson gson = new Gson();        final Msg msg = gson.fromJson(frame.text(), Msg.class);        if (null == msg || null == msg.getSignal()) {            LOGGER.info("illegal msg");            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("数据非法")));            return;        }        final Signal sig = Signal.getByName(msg.getSignal());        switch (sig) {            case LOGIN:                this.login(ctx, msg.getToken());                break;            case SEND_MSG:                this.sendMsg(ctx, msg);                break;            case PULL_MSG:                this.pullMsg(ctx);                break;            default:                break;        }    }    private void pullMsg(ChannelHandlerContext ctx) {        final String to = ctx.channel().attr(Connection.uidKey).get();        LOGGER.info("user {} pull_msg");        if (null == to) {            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("用户未登录")));            return;        }        final String text = this.mailList2msg(Lists.newArrayList(InboxInfo.mailIndex.get(to).values()));        ctx.channel().writeAndFlush(new TextWebSocketFrame(text));        LOGGER.info("user {} pull_msg", to);    }    private void sendMsg(ChannelHandlerContext ctx, Msg msg) {        if (null == msg || null == msg.getPayload()) {            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("数据非法")));            return;        }        final String from = ctx.channel().attr(Connection.uidKey).get();        if (null == from) {            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("用户未登录")));            return;        }        final Mail mail = this.msg2mail(from, msg.getPayload());        if (null == mail || Strings.isNullOrEmpty(mail.getText())) {            ctx.channel().writeAndFlush(new TextWebSocketFrame(this.buildNotify("数据非法")));            return;        }        saveMail(mail);        LOGGER.info(            "address:{} | from: {} | to:{} | msg:{} ",            ctx.channel().remoteAddress(),            mail.getFrom(),            mail.getTo(),            mail.getText()        );        if (null == Connection.clients.get(mail.getTo())) {            LOGGER.info("user {} is offline", mail.getTo());            ctx.channel().writeAndFlush(                new TextWebSocketFrame(                    this.buildNotify(mail.getTo() + "当前不在线,稍后投递")                )            );        } else {            Connection.clients.get(mail.getTo()).channel().writeAndFlush(                new TextWebSocketFrame(this.mail2msg(mail))            );            InboxInfo.mailIndex.get(mail.getTo()).get(mail.getId()).setStatus(MailStatus.DELIVERED.getValue());            ctx.channel().writeAndFlush(                new TextWebSocketFrame(                    this.buildNotify("给 " + mail.getTo() + " 的mail发送成功")                )            );        }    }    private void login(ChannelHandlerContext ctx, String s) {        final String uid = s;        LOGGER.info("{} login", uid);        Connection.clients.put(uid, ctx);        ctx.channel().attr(Connection.uidKey).set(uid);        final Msg msg = new Msg();        msg.setId(UUID.randomUUID().toString());        msg.setFrom("srv");        msg.setTo(uid);        msg.setSignal(Signal.NTF.getName());        msg.setPayload("{\"login\":\"success\"}");        Gson gson = new Gson();        ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(msg)));    }    @Override    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {        LOGGER.info("ChannelId {}", ctx.channel().id().asLongText());    }    @Override    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {        LOGGER.info("用户下线: {}", ctx.channel().id().asLongText());        ctx.channel().writeAndFlush(this.buildNotify("用户下线"));    }    @Override    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {        LOGGER.info("exception", cause);        ctx.channel().writeAndFlush(this.buildNotify(cause.getMessage()));        ctx.channel().close();    }    private void saveMail(Mail mail) {        if (null == OutboxInfo.mail.get(mail.getFrom())) {            OutboxInfo.mail.put(mail.getFrom(), new HashMap<>(8));        }        OutboxInfo.mail.get(mail.getFrom()).put(mail.getId(), mail);        if (null == InboxInfo.mailIndex.get(mail.getTo())) {            InboxInfo.mailIndex.put(mail.getTo(), new HashMap<>(8));        }        InboxInfo.mailIndex.get(mail.getTo()).put(mail.getId(), mail);    }    private Mail msg2mail(final String from, final String msg) {        final Gson gson = new Gson();        final Mail mail = gson.fromJson(msg, Mail.class);        mail.setFrom(from);        mail.setCreateTime(new Date());        return mail;    }    private String mail2msg(final Mail mail) {        final Gson gson = new Gson();        final Msg msg = new Msg();        msg.setSignal(Signal.SEND_MSG.getName());        msg.setFrom("server");        msg.setTo(mail.getTo());        msg.setPayload(gson.toJson(mail));        return gson.toJson(msg);    }    private String mailList2msg(final List<Mail> mail) {        final Gson gson = new Gson();        final Msg msg = new Msg();        msg.setSignal(Signal.SEND_MSG.getName());        msg.setFrom("server");        msg.setTo(mail.get(0).getTo());        final List<Mail> mails = new ArrayList<>(mail.size());        mail.forEach((v) -> {            if (v.getStatus() == MailStatus.DELIVERED.getValue()) {                return;            }            mails.add(v);            v.setDelivered();        });        msg.setPayload(gson.toJson(mails));        return gson.toJson(msg);    }    private String buildNotify(final String info) {        final Gson gson = new Gson();        final Msg msg = new Msg();        msg.setSignal(Signal.NTF.getName());        msg.setPayload("{\"info\":\"" + info + "\"}");        return gson.toJson(msg);    }}