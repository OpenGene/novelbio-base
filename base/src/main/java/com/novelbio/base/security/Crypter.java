package com.novelbio.base.security;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crypter {
	private static Logger logger = LoggerFactory.getLogger(Crypter.class);
	
	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/** RSA公钥 */
	private static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCR4nOCkGnBwUcQPzXCYnQwQ2+IXwkTkGDc3WyU\n"
									+ "8eAlV/jQTO2LGv0JECewGr2g+DetnwAfvHgv4HH3NCAgbK2yUnOpQPSaMZIUMNCWatoNTsZm0WOn\n"
									+ "3XQtreQdAWgskPflxU5oJs9IUErmHoXzs2aVxozizJPazLIKKttdz5PJlwIDAQAB";
	/** RSA私钥 */
	private static String privateKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJHic4KQacHBRxA/NcJidDBDb4hf\n"
									+ "CROQYNzdbJTx4CVX+NBM7Ysa/QkQJ7AavaD4N62fAB+8eC/gcfc0ICBsrbJSc6lA9JoxkhQw0JZq\n"
									+ "2g1OxmbRY6fddC2t5B0BaCyQ9+XFTmgmz0hQSuYehfOzZpXGjOLMk9rMsgoq213Pk8mXAgMBAAEC\n"
									+ "gYA9fi/khE0f8AGtdoeJpYiGY5aa7DxeM8iwsOE5M2+hLiDiZNrofPrWHCoukEcImDiYBeK+fepH\n"
									+ "fBtt8VntcQIR+Fg/4G7yM4T8cF6IoHN8ygjXtfUlen8U0tF5Zoje1/sCnkEtXFcnLT57RPqE+1/k\n"
									+ "YTb+JlxCRBab+tcuS8j5SQJBAOWdXh+WRHxa9Y0wN3l6KZtbH4ZXMXYwWkR89ezlwMX4epDaVHIg\n"
									+ "1SonW0x5qyfp1pBsmkO+Ktb9To1v9mJgOrsCQQCipfk0Onmaki/C3/q4guqXNF1jMMHW/dkVg/1L\n"
									+ "lv1/ttf1pzAYUKwsmeBvCql6dK17lB330kfS2qS2SmTQSATVAkANNWJGuuQxqyHY/18Rk9902mcT\n"
									+ "2Uw1Gk73BaE4AXd3a/XRA148OntIs37jBVS9NQxsvnKZVwUr3OJ57Gjl+9clAkAkb/5mPvtjL4DX\n"
									+ "rYnjsCCSAz8wq4mhenkZotoqBwd/hxzWgb/6kogf7dEjz0Wsk1sSJlqYXFmO2UCxPfVb2+aJAkB2\n"
									+ "5k23/SLS0RS0pVD9Kudl8miQSUXbBljoD5wru1rAUOiTySCUWP9dv5FyGobYyBCCJUluG4bC8IiH\n"
									+ "stDSUBh6";
	
	/** AES密钥 */
	private static final String AES_KEY = "anYX2bSM6l/z7OSSox6zKA==\n";
	
	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}
	
	/**
	 * 使用RSA算法的公钥加密明文字符串，经过Base64后输出
	 * @param plaintext 明文字符串
	 * @return String 密文
	 */
	public static String encryptByPublicKey(String plaintext) {
		try {
			checkNotNull(plaintext, "plaintext is null");
			byte[] encodedData = RSACoder.encryptByPublicKey(plaintext.getBytes(), publicKey);
			return Coder.encryptBASE64(encodedData);
		} catch (Exception e) {
			logger.error("encryptByPublicKey error.", e);
		}
		return null;
	}
	
	/**
	 * 使用RSA算法的私钥解密密文字符串
	 * @param ciphertext 经过Base64后的密文字符串
	 * @return String 明文
	 */
	public static String decryptByPrivateKey(String ciphertext) {
		try {
			checkNotNull(ciphertext, "ciphertext is null");
			byte[] output = Coder.decryptBASE64(ciphertext);
			byte[] decodedData = RSACoder.decryptByPrivateKey(output, privateKey);
			return new String(decodedData);
		} catch (Exception e) {
			logger.error("decryptByPrivateKey error.", e);
		}
		return null;
	}

	/**
	 * 使用MD5算法签名
	 * @param input 待字符串
	 * @return String 签名
	 */
	public static String signByMD5(String input) {
		try {
			checkNotNull(input, "input is null");
			return toHexString(Coder.encryptMD5(input.getBytes()));
		} catch (Exception e) {
			logger.error("signByMD5 error.", e);
		}
		return null;
	}
	
	/**
	 * 进行BASE64编码
	 * @param input 待编码字符串
	 * @return String
	 */
	public static String encryptBASE64(String input) {
		try {
			checkNotNull(input, "input is null");
			String output = Coder.encryptBASE64(input.getBytes());
			return output;
		} catch (Exception e) {
			logger.error("encryptBASE64 error.", e);
		}
		return null;
	}

	/**
	 * 进行BASE64解码
	 * @param input 待解码字符串
	 * @return String
	 */
	public static String decryptBASE64(String input) {
		try {
			checkNotNull(input, "input is null");
			byte[] output = Coder.decryptBASE64(input);
			return new String(output);
		} catch (Exception e) {
			logger.error("decryptBASE64 error.", e);
		}
		return null;
	}
	
	/**
	 * 进行AES算法加密
	 * @param input 待加密字符串
	 * @return String
	 */
	public static String encryptAES(String input) {
		try {
			checkNotNull(input, "input is null");
			String output = Coder.encryptBASE64(DESCoder.encryptAES(input.getBytes(), AES_KEY));
			return output;
		} catch (Exception e) {
			logger.error("encryptAES error.", e);
		}
		return null;
	}
	
	/**
	 * 进行AES算法解密
	 * @param input 待解密字符串
	 * @return String
	 */
	public static String decryptAES(String input) {
		try {
			checkNotNull(input, "input is null");
			byte[] output = DESCoder.decryptAES(Coder.decryptBASE64(input), AES_KEY);
			return new String(output);
		} catch (Exception e) {
			logger.error("decryptAES error.", e);
		}
		return null;
	}
}
