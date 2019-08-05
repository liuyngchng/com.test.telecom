package com.demo.file.handler;

import com.demo.file.util.ConfigUtil;
import com.demo.file.util.DbUtil;
import com.demo.file.util.GeneralResponse;
import com.demo.file.util.ResponseUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class HttUploadHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttUploadHandler.class);

    /**
     * true: 将数据缓存至磁盘，false: 将数据缓存在memory
     */
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final String FILE_UPLOAD = "/data/";

    private static final String URI = "/upload";

    private HttpPostRequestDecoder httpDecoder;




    private HttpRequest request;

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject)
        throws Exception {
        if (httpObject instanceof HttpRequest) {
            this.request = (HttpRequest) httpObject;
            if (this.request.uri().startsWith(URI) && this.request.method().equals(HttpMethod.POST)) {
                this.httpDecoder = new HttpPostRequestDecoder(factory, request);
                this.httpDecoder.setDiscardThreshold(0);
            } else {
                //传递给下一个Handler
                ctx.fireChannelRead(httpObject);
            }
        }
        if (httpObject instanceof HttpContent) {
            if (this.httpDecoder != null) {
                final HttpContent chunk = (HttpContent) httpObject;
                this.httpDecoder.offer(chunk);
                if (chunk instanceof LastHttpContent) {
                    this.writeChunk(ctx);
                    this.httpDecoder.destroy();
                    this.httpDecoder = null;
                }
                ReferenceCountUtil.release(httpObject);
            } else {
                ctx.fireChannelRead(httpObject);
            }
        }

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

    private void writeChunk(ChannelHandlerContext ctx) throws IOException {
        while (this.httpDecoder.hasNext()) {
            InterfaceHttpData data = this.httpDecoder.next();
            if (data != null && InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
                final FileUpload fileUpload = (FileUpload) data;
                final File file = new File(FILE_UPLOAD + fileUpload.getFilename());
                LOGGER.info("upload file: {}", file);
                try (FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                     FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
                    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                    ResponseUtil.response(ctx, this.request, new GeneralResponse(HttpResponseStatus.OK, "SUCCESS", null));
                }
            }
        }
    }

    /**
     * 保存文件元数据.
     * @param fileId
     */
    private void saveFileMetadata(String fileId) {
        DbUtil.saveMetadata(fileId, ConfigUtil.getLocalIp());
    }
}