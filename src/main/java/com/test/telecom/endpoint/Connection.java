package com.test.telecom.endpoint;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 18/07/2019.
 */
public class Connection {

    public static final Map<String, ChannelHandlerContext> clients = new HashMap<String, ChannelHandlerContext>(8);
}
