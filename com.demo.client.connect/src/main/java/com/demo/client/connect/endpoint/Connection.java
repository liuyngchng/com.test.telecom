package com.demo.client.connect.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Connection information
 * @author Richard Liu
 * @since 2019.07.18
 */
public class Connection {

    public static final Map<String, ChannelHandlerContext> clients = new HashMap<String, ChannelHandlerContext>(8);

    public static final AttributeKey<String> uidKey = AttributeKey.newInstance("uid");
}
