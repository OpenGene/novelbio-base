package com.novelbio.base.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hash工具类
 */
public class HashUtil {
	private static Logger logger = LoggerFactory.getLogger(HashUtil.class);

	private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private MessageDigest md5 = null;
	
	/** MD5计算耗时（毫秒） */
	private long TimeConsuming = 0;

	public HashUtil() {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    }
	
	public String getMD5(ByteBuffer buf) {
		long a = System.currentTimeMillis();
		md5.reset();
		md5.update(buf);
		String md5Str = toHexString(md5.digest());
		long b = System.currentTimeMillis();
		TimeConsuming = TimeConsuming + (b - a);
		return md5Str;
	}
	
	public long getTimeConsuming() {
		return TimeConsuming;
	}

	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}
	
	public static String getHashOfMD5(ByteBuffer buf) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(buf);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toHexString(md5.digest());
	}
	
	public static String getHashOfMD5(byte[] input) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			md5.update(input);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toHexString(md5.digest());
	}

	public static String getHashOfMD5(String fileName) throws Exception {
		FileInputStream fStream = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			fStream = new FileInputStream(fileName);
			FileChannel fChannel = fStream.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
			long s = System.currentTimeMillis();
			for (int count = fChannel.read(buffer); count != -1; count = fChannel.read(buffer)) {
				buffer.flip();
				md5.update(buffer);
				if (!buffer.hasRemaining()) {
					//System.out.println("count:"+count);
					buffer.clear();
				}
			}
			String md5Str = toHexString(md5.digest());
			s = System.currentTimeMillis() - s;
			
			return md5Str;

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fStream != null)
					fStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			String o = "asdfasdfasdf1234!@##$%^78中文测试内容   *9)||/?,/.<>";
			System.out.println(getHashOfMD5(o.getBytes()));
//			getHashOfMD5("/home/novelbio/backup/source/Fedora-Live-MATE_Compiz-x86_64-23-10.iso");
			//getHashOfMD5("/home/novelbio/文档/归档/每周分享讨论整理_down01.ppt");
			//System.err.println("werwer");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}