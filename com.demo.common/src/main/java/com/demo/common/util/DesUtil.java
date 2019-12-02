package com.demo.common.util;

import io.netty.handler.codec.base64.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DesUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(DesUtil.class);
	private static SecureRandom sr = new SecureRandom();
	private static SecretKeyFactory keyFactory;
	private static KeyGenerator kg;

	private static byte[] defaultKey;

	static {
		try {
			keyFactory = SecretKeyFactory.getInstance("DES");
			kg = KeyGenerator.getInstance("DES");
			kg.init(sr);
			defaultKey = generateKey();
		} catch (NoSuchAlgorithmException neverHappens) {
			neverHappens.printStackTrace();
			LOGGER.error("error", neverHappens);
		}
	}

	public static byte[] generateKey() {
		return kg.generateKey().getEncoded();
	}

	/**
	 * encrypt data with default key.
	 * @param text plain text.
	 * @return A base64 encoding encrypt data byte[].
	 */
	public static String encrypt(final String text) {
		return java.util.Base64.getEncoder().encodeToString(
			encrypt(
				defaultKey,
				text
			)
		);
	}

	/**
	 * Decrypt a base64 string of a encrypt data byte[] with default key.
	 * @param base64Str A base64 string of a encrypt data byte[]
	 * @return decrypted data string.
	 */
	public static String decrypt(String base64Str) {
		return decrypt(
			defaultKey,
			java.util.Base64.getDecoder().decode(
				base64Str
			)
		);
	}

	/**
	 * encrypt
	 * @param rawKeyData key
	 * @param str data
	 * @return A encrypted data byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte rawKeyData[], String str) {

		try {
			final DESKeySpec dks = new DESKeySpec(rawKeyData);
			final SecretKey key = keyFactory.generateSecret(dks);
			final Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key, sr);
			return cipher.doFinal(str.getBytes());
		} catch (Exception e) {
			LOGGER.error("error, e");
			return new byte[1];
		}
	}

	/**
	 * decrypt
	 * @param rawKeyData key
	 * @param encryptedData data
	 * @return A decrypted data byte[]
	 * @throws Exception
	 */
	public static String decrypt(byte rawKeyData[], byte[] encryptedData) {
		try {
			final DESKeySpec dks = new DESKeySpec(rawKeyData);
			final SecretKey key = keyFactory.generateSecret(dks);
			final Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key, sr);
			byte decryptedData[] = cipher.doFinal(encryptedData);
			return new String(decryptedData);
		} catch (Exception ex) {
			LOGGER.error("error", ex);
			return "";
		}
	}

	public static void main(String[] args) throws Exception {
		String text = "this is me";
		String encrypted = encrypt(text);
		LOGGER.info("encrypted data is {}, hashcode is {}", encrypted, encrypted.hashCode());
		LOGGER.info("decrypted data is {}", decrypt(encrypted));
	}
}
