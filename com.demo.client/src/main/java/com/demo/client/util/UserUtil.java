package com.demo.client.util;

import com.demo.common.enums.ErrorCode;
import com.demo.common.model.Result;
import com.demo.common.orm.UserDb;
import com.demo.common.util.DesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserUtil.class);

	/**
	 * User login.
	 * @param username userName
	 * @param password password
	 * @returken string
	 */
	public static Result login(String username, String password) {
		final Result result = new Result();
		int userId = UserDb.getUid(username, password);
		if (userId == -1) {
			int userIdByName = UserDb.getUidByUserName(username);
			if (userIdByName == -1) {
				LOGGER.info("{} login failed, {}", username, result);
				result.setCode(ErrorCode.USER_NOT_EXIST.getCode());
				result.setDesc(ErrorCode.USER_NOT_EXIST.getDesc());
			} else {
				result.setCode(ErrorCode.PS_ERROR.getCode());
				result.setDesc(ErrorCode.PS_ERROR.getDesc());
			}
		} else {
			LOGGER.info("{} login success", username);
			result.setCode(ErrorCode.OK.getCode());
			result.setDesc(DesUtil.encrypt(String.format("%s#%s", userId, username)));
		}
		return result;
	}
}
