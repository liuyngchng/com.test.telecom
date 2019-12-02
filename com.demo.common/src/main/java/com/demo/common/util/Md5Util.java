package com.demo.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

	private static final Logger LOGGER = LoggerFactory.getLogger(Md5Util.class);

	private static final char[] md5Char =
		{
			'0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F'
		};

	public final static String getVal(final String pwd) {
		final byte[] btInput = pwd.getBytes();
		MessageDigest mdInst;
		try {
			mdInst = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			LOGGER.error("error", ex);
			return "";
		}
		mdInst.update(btInput);
		final byte[] md = mdInst.digest();
		final int j = md.length;
		final char str[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			final byte bi = md[i];
			str[k++] = md5Char[bi >>> 4 & 0xf];
			str[k++] = md5Char[bi & 0xf];
		}
		return new String(str);
	}
	public static void main(String[] args) {
		String before="192.168.1.1";
		String result = Md5Util.getVal(before);
		LOGGER.info("{},length {}", result, result.length());
//        int key = (((((((27* (byte)'h'+27)* (byte)'e') + 27) * (byte)'l') + 27) * (byte)'l' +27) * 27 ) + (byte)'o' ;
//        System.out.println(key);
		LOGGER.info("{}", before.hashCode());
	}
}
