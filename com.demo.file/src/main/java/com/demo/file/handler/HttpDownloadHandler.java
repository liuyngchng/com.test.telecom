package com.demo.file.handler;

import com.demo.cluster.info.ClusterInfo;
import com.demo.cluster.model.ServiceInfo;
import com.demo.file.util.ConfigUtil;
import com.demo.file.util.DbUtil;
import com.demo.file.util.GeneralResponse;
import com.demo.file.util.ResponseUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by richard on 05/08/2019.
 */
public class HttpDownloadHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDownloadHandler.class);

    public HttpDownloadHandler() {
        super(false);
    }

    private String filePath = "/data/file/";

    private static final String USER_AGENT = "Mozilla/5.0";


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
            final String host = DbUtil.getHost(filename);
            if (host.equals(ConfigUtil.getLocalIp())) {
                LOGGER.info("get local file {} from {}", filename, host);
                this.getLocalFile(ctx, request, filename);
            } else {
                LOGGER.info("get remote file {} from {}", filename, host);
                this.getRemoteFile(ctx, filename, host);
            }
        } else {
            ctx.fireChannelRead(request);
        }
    }

    /**
     * 从远程机器获取IP
     * @param ctx
     * @param filename
     */
    private void getRemoteFile(ChannelHandlerContext ctx, String filename, String ip) {
        final List<ServiceInfo> serviceInfoList = ClusterInfo.getServiceInfo("file");
        AtomicBoolean isHostActive = new AtomicBoolean(false);
        serviceInfoList.forEach(item -> {
            if (item.getIp().equals(ip)) {
                isHostActive.set(true);
            }
        });
        if (!isHostActive.get()) {
            LOGGER.info("file server unavailable {}", ip);
            ctx.writeAndFlush("service unavailable");
            return;
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(String.format("http://%s:%s/download?file=%s", ip, ConfigUtil.port, filename));
        httpGet.addHeader("User-Agent", USER_AGENT);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            LOGGER.info("GET Response Status {}", httpResponse.getStatusLine().getStatusCode());

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();

            LOGGER.debug(response.toString());
            httpClient.close();
        } catch (IOException ex) {
            LOGGER.error("error", ex);
            ctx.writeAndFlush(null);
        }
        ctx.writeAndFlush(null);
    }

    /**
     * 获取本地文件
     * @param ctx
     * @param request
     * @param filename
     */
    private void getLocalFile(ChannelHandlerContext ctx, FullHttpRequest request, String filename) {
        GeneralResponse generalResponse;File file = new File(filePath + filename);
        try {


            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            long fileLength = raf.length();
            final HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
            response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", file.getName()));
            ctx.write(response);
            // use zero-copy file transfer.
            final ChannelFuture sendFileFuture = ctx.write(
                new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise()
            );
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
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        LOGGER.warn("{}", e);
        ctx.close();

    }
}
