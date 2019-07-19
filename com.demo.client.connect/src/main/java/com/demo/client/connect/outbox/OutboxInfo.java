package com.demo.client.connect.outbox;

import com.demo.client.connect.model.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Inbox info.
 * @author Richard Liu
 * @since 2019.07.18
 */
public class OutboxInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxInfo.class);

    public static final Map<String, Map<Long, Mail>> mail = new HashMap<>(8);
}
