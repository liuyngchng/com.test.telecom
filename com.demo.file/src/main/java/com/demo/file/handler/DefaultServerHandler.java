package com.demo.file.handler;

import com.demo.file.util.GeneralResponse;
import com.demo.file.util.ResponseUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URI;

/**
 * Created by richard on 05/08/2019.
 */
public class DefaultServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerHandler.class);

    private static final String UPLOAD_URI = "/upload";

    private static final String location = "static/";

    // 404文件页面地址
    private static final File NOT_FOUND = new File(DefaultServerHandler.class.getClassLoader().getResource(location + "404.html").getPath());


    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        LOGGER.info("url is {}", request.uri());
        if (request.uri().startsWith(UPLOAD_URI) && request.method().equals(HttpMethod.GET)) {
            try {
                this.feedStaticContent(ctx, request);
            } catch (Exception ex) {
                LOGGER.error("error", ex);
                ctx.close();
            }
        } else {
            GeneralResponse generalResponse = new GeneralResponse(HttpResponseStatus.BAD_REQUEST, "请检查你的请求方法及url", null);
            ResponseUtil.responseJson(ctx, request, generalResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        LOGGER.warn("{}", e);
        ctx.close();
    }

    private void feedStaticContent(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 获取URI
        String uri = request.uri().replace("/", "");
        URI url = new URI(uri);
        uri = url.getPath();
        // 设置不支持favicon.ico文件
        if ("favicon.ico".equals(uri)) {
            return;
        }
        // 根据路径地址构建文件
        File html;
        String path = "";
        try {
            path = this.getClass().getClassLoader().getResource(location + uri).getPath();
            html = new File(path);
        } catch (Exception ex) {
            LOGGER.error("file resource can't found for {}", uri);
            path = "static/404.html";
            html = new File("undefined");
        }
        // 状态为1xx的话，继续请求
        if (HttpUtil.is100ContinueExpected(request)) {
            send100Continue(ctx);
        }

        // 当文件不存在的时候，将资源指向NOT_FOUND
        if (!html.exists()) {
            html = NOT_FOUND;
        }

        RandomAccessFile file = new RandomAccessFile(html, "r");
        HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);

        // 文件没有发现设置状态为404
        if (html == NOT_FOUND) {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
        }

        // 设置文件格式内容
        if (path.endsWith(".html")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        } else if (path.endsWith(".js")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
        } else if(path.endsWith(".css")) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request);

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);

        if (ctx.pipeline().get(SslHandler.class) == null) {
            ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
        } else {
            ctx.write(new ChunkedNioFile(file.getChannel()));
        }
        // 写入文件尾部
        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
        file.close();
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
        ctx.writeAndFlush(response);
    }
}
