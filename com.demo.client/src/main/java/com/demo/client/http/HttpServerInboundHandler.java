package com.demo.client.http;

import com.demo.client.util.UserUtil;
import com.demo.common.enums.ErrorCode;
import com.demo.common.model.Result;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerInboundHandler.class);
	public static final String STATIC_LOGIN_HTML = "static/login.html";
	public static final String STATIC_INDEX_HTML = "static/index.html";

	private HttpRequest request;
	/**
	 * Request url path
	 */
	private String path;

	/**
	 * static content path
	 */
	private String staticPath;

	private String token;

	/**
	 * 需要替换静态内容的部分
	 */
	private Map<String, String> markerMap = new HashMap<>(8);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
			final String uri = request.uri();
			LOGGER.debug("uri is {}", uri);
			path = uri.split("\\?")[0].trim();
			if (uri.split("\\?").length > 1) {
				final String tokenStr = uri.split("\\?")[1].trim();
				if (tokenStr.contains("token=")) {
					token = tokenStr.split("=")[1].trim();
					LOGGER.debug("token is {}", token);
				}
			}
		}
		if (msg instanceof HttpContent) {
			final HttpContent content = (HttpContent) msg;
			final ByteBuf buf = content.content();
			final String body = buf.toString(CharsetUtil.UTF_8);
			buf.release();
			LOGGER.debug("content is {}", body);
			String response;
			if ("/login".equals(path) && HttpMethod.POST.equals(request.method())){
				LOGGER.info("start login.");
				final Gson gson = new Gson();
				final Result result = this.login(body);
				response = gson.toJson(result);
			} else {
				this.getStaticPath();
				final InputStreamReader isr =
					new InputStreamReader(
						this.getClass().getClassLoader().getResourceAsStream(
							staticPath
						)
					);
				final String staticContent = new BufferedReader(isr).lines().parallel().collect(Collectors.joining());
				response = this.replaceMarker(staticContent);
			}

			ctx.writeAndFlush(
				this.buildResponse(
					response
				)
			);
		}
	}


	private Result login(String text) {
		String username = "", password = "";
		if (!Strings.isNullOrEmpty(text)) {
			final String[] kvs = text.split("&");
			for (int i = 0; i < kvs.length; i++) {
				final String[]kv = kvs[i].split("=");
				if (kv[0].equals("username")) {
					username = kv[1].trim();
				} else if (kv[0].equals("password")) {
					password = kv[1].trim();
				}
			}
		}
		Result result = new Result();
		if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
			result.setCode(ErrorCode.USER_PS_NULL.getCode());
			result.setDesc(ErrorCode.USER_PS_NULL.getDesc());
		} else {
			result = UserUtil.login(username, password);
		}
		return result;
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("error", cause);
		ctx.close();
	}

	/**
	 * 获取静态资源.
	 * @return A String path
	 */
	private void getStaticPath() {
		switch (path) {
			case "/":
				final String token = request.headers().get("token");
				if (Strings.isNullOrEmpty(token)) {
					staticPath = STATIC_LOGIN_HTML;
				} else {
					staticPath = STATIC_INDEX_HTML;
				}
				break;
			default:
				staticPath = "static" + path;
				break;
		}
		LOGGER.debug("static path is {}", staticPath);
	}

	private FullHttpResponse buildResponse(String res) {
		FullHttpResponse response = new DefaultFullHttpResponse(
			HttpVersion.HTTP_1_1,
			HttpResponseStatus.OK,
			Unpooled.wrappedBuffer(res.getBytes(CharsetUtil.UTF_8))
		);
		response.headers().set(
			HttpHeaderNames.CONTENT_TYPE,
			this.getContentType()
		);
		response.headers().set(
			HttpHeaderNames.CONTENT_LENGTH,
			response.content().readableBytes()
		);
		if (HttpUtil.isKeepAlive(request)) {
			response.headers().set(
				HttpHeaderNames.CONNECTION,
				HttpHeaderValues.KEEP_ALIVE
			);
		}
		if (!Strings.isNullOrEmpty(token)){
			response.headers().set("token", token);
			LOGGER.info("token is {}", token);
		}
		return response;
	}

	private String replaceMarker(String html) {
		markerMap.forEach((k,v)-> {
			k.replaceAll("\\%","\\\\%");
			html.replaceFirst(k,v);
		});
		return html;
	}

	private String getContentType(){
		String contentType;
		if (Strings.isNullOrEmpty(staticPath)) {
			contentType = HttpHeaderValues.APPLICATION_JSON.toString();
		} else if (staticPath.endsWith("html")) {
			contentType = "text/html;charset=UTF-8";
		} else if(staticPath.endsWith("js")) {
			contentType = "application/javascript";
		} else {
			contentType = HttpHeaderValues.APPLICATION_JSON.toString();
		}
		return contentType;
	}
}
