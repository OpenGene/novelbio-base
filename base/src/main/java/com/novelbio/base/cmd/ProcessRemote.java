package com.novelbio.base.cmd;

import java.nio.charset.Charset;

public class ProcessRemote {

	private Connection conn;
	/** 远程机器IP */
	private String ip;
	/** */
	/** 用户名 */
	private String usr;
	/** */
	/** 密码 */
	private String psword;
	private String charset = Charset.defaultCharset().toString();

	private static final int TIME_OUT = 1000 * 5 * 60;
	
	public CmdRemoteOperate(String ip, String usr, String pwd) {
		this.ip = ip;
		this.usr = usr;
		this.psword = pwd;
	}

	/**
	 * 登录
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean login() throws IOException {
		conn = new Connection(ip);
		conn.connect();
		return conn.authenticateWithPassword(usr, psword);
	}

	/**
	 * 执行脚本
	 * 
	 * @param cmds
	 * @return
	 * @throws Exception
	 */
	public int exec(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;
		try {
			if (login()) {
				// Open a new {@link Session} on this connection
				Session session = conn.openSession();
				// Execute a command on the remote machine.
				session.execCommand(cmds);

				stdOut = new StreamGobbler(session.getStdout());
				outStr = processStream(stdOut, charset);

				stdErr = new StreamGobbler(session.getStderr());
				outErr = processStream(stdErr, charset);

				session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);

				System.out.println("outStr=" + outStr);
				System.out.println("outErr=" + outErr);

				ret = session.getExitStatus();
			} else {
				throw new AppException("登录远程机器失败" + ip); // 自定义异常类 实现略
			}
		} finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	/** */
	/**
	 * @param in
	 * @param charset
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private String processStream(InputStream in, String charset) throws Exception {
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		while (in.read(buf) != -1) {
			sb.append(new String(buf, charset));
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		RmtShellExecutor exe = new RmtShellExecutor("***.**.**.***", "sshapp", "sshapp");
		// 执行myTest.sh 参数为java Know dummy
		System.out.println(exe.exec("sh /webapp/myshell/myTest.sh java Know dummy"));
		// exe.exec("uname -a && date && uptime && who");
	}


}
