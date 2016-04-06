package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SCPInputStream;
import ch.ethz.ssh2.SCPOutputStream;
import ch.ethz.ssh2.Session;

import com.novelbio.base.fileOperate.FileOperate;

/** 基于ssh的scp，文件复制 */
public class SshScp {
	
	private Connection conn;
	Session session;
	/** 远程机器IP */
	private String ip;
	/** 用户名 */
	private String usr;
	/** 密码 */
	private String psword;
	
	private char[] key;
	
	public SshScp(String ip, String usr, String pwd) {
		this.ip = ip;
		this.usr = usr;
		this.psword = pwd;
	}
	public SshScp(String ip, String usr) {
		this.ip = ip;
		this.usr = usr;
	}

	public void setKey(char[] key) {
		this.key = key;
	}
	public void setKey(String keyInfo) {
		this.key = keyInfo.toCharArray();
	}
	public void setKeyFile(String keyFile) {
		StringBuilder stringBuilder = new StringBuilder();
		TxtReadandWrite txtRead = new TxtReadandWrite(keyFile);
		for (String content : txtRead.readlines()) {
			stringBuilder.append(content);
			stringBuilder.append(TxtReadandWrite.ENTER_LINUX);
		}
		txtRead.close();
		key = stringBuilder.toString().toCharArray();
	}
	
	/**
	 * 登录
	 * 
	 * @param keyFile 公钥文件
	 * @return
	 * @throws IOException
	 */
	public void login() throws IOException {
		conn = new Connection(ip);
		conn.connect();
		boolean isConnected = false;
		if (key != null) {
			isConnected = conn.authenticateWithPublicKey(usr, key, psword);
		} else {
			isConnected = conn.authenticateWithPassword(usr, psword);
		}
		if (!isConnected) {
			throw new RuntimeException("cannot connect to " + ip + " with name " + usr);
		}
	}
	
	/**
	 * 上传文件
	 * @param file
	 * @param remoteDir 远程文件夹，不包含文件名
	 * @throws IOException
	 */
	public void uploadFile(String file, String remoteDir) throws IOException {
		SCPClient scpClient = conn.createSCPClient();
		Path path = FileOperate.getPath(file);
		long length = FileOperate.getFileSizeLong(path);
		String fileName = FileOperate.getFileName(file);
		SCPOutputStream os = scpClient.put(fileName, length, remoteDir, null);
		InputStream is = FileOperate.getInputStream(path);
		IOUtils.copy(is, os);
		os.close();
		is.close();
	}

	/**
	 * 把远程文件下载到本地文件夹
	 * @param remoteFile
	 * @param localPath 本地文件夹名，注意不是文件名
	 * @param isCover
	 * @throws IOException
	 */
	public void downloadFileToPath(String remoteFile, String localPath, boolean isCover) throws IOException {
		SCPClient scpClient = conn.createSCPClient();
		String fileName = FileOperate.getFileName(remoteFile);
		String outFile = FileOperate.addSep(localPath) + fileName;
		if (!isCover && FileOperate.isFileFolderExist(outFile)) {
			return;
		}
		FileOperate.DeleteFileFolder(outFile);
		SCPInputStream is = scpClient.get(remoteFile);
		OutputStream os = FileOperate.getOutputStream(outFile);
		IOUtils.copy(is, os);
		is.close();
		os.close();
	}
	
	/**
	 * 把远程文件下载到本地
	 * @param remoteFile
	 * @param localFile 本地文件名
	 * @param isCover
	 * @throws IOException
	 */
	public void downloadFile(String remoteFile, String localFile, boolean isCover) throws IOException {
		SCPClient scpClient = conn.createSCPClient();
		if (!isCover && FileOperate.isFileFolderExist(localFile)) {
			return;
		}
		FileOperate.DeleteFileFolder(localFile);
		SCPInputStream is = scpClient.get(remoteFile);
		OutputStream os = FileOperate.getOutputStream(localFile);
		IOUtils.copy(is, os);
		is.close();
		os.close();
	}
	
	/** 断开连接 */
	public void close() {
		if (conn != null) {
			conn.close();
		}
	}
	
}
