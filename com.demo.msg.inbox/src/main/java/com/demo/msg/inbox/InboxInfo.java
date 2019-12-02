package com.demo.msg.inbox;
import com.demo.common.model.Mail;
import com.demo.common.orm.InboxDb;
import com.demo.common.util.SQLiteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Inbox info.
 * @author Richard Liu
 * @since 2019.07.18
 */
public class InboxInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboxInfo.class);

    public static final Map<String, Map<Long, Mail>> mailIndex = new HashMap<>(8);

    public static final PriorityBlockingQueue<Mail> queue = new PriorityBlockingQueue(128);

    public static List<Mail> getMailByUid(int uid, int pageSize, int page) {

        return InboxDb.getMailByUid(uid,pageSize,page);
    }


    public static void main(String[] args) {
        queue.contains(null);

    }
}
