package com.demo.common.orm;

import com.demo.common.util.Md5Util;
import com.demo.common.util.SQLiteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class UserDb {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDb.class);

	public static int getUid(String userName, String password) {
		final ResultSet rs = SQLiteUtil.executeQuery(
			String.format(
				"select id from user_base where user_name ='%s' and password = '%s'",
				userName,
				Md5Util.getVal(password)
			)
		);
		int uid = -1;
		try {
			if (null !=rs && rs.next()) {
				uid = rs.getInt(1);
			}
		} catch (Exception ex) {
			LOGGER.error("error", ex);
		}
		return uid;
	}

	public static int getUidByUserName(String userName) {
		ResultSet rs = SQLiteUtil.executeQuery(
			String.format(
				"select id from user_base where user_name ='%s'",
				userName
			)
		);
		int uid = -1;
		try {
			if (null !=rs && rs.next()) {
				uid = rs.getInt(1);
			}
		} catch (Exception ex) {
			LOGGER.error("error", ex);
		}
		return uid;
	}
}
