package com.novelbio.base.dataOperate;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.thoughtworks.xstream.core.util.Base64Encoder;

public class SecurityCipher {
	private static final String IV = "0102030405060708";
	private static final String algo = "AES/CBC/PKCS5Padding";
	SecretKeySpec skeySpec;
	Cipher cipherEncode;
	Cipher cipherDecode;
	
	public void setKey(String key) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
		skeySpec = getKey(key);
		IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
		cipherEncode = Cipher.getInstance(algo);
		cipherEncode.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		
		cipherDecode = Cipher.getInstance(algo);
		cipherDecode.init(Cipher.DECRYPT_MODE, skeySpec, iv);
	}

	private static SecretKeySpec getKey(String strKey) throws NoSuchAlgorithmException {
		byte[] arrBTmp = strKey.getBytes();
//		byte[] arrB = new byte[32]; // 创建一个空的16位字节数组（默认值为0）
//
//		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
//			arrB[i] = arrBTmp[i];
//		}
		int maxLen = Cipher.getMaxAllowedKeyLength("AES");
		int keyLength = maxLen < 256 ? 128 : 256;
		keyLength = Math.min(arrBTmp.length, keyLength/8);
		
		SecretKeySpec skeySpec = new SecretKeySpec(arrBTmp, 0, 16, "AES");

		return skeySpec;
	}

	public String encrypt(String strIn) throws IllegalBlockSizeException, BadPaddingException {
		byte[] encrypted = cipherEncode.doFinal(strIn.getBytes());
		Base64Encoder base64Encoder = new Base64Encoder();
		return base64Encoder.encode(encrypted);
	}

	public String decrypt(String strIn) throws Exception {
		Base64Encoder base64Encoder = new Base64Encoder();
		byte[] encrypted1 = base64Encoder.decode(strIn);

		byte[] original = cipherDecode.doFinal(encrypted1);
		String originalString = new String(original);
		return originalString;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Cipher.getMaxAllowedKeyLength("AES"));
		String txt = "中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3中文ABc12aswrgasdfdvgfeds3";
		
		Random random = new Random(System.currentTimeMillis());
	     
		String codE = null;
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		SecurityCipher securityUtil = new SecurityCipher();
		String key =  random.nextInt() + dateUtil.getDateAndRandom() + dateUtil.getDateAndRandom()+ dateUtil.getDateAndRandom()+ dateUtil.getDateAndRandom();
		securityUtil.setKey(key);
		codE = securityUtil.encrypt(txt + dateUtil.getDateAndRandom());
		System.out.println(dateUtil.getElapseTime());
		System.out.println("原文：" + txt);
		System.out.println("密钥：" + codE);

	}
}