package com.demo.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SQLiteUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteUtil.class);
	private static final String Drive = "org.sqlite.JDBC";
	private static final String connectionUrl = "jdbc:sqlite:db/demo.db";
	private static Connection connection;
	static {
		try {
			Class.forName(Drive);
			connection = DriverManager.getConnection(connectionUrl);
			connection.setAutoCommit(true);
			LOGGER.info("connection initialized.");
		} catch (Exception ex) {
			LOGGER.error("error", ex);
		}
	}

	public static ResultSet executeQuery(String sql) {
		ResultSet rs = null;
		try {
			final Statement statement = connection.createStatement();
			rs = statement.executeQuery(sql);
		} catch (Exception ex) {
			LOGGER.error("error", ex);
		}
		return rs;
	}

	private static void initUser() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS user_base");
			statement.executeUpdate(getCreateUserTableSql());
			statement.executeUpdate(
				String.format(
					"insert into user_base (id, user_name, password) values(%s,'%s','%s')",
					1,
					"abc",
					Md5Util.getVal("abc")
				)
			);
			statement.executeUpdate(
				String.format(
					"insert into user_base (id, user_name, password) values(%s,'%s','%s')",
					2,
					"def",
					Md5Util.getVal("def")
				)
			);
			final ResultSet rSet = statement.executeQuery("select * from user_base");//搜索数据库，将搜索的放入数据集ResultSet中
			while (rSet.next()) {            //遍历这个数据集
				LOGGER.info("{},{},{}" ,rSet.getInt(1), rSet.getString(2),rSet.getString(3));
			}
			rSet.close();//关闭数据集
			connection.close();//关闭数据库连接
		} catch (Exception e) {
			LOGGER.error("error", e);
		}
	}

	private static void initInbox() {
		try {
			final Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS mail_inbox");
			statement.executeUpdate(getCreateMailInboxTableSql());
			statement.executeUpdate(
				String.format(
					"insert into mail_inbox (id, uid, `from`, `to`, status, title, attachment, create_time,update_time) values(%s,%s,'%s','%s',%s,'%s','%s','%s','%s')",
					1,
					1,
					"abc",
					"abc",
						0,
					"this is a test from abc to abc",
					"http://www.file.com/abc.pdf",
					System.currentTimeMillis(),
					System.currentTimeMillis()
				)
			);
			statement.executeUpdate(
				String.format(
					"insert into mail_inbox (id, uid, `from`, `to`, status, title, attachment,create_time,update_time) values(%s,%s,'%s','%s',%s,'%s','%s','%s','%s')",
					2,
					2,
					"def",
					"abc",
					0,
					"this is a test from def to abc",
					"http://www.file.com/def.pdf",
					System.currentTimeMillis(),
					System.currentTimeMillis()
				)
			);
			final ResultSet rSet = statement.executeQuery("select * from mail_inbox");//搜索数据库，将搜索的放入数据集ResultSet中
			while (rSet.next()) {            //遍历这个数据集
				LOGGER.info(
					"{},{},{},{},{},{},{},{},{}" ,
					rSet.getInt(1),
					rSet.getString(2),
					rSet.getString(3),
					rSet.getString(4),
					rSet.getInt(5),
					rSet.getString(6),
					rSet.getString(7),
					rSet.getTimestamp(8),
					rSet.getTimestamp(9)
				);
			}
			rSet.close();//关闭数据集
			connection.close();//关闭数据库连接
		} catch (Exception e) {
			LOGGER.error("error", e);
		}
	}

	public static String getCreateUserTableSql() {
		String createTableSql = "CREATE TABLE `user_base` (" +
			"  `id` int(11) NOT NULL," +
			"  `user_name` varchar(64) NOT NULL," +
			"  `password` varchar(64) NOT NULL," +
			"  `note` varchar(45) DEFAULT NULL," +
			"  PRIMARY KEY (`id`)" +
			");";
//            String createIndexSql = "CREATE  UNIQUE INDEX `userId` ON usertoken (`userId`);";
		LOGGER.info("createTableSql is {}", createTableSql);
		return createTableSql;
	}

	public static String getCreateMailInboxTableSql() {
		String createTableSql = "CREATE TABLE `mail_inbox` (" +
			"  `id` int(11) NOT NULL," +
			"  `uid` int(11) NOT NULL," +
			"  `from` varchar(45) DEFAULT NULL," +
			"  `to` varchar(45) DEFAULT NULL," +
			"  `status` int(11) DEFAULT NULL," +
			"  `title` varchar(45) DEFAULT NULL," +
			"  `attachment` varchar(45) DEFAULT NULL," +
			"  `create_time` timestamp NULL DEFAULT NULL," +
			"  `update_time` timestamp NULL DEFAULT NULL," +
			"  PRIMARY KEY (`uid`)" +
			");";

		LOGGER.info("createTableSql is {}", createTableSql);
		return createTableSql;
	}

	public static String getCreateMailOutboxTableSql() {
		String createTableSql = "CREATE TABLE `mail_outbox` (" +
				"  `id` int(11) NOT NULL," +
				"  `uid` int(11) NOT NULL," +
				"  `to` varchar(45) DEFAULT NULL," +
				"  `title` varchar(45) DEFAULT NULL," +
				"  `attachment` varchar(45) DEFAULT NULL," +
				"  PRIMARY KEY (`uid`)" +
				");";
		LOGGER.info("createTableSql is {}", createTableSql);
		return createTableSql;
	}
	public static void main(String[] args) {
//		initUser();
		initInbox();
	}
}
