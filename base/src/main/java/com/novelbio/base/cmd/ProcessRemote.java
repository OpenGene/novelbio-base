package com.novelbio.base.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class ProcessRemote implements IntProcess {

	private Connection conn;
	Session session;
	boolean isConnected = false;
	/** 远程机器IP */
	private String ip;
	/** 用户名 */
	private String usr;
	/** 密码 */
	private String psword;
	
	private char[] key;
	
	/** 0表示没有过时时间，一直等下去 */
	private int timeOut = 0;
	
	public ProcessRemote(String ip, String usr, String pwd) {
		this.ip = ip;
		this.usr = usr;
		this.psword = pwd;
	}
	public ProcessRemote(String ip, String usr) {
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
	private boolean login() throws IOException {
		if (isConnected) {
			return true;
		}
		conn = new Connection(ip);
		conn.connect();
		if (key != null) {
			isConnected = conn.authenticateWithPublicKey(usr, key, psword);
		} else {
			isConnected = conn.authenticateWithPassword(usr, psword);
		}
		return isConnected;
	}
	
	/** 断开连接 */
	public void close() {
		if (session != null) {
			session.close();
		}
		if (conn != null) {
			conn.close();
		}
		isConnected = false;
	}

	@Override
	public void exec(String[] cmd) throws Exception {
		session = null;
		if (!login()) {
			throw new Exception("connection error: cannot login");
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : cmd) {
			//考虑是否合适
			if (string.startsWith("/") && string.contains(" ")) {
				string = CmdOperate.addQuot(string);
			}
			stringBuilder.append(string);
			stringBuilder.append(" ");
		}
		String cmdRun = stringBuilder.toString().trim();
		
		session = conn.openSession();
		session.execCommand(cmdRun);
	}

	@Override
	public int waitFor() throws InterruptedException {
		session.waitForCondition(ChannelCondition.EXIT_STATUS, timeOut);
		return session.getExitStatus();
	}

	@Override
	public InputStream getStdErr() {
		return session.getStderr();
	}

	@Override
	public InputStream getStdOut() {
		return session.getStdout();
	}

	@Override
	public OutputStream getStdIn() {
		return session.getStdin();
	}

	@Override
	public boolean isCmdStarted() {
		return session != null;
	}

	@Override
	public void stopProcess() throws Exception {
		session.close();
	}

	@Override
	public List<ProcessInfo> getLsProcInfo() throws Exception {
		throw new ExceptionCmd("cannot get processinfo while using remote cmd");
	}


}
