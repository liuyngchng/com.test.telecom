package com.demo.client.connect.protobuf;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by richard on 19/07/2019.
 */
public class MessageTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageTest.class);

    @Test
    public void testSerialize() throws Exception {
        Message.data.Builder builder = Message.data.newBuilder();
        builder.setAttachment("a");
        builder.setFrom("from");
        builder.setTo("to");
        builder.setSignal("send_msg");
        Message.data data = builder.build();
        LOGGER.info("data size is {}", data.getSerializedSize());
        LOGGER.info("data is {}", data.toString());
        byte[] pbArray = data.toByteArray();
        LOGGER.info("pbArray length is {}", pbArray.length);

    }
}