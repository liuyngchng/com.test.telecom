package com.demo.common.orm;

import com.demo.common.model.Mail;
import com.demo.common.util.SQLiteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InboxDb {

	private static final Logger LOGGER = LoggerFactory.getLogger(InboxDb.class);

	/**
	 * Get user mail in inbox
	 * @param uid UserId
	 * @param pageSize
	 * @param page index from 1 to n.
	 * @return A {@link Mail} list.
	 */
	public static List<Mail> getMailByUid(int uid, int pageSize, int page) {
		final List<Mail> mailList = new ArrayList<>(4);
		final ResultSet rs = SQLiteUtil.executeQuery(
			String.format(
				"select id, from, to, status, title, attachment,create_time,update_time " +
					"from mail_inbox where uid = %s limit %s offset %s order by id desc",
				uid,
				pageSize,
				(page -1) * pageSize
			)
		);
		if (null == rs) {
			return mailList;
		}
		try {
			while (rs.next()) {
				mailList.add(buildMail(rs));
			}
		} catch (Exception ex) {
			LOGGER.error("error", ex);
		}
		return mailList;
	}

	private static Mail buildMail(ResultSet rs) throws SQLException {
		final Mail mail = new Mail();
		mail.setId(rs.getInt(1));
		mail.setFrom(rs.getString(2));
		mail.setTo(rs.getString(3));
		mail.setStatus(rs.getInt(4));
		mail.setTitle(rs.getString(5));
		mail.setAttachment(rs.getString(6));
		mail.setCreateTime(rs.getDate(7));
		mail.setUpdateTime(rs.getDate(8));
		return mail;
	}
}
