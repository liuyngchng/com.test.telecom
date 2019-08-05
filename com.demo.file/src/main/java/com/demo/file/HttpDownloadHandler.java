package com.demo.file;

import com.demo.file.util.GeneralResponse;
import com.demo.file.util.ResponseUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by richard on 05/08/2019.
 */
public class HttpDownloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadHandler.class);

    public HttpDownloadHandler() {
        super(false);
    }

    private String filePath = "/data/file/";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        if (uri.startsWith("/download") && request.method().equals(HttpMethod.GET)) {
            GeneralResponse generalResponse;
            String filename = "";
            try {
                filename = request.uri().split("\\?")[1].split("=")[1].trim();
            } catch (IndexOutOfBoundsException ex) {
                LOGGER.error("error", ex);
                generalResponse = new GeneralResponse(HttpResponseStatus.BAD_REQUEST, "parameter error ", null);
                ResponseUtil.response(ctx, request, generalResponse);
                return;
            }

            File file = new File(filePath + filename);
            try {


                final RandomAccessFile raf = new RandomAccessFile(file, "r");
                long fileLength = raf.length();
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
                response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
                ctx.write(response);
                ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
                sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                    @Override
                    public void operationComplete(ChannelProgressiveFuture future)
                        throws Exception {
                        LOGGER.info("file {} transfer complete.", file.getName());
                        raf.close();
                    }

                    @Override
                    public void operationProgressed(ChannelProgressiveFuture future,
                                                    long progress, long total) throws Exception {
                        if (total < 0) {
                            LOGGER.warn("file {} transfer progress: {}", file.getName(), progress);
                        } else {
                            LOGGER.debug("file {} transfer progress: {}/{}", file.getName(), progress, total);
                        }
                    }
                });
                ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } catch (FileNotFoundException e) {
                LOGGER.warn("file {} not found", file.getPath());
                generalResponse = new GeneralResponse(HttpResponseStatus.NOT_FOUND, String.format("file %s not found", file.getPath()), null);
                ResponseUtil.response(ctx, request, generalResponse);
            } catch (IOException e) {
                LOGGER.warn("file {} has a IOException: {}", file.getName(), e.getMessage());
                generalResponse = new GeneralResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, String.format("读取 file %s 发生异常", filePath), null);
                ResponseUtil.response(ctx, request, generalResponse);
            }
        } else {
            ctx.fireChannelRead(request);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        LOGGER.warn("{}", e);
        ctx.close();

    }
}
