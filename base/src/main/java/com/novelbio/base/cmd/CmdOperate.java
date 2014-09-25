package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate.FinishFlag;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好<p>
 * <b>管道只支持最后的一个 > </b>
 * 例如 "bwa aaa bbb > ccc"，此时会根据ccc的后缀，gz还是bz2，自动选择相应的压缩流<br>
 * <b>不支持这种</b> "bwa aaa bbb | grep sd > ccc"
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String> {
	private static final Logger logger = Logger.getLogger(CmdOperate.class);

	/** 进程 */
	IntProcess process;
	
	/** 临时文件在文件夹 */
	String scriptFold = "";
	
	/** 标准输出流保存的路径 */
	String saveFilePath;
	/** 标准错误流保存的路径 */
	String saveErrPath;
	
	/**输出文件的名字 */
	Set<String> setOutfile = new HashSet<>();
	
	/** 结束标志，0表示正常退出 */
	FinishFlag finishFlag;
	long runTime = 0;
	/** 标准输出的信息 */
	LinkedList<String> lsOutInfo;
	/** 出错输出的信息 */
	LinkedList<String> lsErrorInfo = new LinkedList<>();
	StreamGobbler errorGobbler;
	StreamGobbler outputGobbler;
	
	/** 是否需要获取cmd的标准输出流 */
	boolean getCmdInStdStream = false;
	/** 是否需要获取cmd的标准错误流 */
	boolean getCmdInErrStream = false;
	
	/** 用来传递参数，拷贝输入输出文件夹的类 */
	CmdPath cmdPath = new CmdPath();
	
	/** 如果选择用list来保存结果输出，最多保存500行的输出信息 */
	int lineNumStd = 1000;
	/** 如果选择用list来保存错误输出，最多保存500行的输出信息 */
	int lineNumErr = 1000;//最多保存500行的输出信息
	
	
	public CmdOperate()  {}
	/**
	 * 直接运行，不写入文本
	 * @param cmd
	 */
	public CmdOperate(String cmd) {
		process = new ProcessCmd();
		String[] cmds = cmd.trim().split(" ");
		for (String string : cmds) {
			if (string.trim().equals("")) {
				continue;
			}
			cmdPath.addCmdParam(string);
		}
	}

	/**
	 * 初始化后直接开新线程即可 先写入Shell脚本，再运行
	 * 
	 * @param cmd
	 *            输入命令
	 * @param cmdWriteInFileName
	 *            将命令写入的文本
	 */
	@Deprecated
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		process = new ProcessCmd();
		setCmdFile(cmd, cmdWriteInFileName);
	}
	/**
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * 
	 * @param cmd
	 */
	private void setCmdFile(String cmd, String cmdWriteInFileName) {
		String newCmd = null;
		for(String text : cmd.trim().split(" ")){
			if (newCmd == null) {
				newCmd = FileHadoop.convertToLocalPath(text);
			}else {
				newCmd += " " + FileHadoop.convertToLocalPath(text);
			}
		}
		logger.info(newCmd);
		String cmd1SH = PathDetail.getTmpConfFold() + cmdWriteInFileName.replace("\\", "/") + DateUtil.getDateAndRandom() + ".sh";
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(newCmd);
		txtCmd1.close();
		cmdPath.clearLsCmd();
		cmdPath.addCmdParam("sh");
		cmdPath.addCmdParam(cmd1SH);
	}
	public CmdOperate(List<String> lsCmd) {
		process = new ProcessCmd();
		cmdPath.setLsCmd(lsCmd);
	}

	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param lsCmd
	 */
	public CmdOperate(String ip, String usr, String pwd, List<String> lsCmd) {
		process = new ProcessRemote(ip, usr, pwd);
		cmdPath.setLsCmd(lsCmd);
	}
	
	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param keyInfo 私钥内容
	 * @param lsCmd
	 */
	public CmdOperate(String ip, String usr, String pwd, String keyInfo, List<String> lsCmd) {
		process = new ProcessRemote(ip, usr, pwd);
		((ProcessRemote)process).setKey(keyInfo);
		cmdPath.setLsCmd(lsCmd);
	}
	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param keyInfo 私钥内容
	 * @param lsCmd
	 */
	public CmdOperate(String ip, String usr, String pwd, String keyInfo) {
		process = new ProcessRemote(ip, usr, pwd);
		((ProcessRemote)process).setKey(keyInfo);
	}

	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param keyInfo 私钥内容
	 * @param lsCmd
	 */
	public CmdOperate(String ip, String usr, String pwd, char[] keyInfo, List<String> lsCmd) {
		process = new ProcessRemote(ip, usr, pwd);
		((ProcessRemote)process).setKey(keyInfo);
		cmdPath.setLsCmd(lsCmd);
	}
	
	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param lsCmd
	 * @param keyFile 私钥文件
	 */
	public CmdOperate(String ip, String usr, String pwd, List<String> lsCmd, String keyFile) {
		process = new ProcessRemote(ip, usr, pwd);
		((ProcessRemote)process).setKeyFile(keyFile);
		cmdPath.setLsCmd(lsCmd);
	}
	
	/** 如果为null就不加入 */
	public void addCmdParam(String param) {
		if (!StringOperate.isRealNull(param)) {
			cmdPath.addCmdParam(param);
		}
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	public void addCmdParamInput(String input, boolean isAddToLsCmd) {
		cmdPath.addCmdParamInput(input, isAddToLsCmd);
	}
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	public void addCmdParamOutput(String output, boolean isAddToLsCmd) {
		if (StringOperate.isRealNull(saveErrPath)) {
			saveErrPath = output + "errorInfo.txt";
		}
		if (StringOperate.isRealNull(saveFilePath)) {
			saveFilePath = output + "stdInfo.txt";
		}
		cmdPath.addCmdParamOutput(output, isAddToLsCmd);
	}
	
	/** 是否将输入文件拷贝到临时文件夹，默认为false */
	public void setRedirectInToTmp(boolean isRedirectInToTmp) {
		//TODO
		cmdPath.setRedirectInToTmp(isRedirectInToTmp);
	}
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹，默认为false */
	public void setRedirectOutToTmp(boolean isRedirectOutToTmp) {
		//TODO
		cmdPath.setRedirectOutToTmp(isRedirectOutToTmp);
	}
	
	/** 如果param为null则返回 */
	public void addCmdParam(List<String> lsCmd) {
		cmdPath.addCmdParam(lsCmd);
	}
	/** 如果param为null则返回 */
	public void addCmdParam(String[] param) {
		cmdPath.addCmdParam(param);
	}

	
	/** 返回执行的具体cmd命令，不会将文件路径删除，仅给相对路径 */
	public String getCmdExeStr() {
		return getCmdExeStrReal();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String getCmdExeStrModify() {
		return cmdPath.getCmdExeStrModify();
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String getCmdExeStrReal() {
		return cmdPath.getCmdExeStrReal();
	}
	
	/** 是否获得cmd的标准输出流
	 * 默认false<br>
	 * 设定为true就可以通过{@link #getStreamStd()}来获得输出流<br>
	 * <b>优先级高于cmd命令中的重定向</b>
	 * @param getCmdInStdStream
	 */
	public void setGetCmdInStdStream(boolean getCmdInStdStream) {
		this.getCmdInStdStream = getCmdInStdStream;
	}
	/** 是否获得cmd的错误输出流
	 * 默认false<br>
	 * 设定为true就可以通过{@link #getStreamErr()}来获得错误流<br>
	 * <b>优先级高于cmd命令中的重定向</b>
	 * @param getCmdInStdStream
	 */
	public void setGetCmdInErrStream(boolean getCmdInErrStream) {
		this.getCmdInErrStream = getCmdInErrStream;
	}


	/** 只有当{@link #setGetCmdInErrStream(boolean)} 为false时才有用 <br>
	 * 用getStdOut获得标准输出的结果， */
	public void setGetLsStdOut() {
		lsOutInfo = new LinkedList<>();
	}

	/** 需要获得多少行错误输出流 */
	public void setGetLsErrOut(int lineNum) {
		this.lineNumErr = lineNum;
	}
	
	/** 程序执行完后可以看错误输出<br>
	 * 仅返回最多{@link #lineNum}行的信息
	 * 内部实现为linkedlist
	 */
	public List<String> getLsErrOut() {
		int i = 0;
		while (errorGobbler == null) {
			if (i++ > 10) break;
			try { Thread.sleep(100); } catch (InterruptedException e) { 	e.printStackTrace(); 	}
		}
		
		if (errorGobbler == null) {
			return new ArrayList<>();
		}
		
		while (true) {
			if (errorGobbler.isFinished()) {
				break;
			}
			
			try { Thread.sleep(100); } catch (Exception e) { }
		}
		return lsErrorInfo;
	}
	/** 和{@link #getLsErrOut()}一样，只不过返回用\n分割的错误流信息
	 * 没有则返回"";
	 */
	public String getErrOut() {
		StringBuilder errInfo = new StringBuilder();
		List<String> lsErr = getLsErrOut();
		if (lsErr == null) {
			return "";
		}
		for (String string : lsErr) {
			errInfo.append(string);
			errInfo.append('\n');
		}
		return errInfo.toString();
	}
	/** 程序执行完后可以看标准输出<br>
	 * 仅返回最多{@link #lineNum}行的信息
	 * 内部实现为linkedlist
	 */
	public List<String> getLsStdOut() {
		int i  = 0;
		while (outputGobbler == null) {
			if (i++ > 10) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (outputGobbler == null) {
			return new ArrayList<>();
		}
		while (true) {
			if (outputGobbler.isFinished()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return lsOutInfo;
	}
	
	/** 获得命令行的标准输出流， <br>
	 * 设定了{@link #setGetCmdInStdStream(boolean)} 为true才有用 */
	public InputStream getStreamStd() {
		while (outputGobbler == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return outputGobbler.getCmdOutStream();
	}
	/** 获得命令行的错误输出流， <br>
	 * 设定了{@link #setGetCmdInErrStream(boolean)} 为true才有用 */
	public InputStream getStreamErr() {
		while (errorGobbler == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return errorGobbler.getCmdOutStream();
	}
	/**
	 * 直接运行cmd，可能会出错 返回两个arraylist-string 第一个是Info 第二个是error
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private void doInBackgroundB() throws Exception {
		finishFlag = new FinishFlag();
		cmdPath.copyFileIn();
		String[] cmdRun = cmdPath.getRunCmd();
		boolean writeStdTxt = true, writeErrTxt = true;
		if (!StringOperate.isRealNull(cmdPath.getSaveFilePath())) {
			this.saveFilePath = cmdPath.getSaveFilePath();
			writeStdTxt = false;
		}
		if (!StringOperate.isRealNull(cmdPath.getSaveErrPath())) {
			this.saveErrPath = cmdPath.getSaveErrPath();
			writeErrTxt = false;
		}
		
		process.exec(cmdRun);

		setErrorStream(writeStdTxt);
		setStdStream(writeErrTxt);

		errorGobbler.start();
		outputGobbler.start();
		
		finishFlag.flag = process.waitFor();
		outputGobbler.join();
		errorGobbler.join();
		if (!getCmdInStdStream && saveFilePath != null) {
			outputGobbler.close();
		}
		if (!getCmdInErrStream && saveErrPath != null) {
			errorGobbler.close();
		}
		
		if (isFinishedNormal()) {
			cmdPath.moveFileOut();
		}
		cmdPath.deleteTmpFile();
	}

	/** 必须等程序启动后才能获得
	 * 得到 输入给cmd 的 output流，可以往里面写东西
	 */
	public OutputStream getStdin() {
		while (!process.isCmdStarted()) {
			try { Thread.sleep(1); } catch (InterruptedException e) { }
		}
		return process.getStdIn();
	}
	
	/** 是否将err写入外部指定的文件中，而不是写入cmd中的文件，同时会定时--约2秒， 刷新 输出文件<p>
	 * 使用场景：cmd命令在std上会有标准输出和错误输出信息，那么我们希望将输出信息写入一个文本，<br>
	 * 因为标准输出和错误输出的信息量一般不多，所以写入很长时间也不一定会刷新，那么我们这里就会<br>
	 * 定时的刷新文本，以保证能及时看到输出信息结果
	 * @param writeErrTxt
	 */
	private void setErrorStream(boolean writeErrTxt) {
		errorGobbler = new StreamGobbler(process.getStdErr());
		if (!getCmdInErrStream) {
			if (saveErrPath != null) {
				FileOperate.createFolders(FileOperate.getPathName(saveErrPath));
				//标准输出流不能被关闭
				TxtReadandWrite txtWrite = new TxtReadandWrite(saveErrPath, true);
				errorGobbler.setOutputStream(txtWrite.getOutputStream(), writeErrTxt);
			} else if (lsErrorInfo != null) {
				errorGobbler.setLsInfo(lsErrorInfo, lineNumErr, true);
			} else {
				errorGobbler.setOutputStream(System.err, false);
			}
		} else {
			errorGobbler.setGetInputStream(true);
		}
	}

	/** 是否将std写入外部指定的文件中，而不是写入cmd中的文件，同时会定时--约2秒， 刷新 输出文件<p>
	 * 使用场景：cmd命令在std上会有标准输出和错误输出信息，那么我们希望将输出信息写入一个文本，<br>
	 * 因为标准输出和错误输出的信息量一般不多，所以写入很长时间也不一定会刷新，那么我们这里就会<br>
	 * 定时的刷新文本，以保证能及时看到输出信息结果
	 * @param writeStdTxt
	 */
	private void setStdStream(boolean writeStdTxt) {
		outputGobbler = new StreamGobbler(process.getStdOut());
		if (!getCmdInStdStream) {
			if (saveFilePath != null) {
				FileOperate.createFolders(FileOperate.getPathName(saveFilePath));
				//标准输出流不能被关闭
				TxtReadandWrite txtWrite = new TxtReadandWrite(saveFilePath, true);
				outputGobbler.setOutputStream(txtWrite.getOutputStream(), writeStdTxt);
			} else if (lsOutInfo != null) {
				outputGobbler.setLsInfo(lsOutInfo, lineNumStd, false);
			} else {
				outputGobbler.setOutputStream(System.out, false);
			}
		} else {
			outputGobbler.setGetInputStream(true);
		}
	}
	
	/** 运行，出错会抛出异常
	 * @param info 输入异常的信息，譬如软件名，类似
	 * bwa error: 这种，后面会附上具体运行的代码等
	 */
	public void runWithExp(String info) {
		run();
		if (!isFinishedNormal()) {
			throw new ExceptionCmd(info, this);
		}
	}
	/** 运行，出错会抛出异常 */
	public void runWithExp() {
		run();
		if (!isFinishedNormal()) {
			throw new ExceptionCmd(this);
		}
	}
	
	@Override
	protected void running() {
		String cmd = "";
		logger.info("实际运行命令: " + getCmdExeStr());
		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		try {
			doInBackgroundB();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("cmd cannot executed correctly: " + cmd);
		}
		runTime = dateTime.getElapseTime();
		if (process instanceof ProcessRemote) {
			((ProcessRemote)process).close();
		}
	}
	
	@Override
	public boolean isRunning() {
		if(finishFlag != null && finishFlag.flag == null)
			return true;
		return false;
	}

	/** 是否正常结束 */
	public boolean isFinishedNormal() {
		if (finishFlag != null && finishFlag.flag != null && finishFlag.flag == 0) {
			return true;
		}
		return false;
	}

	/** 返回运行所耗时间，单位ms */
	public long getRunTime() {
		return runTime;
	}

	/** 不能实现 */
	@Deprecated
	public void threadSuspend() { }

	/** 不能实现 */
	@Deprecated
	public synchronized void threadResume() { }

	/** 终止线程，在循环中添加 */
	public void threadStop() {
		if (process != null && process.isCmdStarted()) {
			try {
				process.stopProcess();
			} catch (Exception e) {
				logger.error("stop thread error:\n" + getCmdExeStr(), e);
			}
		}
	}

	/** 添加引号，一般是文件路径需要添加引号 **/
	public static String addQuot(String pathName) {
		return "\"" + pathName + "\"";
	}
	
	/** 将输入的被引号--包括英文的单引号和双引号--包围的path信息修改为相对路径 */
	public static String makePathToRelative(String input) {
		PatternOperate patternOperate = new PatternOperate("\"(.+?)\"|\'(.+?)\'", false);
		List<String> lsInfo = patternOperate.getPat(input, 1,2);		
		for (String string : lsInfo) {
			input = input.replace(string, FileOperate.getFileName(string));
		}
		
		return input;
	}
	
	static class FinishFlag {
		Integer flag = null;
	}
}

class StreamGobbler extends Thread {
	private static final Logger logger = Logger.getLogger(StreamGobbler.class);
	
	/** 每2000ms刷新一次txt文本，这是因为写入错误行会很慢，刷新就可以做到及时看结果 */
	private static final int txtFlushTime = 2000;
		
	InputStream is;
	OutputStream os;
	LinkedList<String> lsInfo;
	FinishFlag finishFlag;
	boolean isFinished = false;
	boolean getInputStream = false;
	int lineNum = 500;
	/** 如果将输出信息写入lsInfo中，是否还将这些信息打印到控制台 */
	boolean isSysout = false;
	
	/** 是否按照写入txt的格式来写入流 */
	boolean isWriteToTxt = false;
	DateUtil dateUtil = new DateUtil();
	
	StreamGobbler(InputStream is) {
		this.is = is;
	}
	/**
	 *  指定一个out流，cmd的输出流就会定向到该流中<br>
	 * 该方法和{@link #setGetInputStream(boolean)} 冲突
	 * @param os 输出流
	 * @param isWriteToTxt 是否按照写入txt的格式来写输出流，并且定时刷新
	 * true则表示会从输入流中逐行读取，然后写入 os，并且会定时刷新os，以保证能够及时看到输出文件中的内容
	 */
	public void setOutputStream(OutputStream os, boolean isWriteToTxt) {
		this.os = os;
		this.isWriteToTxt = isWriteToTxt;
	}

	
	/** 是否要获取输入流，默认为false<br>
	 * {@link #setOutputStream(OutputStream)} 会覆盖该方法
	 *  */
	public void setGetInputStream(boolean getInputStream) {
		this.getInputStream = getInputStream;
	}
	public void setLsInfo(LinkedList<String> lsInfo, int linNum, boolean isSysout) {
		this.lsInfo = lsInfo;
		this.lineNum = linNum;
		this.isSysout = isSysout;
	}
	public boolean isFinished() {
		return isFinished;
	}
	public InputStream getCmdOutStream() {
		return is;
	}
	public void run() {
		dateUtil.setStartTime();
		isFinished = false;
		if (!getInputStream) {
			if (os == null) {
				exhaustInStream(is);
			} else {
				if (isWriteToTxt) {
					Timer timer = new Timer();

				    timer.schedule(new TimerTask() {
						public void run() {
							try { os.flush(); } catch (Exception e) {e.printStackTrace(); }							
						}
					}, txtFlushTime, txtFlushTime);
					
					writeToTxt(is, os);
					timer.cancel();
				} else {
					copyLarge(is, os);
				}

			}
			isFinished = true;
		}
	}
	
	private void exhaustInStream(InputStream inputStream) {
		try {
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (lsInfo != null) {
					lsInfo.add(line);
					i++;
					if (i > lineNum) {
						lsInfo.poll();
					}
//					if (isSysout) {
						logger.info(line);
//					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/** 从流中读取string，然后写入outputStream，同时也会写入lsInfo */
	private void writeToTxt(InputStream inputStream, OutputStream outputStream) {
		try {
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			int i = 0;
			while ((line = br.readLine()) != null) {
				if (lsInfo != null) {
					lsInfo.add(line);
					i++;
					if (i > lineNum) {
						lsInfo.poll();
					}	
				}
//				if (isSysout) {
					logger.info(line);
//				}
				outputStream.write((DateUtil.getDateDetail() + " " + line + TxtReadandWrite.ENTER_LINUX).getBytes());
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static final int EOF = -1;
  /**
    * Copies bytes from a large (over 2GB) <code>InputStream</code> to an
    * <code>OutputStream</code>.
    * <p>
    * This method uses the provided buffer, so there is no need to use a
    * <code>BufferedInputStream</code>.
    * <p>
    *
    * @param input  the <code>InputStream</code> to read from
    * @param output  the <code>OutputStream</code> to write to
    * @param buffer the buffer to use for the copy
    * @return the number of bytes copied
    * @throws NullPointerException if the input or output is null
    * @throws IOException if an I/O error occurs
    * @since 2.2
    */
	public static long copyLarge(final InputStream input, final OutputStream output) {
		try {
			byte[] buffer = new byte[1024 * 4];
			long count = 0;
			int n = 0;
			while (EOF != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		} catch (Exception e) {
			throw new RuntimeException("copy info error", e);
		}

	}
	
	/** 关闭输出流 */
	public void close() {
		//不用关闭输入流，因为process会自动关闭该流
		try {
			os.flush();
			os.close();
		} catch (Exception e) { }
	}
}




class ProgressData {
	public String strcmdInfo;
	/**
	 * true : info false : error
	 */
	public boolean info;
}
