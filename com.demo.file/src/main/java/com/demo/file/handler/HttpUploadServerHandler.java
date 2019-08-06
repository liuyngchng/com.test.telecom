package com.demo.file.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static io.netty.buffer.Unpooled.*;


/**
 * Created by richard on 05/08/2019.
 * @see {https://blog.csdn.net/qq_25310669/article/details/88629430}
 */
public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUploadServerHandler.class);

    //浏览器发来的request请求，转化为了这个对象
    private HttpRequest request;

    private boolean readingChunks;

    private final StringBuilder responseContent = new StringBuilder();

    private static final HttpDataFactory factory =
        new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
    //发文件需要post请求，过来数据之后需要解码post
    private HttpPostRequestDecoder decoder;

    static {
        //这是存储上传文件和属性的位置的配置，如果不存在该文件夹会报错。
        DiskFileUpload.deleteOnExitTemporaryFile = true; //当文件出现重名的时候是否删除
        DiskFileUpload.baseDirectory = "D:" + File.separatorChar + "aa"; // 系统存储文件的位置
        DiskAttribute.deleteOnExitTemporaryFile = true; //如果属性出现重复选择删掉
        DiskAttribute.baseDirectory = "D:" + File.separatorChar + "aa"; // 属性文件存储目录。
    }

    /**
     * 当通道断开的时候，触发此事件，置空decoder
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    /**
     * 当收到数据的时候，触发的方法，类似channelRead
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            URI uri = new URI(request.uri());
            //如果不是提交的内容，统一画出主界面，然后跳转。
            if (!uri.getPath().startsWith("/form")) {
                // Write Menu
                writeMenu(ctx);
                return;
            }
            //打印一些i请求的基本信息
            responseContent.setLength(0);
            responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            responseContent.append("===================================\r\n");

            responseContent.append("VERSION: " + request.protocolVersion().text() + "\r\n");

            responseContent.append("REQUEST_URI: " + request.uri() + "\r\n\r\n");
            responseContent.append("\r\n\r\n");

            // 遍历请求头的内容，进行输出到responseContent操作
            Iterator<Entry<CharSequence, CharSequence>> iterator = request.headers().iteratorCharSequence();
            while (iterator.hasNext()) {
                Entry<CharSequence, CharSequence> entry = iterator.next();
                responseContent.append("HEADER: " + entry.getKey() + '=' + entry.getValue() + "\r\n");
            }
            responseContent.append("\r\n\r\n");

            // 检查是否存在cookie，如果存在则取出，解码为cookie对象
            Set<Cookie> cookies;
            String value = request.headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            //将cookie内容设置到responseContent
            for (Cookie cookie : cookies) {
                responseContent.append("COOKIE: " + cookie + "\r\n");
            }
            responseContent.append("\r\n\r\n");
            //如果uri请求中带着查询参数，如果有就拿出来打印，针对get是有用的。
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                for (String attrVal: attr.getValue()) {
                    responseContent.append("URI: " + attr.getKey() + '=' + attrVal + "\r\n");
                }
            }
            responseContent.append("\r\n\r\n");


            //如果是get请求，不创建post请求的编码器
            if (request.method().equals(HttpMethod.GET)) {
                // get请求，不创建post请求的编码器，在此停止就好了
                responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
                // 不是现在发送响应: LastHttpContent 将会发送响应。
                //深刻理解HTTP请求分多次。
                return;
            }
            //初始化post编码器，传入http数据工厂和请求
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                e1.printStackTrace();
                responseContent.append(e1.getMessage());
                writeResponse(ctx.channel());
                ctx.channel().close();
                //System.out.println("ErrorDataDecoderException");
                return;
            }
            //判断request是否启用了分块传输
            //当返回的数据比较大时，如果等待生成完数据再传输，这样效率比较低下。相比而言，
            // 服务器更希望边生成数据边传输。可以在响应头加上以下字段标识分块传输：
//            readingChunks = HttpHeaderUtil.isTransferEncodingChunked(request);
            responseContent.append("Is Chunked: " + readingChunks + "\r\n");
            responseContent.append("IsMultipart: " + decoder.isMultipart() + "\r\n");
            if (readingChunks) {
                // Chunk version
                responseContent.append("Chunks: ");
                readingChunks = true;
            }
        }

        // 检查解码器是否创建了
        // 如果没创建则判定是处理的get请求
        if (decoder != null) {
            //如果是 msg 是httpcontent而不是   http请求头
            if (msg instanceof HttpContent) {
                // 新的分块被收到的时候触发此方法
                HttpContent chunk = (HttpContent) msg;
                try {
                    //进行解码，数据全部读入解码器
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    responseContent.append(e1.getMessage());
                    writeResponse(ctx.channel());
                    ctx.channel().close();
                    return;
                }
                //解码一次生成一个0打印,附件大的时候，小圈特别多。
                responseContent.append('o');
                // 一块一块读数据的一个例子
                readHttpDataChunkByChunk();
                System.out.println(" readHttpDataChunkByChunk();");
                // 如果是最后一块数据则发送response。
                if (chunk instanceof LastHttpContent) {
                    writeResponse(ctx.channel());
                    readingChunks = false;
                    reset();
                }
            }
        } else {
            writeResponse(ctx.channel());
        }
    }
    /**
     * 功能描述: 当是最后一次httpcontent的时候，重置请求对象，销毁编码器。编码器中读取了httpcontent的内容
     *
     */
    private void reset() {
        request = null;
        // destroy the decoder to 释放资源
        decoder.destroy();
        decoder = null;
    }

    /**
     *读取http数据ChunkByChunk,这个地方会出现数据积攒，等攒够一块完整的空间，才会执行读的逻辑
     */
    private void readHttpDataChunkByChunk() throws Exception {
        try {
            //decoder全读完了之后才能进while循环。
            System.out.println("decoder.hasNext()-before");
            while (decoder.hasNext()) {
                System.out.println("decoder.hasNext()");
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        System.out.println("writeHttpData(data);");
                        // 具体的执行写的逻辑
                        writeHttpData(data);

                    } finally {
                        data.release();
                    }
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
            responseContent.append("\r\n\r\nEND OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }
    /**
     * 功能描述: 具体往磁盘上写的逻辑
     */
    private void writeHttpData(InterfaceHttpData data) throws Exception {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                // Error while reading data from File, only print name and error
                e1.printStackTrace();

                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                    + attribute.getName() + " Error while reading value: " + e1.getMessage() + "\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                    + attribute.getName() + " data too long\r\n");
            } else {
                responseContent.append("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                    + attribute + "\r\n");
            }
        } else {
            //下面的代码只执行一次
            responseContent.append("\r\n -----------start-------------" + "\r\n");
            responseContent.append("\r\nBODY FileUpload: " + data.getHttpDataType().name() + ": " + data
                + "\r\n");
            responseContent.append("\r\n ------------end------------" + "\r\n");
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {

                    System.out.println("file name : " + fileUpload.getFilename());
                    System.out.println("file length: " + fileUpload.length());
                    System.out.println("file maxSize : " + fileUpload.getMaxSize());
                    System.out.println("file path :" + fileUpload.getFile().getPath());
                    System.out.println("file absolutepath :" + fileUpload.getFile().getAbsolutePath());
                    System.out.println("parent path :" + fileUpload.getFile().getParentFile());

                    if (fileUpload.length() < 1024*1024*10) {
                        responseContent.append("\tContent of file\r\n");
                        try {
                            //responseContent.append(fileUpload.getString(fileUpload.getCharset()));
                        } catch (Exception e1) {
                            // do nothing for the example
                            e1.printStackTrace();
                        }
                        responseContent.append("\r\n");
                    } else {
                        responseContent.append("\tFile too long to be printed out:" + fileUpload.length() + "\r\n");
                    }
                    // fileUpload.isInMemory();// tells if the file is in Memory
                    // or on File
                    fileUpload.renameTo(new File(fileUpload.getFile().getPath())); // enable to move into another
                    // File dest
                    //decoder.removeFileUploadFromClean(fileUpload); //remove
                    // the File of to delete file
                } else {
                    responseContent.append("\tFile to be continued but should not!\r\n");
                }
            }
        }
    }
    /**
     * 功能描述: 上传成功之后，把上传的过程信息，进行打印
     *
     */
    private void writeResponse(Channel channel) {
        // Convert the responseJson content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);

        // Decide whether to close the connection or not.
        boolean close = request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, true)
            || request.protocolVersion().equals(HttpVersion.HTTP_1_0)
            && !request.headers().contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE, true);

        // Build the responseJson object.
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last responseJson.
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        }

        Set<Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // Write the responseJson.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 功能描述: 当用户访问127.0.0.1：8080端口的时候画出界面给予返回
     * @param ctx
     */
    private void writeMenu(ChannelHandlerContext ctx) {
        // 打印出服务器端的html表单
        // 然后将表单字符串转化为bytebuf
        //拼接字符用stringbuilder
        responseContent.setLength(0);

        // create Pseudo Menu
        responseContent.append("<html>");
        responseContent.append("<head>");
        responseContent.append("<title>Netty Test Form</title>\r\n");
        responseContent.append("</head>\r\n");
        responseContent.append("<body bgcolor=white><style>td{font-size: 12pt;}</style>");

        responseContent.append("<table border=\"0\">");
        responseContent.append("<tr>");
        responseContent.append("<td>");
        responseContent.append("<h1>Netty Test Form</h1>");
        responseContent.append("Choose one FORM");
        responseContent.append("</td>");
        responseContent.append("</tr>");
        responseContent.append("</table>\r\n");

        // GET
        responseContent.append("<CENTER>GET FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
        responseContent.append("<FORM ACTION=\"/formget\" METHOD=\"GET\">");
        responseContent.append("<input type=hidden name=getform value=\"GET\">");
        responseContent.append("<table border=\"0\">");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
        responseContent
            .append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
        responseContent.append("</td></tr>");
        responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
        responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
        responseContent.append("</table></FORM>\r\n");
        responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

        // POST
        responseContent.append("<CENTER>POST FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
        responseContent.append("<FORM ACTION=\"/formpost\" METHOD=\"POST\">");
        responseContent.append("<input type=hidden name=getform value=\"POST\">");
        responseContent.append("<table border=\"0\">");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
        responseContent
            .append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
        responseContent.append("<tr><td>Fill with file (only file name will be transmitted): <br> "
            + "<input type=file name=\"myfile\">");
        responseContent.append("</td></tr>");
        responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
        responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
        responseContent.append("</table></FORM>\r\n");
        responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

        // POST with enctype="multipart/form-data"
        responseContent.append("<CENTER>POST MULTIPART FORM<HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");
        responseContent.append("<FORM ACTION=\"/formpostmultipart\" ENCTYPE=\"multipart/form-data\" METHOD=\"POST\">");
        responseContent.append("<input type=hidden name=getform value=\"POST\">");
        responseContent.append("<table border=\"0\">");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"info\" size=10></td></tr>");
        responseContent.append("<tr><td>Fill with value: <br> <input type=text name=\"secondinfo\" size=20>");
        responseContent
            .append("<tr><td>Fill with value: <br> <textarea name=\"thirdinfo\" cols=40 rows=10></textarea>");
        responseContent.append("<tr><td>Fill with file: <br> <input type=file name=\"myfile\">");
        responseContent.append("</td></tr>");
        responseContent.append("<tr><td><INPUT TYPE=\"submit\" NAME=\"Send\" VALUE=\"Send\"></INPUT></td>");
        responseContent.append("<td><INPUT TYPE=\"reset\" NAME=\"Clear\" VALUE=\"Clear\" ></INPUT></td></tr>");
        responseContent.append("</table></FORM>\r\n");
        responseContent.append("<CENTER><HR WIDTH=\"75%\" NOSHADE color=\"blue\"></CENTER>");

        responseContent.append("</body>");
        responseContent.append("</html>");
        //将string转化为Buffer
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        // 画完显示的内容之后，构建resp对象反馈给客户端.1.1版本的http，200的状态码，消息体
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        //设置响应头：编码，反馈的长度
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        // 返回响应.
        ctx.channel().writeAndFlush(response);
    }

    /**
     * 功能描述: 连接出现异常会调用此方法，一般就是记录日志，关闭连接。
     *
     * @auther: 李泽
     * @date: 2019/3/17 11:47
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info(responseContent.toString(), cause);
        ctx.channel().close();
    }
}
