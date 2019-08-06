package com.demo.file.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

/**
 * Created by richard on 05/08/2019.
 */
public class ResponseUtil {
    private static final String SUCCESS = "SUCCESS";

    private ResponseUtil() {
    }

    private static final GeneralResponse notFoundGeneralResponse = new GeneralResponse(HttpResponseStatus.NOT_FOUND, "404 NOT_FOUND", null);

    private static final GeneralResponse successGeneralResponse = new GeneralResponse(HttpResponseStatus.OK, SUCCESS, null);

    public static void notFound(ChannelHandlerContext ctx, FullHttpRequest request) {
        responseJson(ctx, request, notFoundGeneralResponse);
    }

    public static void success(ChannelHandlerContext ctx, FullHttpRequest request) {
        responseJson(ctx, request, successGeneralResponse);
    }


    /**
     * 响应HTTP的请求
     *
     * @param ctx
     * @param request
     * @param generalResponse
     */
    public static void responseJson(ChannelHandlerContext ctx, HttpRequest request, GeneralResponse generalResponse) {
        ResponseUtil.response(ctx, request, JsonUtil.toJson(generalResponse), generalResponse.getStatus(), HttpHeaderValues.APPLICATION_JSON.toString());
    }


    public static void response(ChannelHandlerContext ctx, HttpRequest request, String content, HttpResponseStatus status, String contentType) {
        byte[] jsonByteByte = content.getBytes();
        FullHttpResponse responseJson = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            status,
            Unpooled.wrappedBuffer(jsonByteByte)
        );
        responseJson.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        responseJson.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, responseJson.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (!keepAlive) {
            ctx.write(responseJson).addListener(ChannelFutureListener.CLOSE);
        } else {
            responseJson.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(responseJson);
        }
        ctx.flush();
    }


}
