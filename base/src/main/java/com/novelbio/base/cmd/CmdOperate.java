package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate.FinishFlag;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
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

	/** 是否将pid加2，如果是写入文本然后sh执行，则需要加上2 */
	boolean shPID = false;

	/** 进程 */
	IntProcess process;
	/** 待运行的命令 */
	List<String> lsCmd = new ArrayList<>();
	
	/** 临时文件在文件夹 */
	String scriptFold = "";
	
	/** 标准输出流保存的路径 */
	String saveFilePath;
	/** 标准错误流保存的路径 */
	String saveErrPath;
	
	/**输出文件的名字 */
	Set<String> setOutfile = new HashSet<>();
	/** 是否将输出文件重定位到临时文件夹，最后再拷贝回去 */
	boolean isRedirectToTmp;
	
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
			lsCmd.add(string);
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
		shPID = true;
		logger.info(newCmd);
		String cmd1SH = PathDetail.getTmpConfFold() + cmdWriteInFileName.replace("\\", "/") + DateUtil.getDateAndRandom() + ".sh";
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(newCmd);
		txtCmd1.close();
		lsCmd.clear();
		lsCmd.add("sh");
		lsCmd.add(cmd1SH);
	}
	public CmdOperate(List<String> lsCmd) {
		process = new ProcessCmd();
		this.lsCmd = lsCmd;
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
		this.lsCmd = lsCmd;
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
		this.lsCmd = lsCmd;
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
		this.lsCmd = lsCmd;
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
		this.lsCmd = lsCmd;
	}
	
	/** 如果为null就不加入 */
	public void addCmdParam(String param) {
		if (!StringOperate.isRealNull(param)) {
			lsCmd.add(param);
		}
	}
	
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹 */
	public void setRedirectToTmp(boolean isRedirectToTmp) {
		this.isRedirectToTmp = isRedirectToTmp;
	}
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	public void addCmdParamOutput(String output, boolean isAddToLsCmd) {
		if (StringOperate.isRealNull(output)) {
			throw new ExceptionCmd("Output is null");
		}
		setOutfile.add(output);
		if (isAddToLsCmd) {
			lsCmd.add(output);
		}
	}
	
	/** 如果param为null则返回 */
	public void addCmdParam(List<String> lsCmd) {
		if (lsCmd == null) {
			return;
		}
		String[] param = lsCmd.toArray(new String[0]);
		addCmdParam(param);
	}
	/** 如果param为null则返回 */
	public void addCmdParam(String[] param) {
		if (param == null) {
			return;
		}
		for (String string : param) {
			if (StringOperate.isRealNull(string)) {
				throw new ExceptionCmd("param contains null");
			}
			lsCmd.add(string);
		}
	}

	
	/** 返回执行的具体cmd命令，不会将文件路径删除，仅给相对路径 */
	public String getCmdExeStr() {
		return getCmdExeStrModify();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String getCmdExeStrModify() {
		return getCmdModify(lsCmd);
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String getCmdExeStrReal() {
		return ArrayOperate.cmbString(lsCmd.toArray(new String[0]), " ");
	}
	
	public static String getCmdModify(List<String> lsCmd) {
		String[] cmdArray = lsCmd.toArray(new String[0]);
		return getCmdModify(cmdArray);
	}
	
	public static String getCmdModify(String[] cmdArray) {
		StringBuilder strBuilder = new StringBuilder();
		for (String cmdTmp : cmdArray) {
			String[] subcmd = cmdTmp.split("=");
			strBuilder.append(" ");
			strBuilder.append(FileOperate.getFileName(subcmd[0]));
			for (int i = 1; i < subcmd.length; i++) {
				strBuilder.append("=");
				strBuilder.append(FileOperate.getFileName(subcmd[i]));
			}
		}
		return strBuilder.toString().trim();
	}
	
	/** 是否获得cmd的标准输出流
	 * <b>优先级高于cmd命令中的重定向</b>
	 * @param getCmdInStdStream
	 */
	public void setGetCmdInStdStream(boolean getCmdInStdStream) {
		this.getCmdInStdStream = getCmdInStdStream;
	}
	/** 是否获得cmd的错误输出流
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
	 * 设定了{@link #setGetCmdInStdStream(boolean)} 才有用 */
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
	 * 设定了{@link #setGetCmdInErrStream(boolean)} 才有用 */
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
		
		Map<String, String> mapOutFile2Tmp = getMapOutFile2TmpFile();
		process.exec(getRunCmd(mapOutFile2Tmp));

		setErrorStream();
		setStdStream();

		errorGobbler.start();
		outputGobbler.start();
		
		finishFlag.flag = process.waitFor();
		outputGobbler.join();
		errorGobbler.join();
		if (!getCmdInStdStream || saveFilePath != null) {
			outputGobbler.close();
		}
		if (!getCmdInErrStream || saveErrPath != null) {
			errorGobbler.close();
		}
		
		if (isRedirectToTmp) {
			copyFile(mapOutFile2Tmp);
		}
	}
	
	/**
	 * 输出文件名和临时文件夹的对照表，用途是首先将输出结果输出到tmp文件夹下，然后再拷贝到hdfs中。防止直接使用nfs报错
	 * key 输出文件名，value 重定向到的临时文件夹名
	 * @return
	 */
	private Map<String, String> getMapOutFile2TmpFile() {
		//key 输出文件名，value 重定向到的临时文件夹名
		Map<String, String> mapOutFile2Tmp = new HashMap<>();
		if (isRedirectToTmp) {
			int i = 0;
			for (String outFile : setOutfile) {
				String outRealPath = FileOperate.getPathName(outFile);
				boolean isCreatFold = FileOperate.createFolders(outRealPath);
				if (!isCreatFold) {
					throw new ExceptionCmd("create folder error: " + outRealPath);
				}
				i++;//防止起随机名字的时候出现相同的名字
				String outTmpName = getRedirectPath(PathDetail.getTmpPathRandomWithSep(FileOperate.getFileName(outFile) + i), outFile);
				
				//如果输出文件存在了就把文件移动到临时文件夹中，这是考虑到bwa输入文件
				//为chr.fa，并且没有输出文件，这时候就可以把输入的chr.fa当做输出文件
				if (FileOperate.isFileExistAndBigThanSize(outFile, 0)) {
					FileOperate.copyFile(outFile, outTmpName, true);
				}
				mapOutFile2Tmp.put(outFile, outTmpName);
			}
		}
		return mapOutFile2Tmp;
	}
	
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @return
	 */
	private String[] getRunCmd(Map<String, String> mapOutPath2TmpOut) {
		List<String> lsReal = new ArrayList<>();
		boolean stdOut = false;
		boolean errOut = false;
		for (String tmpCmd : lsCmd) {
			if (tmpCmd.equals(">")) {
				stdOut = true;
				continue;
			} else if (tmpCmd.equals("2>")) {
				errOut = true;
				continue;
			}
			
			if (!errOut && !stdOut && isRedirectToTmp && setOutfile.contains(tmpCmd)) {
				tmpCmd = mapOutPath2TmpOut.get(tmpCmd);
			}
			
			if (stdOut) {
				saveFilePath = tmpCmd;
				stdOut = false;
				continue;
			} else if (errOut) {
				saveErrPath = tmpCmd;
				errOut = false;
				continue;
			}
			
			lsReal.add(convertToLocalCmd(tmpCmd));
		}
		String[] realCmd = lsReal.toArray(new String[0]);
		shPID = false;
		return realCmd;
	}
	
	/** 将cmd中的hdfs路径改为本地路径 */
	private static String convertToLocalCmd(String tmpCmd) {
		String[] subcmd = tmpCmd.split("=");
		subcmd[0] = FileHadoop.convertToLocalPath(subcmd[0]);
		for (int i = 1; i < subcmd.length; i++) {
			subcmd[i] = FileHadoop.convertToLocalPath(subcmd[i]);
		}
		String result = subcmd[0];
		for (int i = 1; i < subcmd.length; i++) {
			result = result + "=" + subcmd[i];
		}
		return result;
	}
	
	private static String getRedirectPath(String tmpPath, String outFileName) {
		String fileName = "";
		if (!outFileName.endsWith("/") && !outFileName.endsWith("\\")) {
			fileName = FileOperate.getFileName(outFileName);
		}
		String tmpFileName = tmpPath + fileName;
		return tmpFileName;
	}

	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中
	 * notmove是不需要移动的文件名
	 * @param tmpPath
	 * @param resultPath
	 * @param notMove
	 * @param isDelFile 如果出错是否删除原来的文件
	 */
	private void copyFile(Map<String, String> mapOutFile2Tmp) {
		for (String outFile : mapOutFile2Tmp.keySet()) {
			String outTmpPath = FileOperate.getPathName(mapOutFile2Tmp.get(outFile));
			String outRealPath = FileOperate.getPathName(outFile);
			
			List<String> lsFilesFinish = FileOperate.getFoldFileNameLs(outTmpPath, "*", "*");
			for (String filePath : lsFilesFinish) {
				String  filePathResult = filePath.replaceFirst(outTmpPath, outRealPath);
				boolean isSucess = FileOperate.copyFile(filePath, filePathResult, false);
				if (!isSucess) {
					throw new ExceptionCmd("cannot copy " + filePath + " to " + filePathResult);
				}
			}
		}
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
	
	private void setErrorStream() {
		errorGobbler = new StreamGobbler(process.getStdErr());
		if (!getCmdInErrStream) {
			if (saveErrPath != null) {
				//标准输出流不能被关闭
				TxtReadandWrite txtWrite = new TxtReadandWrite(saveErrPath, true);
				errorGobbler.setOutputStream(txtWrite.getOutputStream());
			} else if (lsErrorInfo != null) {
				errorGobbler.setLsInfo(lsErrorInfo, lineNumErr, true);
			} else {
				errorGobbler.setOutputStream(System.err);
			}
		} else {
			errorGobbler.setGetInputStream(true);
		}
	}

	private void setStdStream() {
		outputGobbler = new StreamGobbler(process.getStdOut());
		if (!getCmdInStdStream) {
			if (saveFilePath != null) {
				//标准输出流不能被关闭
				TxtReadandWrite txtWrite = new TxtReadandWrite(saveFilePath, true);
				outputGobbler.setOutputStream(txtWrite.getOutputStream());
			} else if (lsOutInfo != null) {
				outputGobbler.setLsInfo(lsOutInfo, lineNumStd, false);
			} else {
				outputGobbler.setOutputStream(System.out);
			}
		} else {
			outputGobbler.setGetInputStream(true);
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

	/**
	 * 返回运行所耗时间，单位ms
	 * 
	 * @return
	 */
	public long getRunTime() {
		return runTime;
	}

	/** 不能实现 */
	@Deprecated
	public void threadSuspend() {
	}

	/**
	 * 不能实现
	 * */
	@Deprecated
	public synchronized void threadResume() {
	}

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
	Logger logger = Logger.getLogger(StreamGobbler.class);
	InputStream is;
	OutputStream os;
	LinkedList<String> lsInfo;
	FinishFlag finishFlag;
	boolean isFinished = false;
	boolean getInputStream = false;
	int lineNum = 500;
	/** 如果将输出信息写入lsInfo中，是否还将这些信息打印到控制台 */
	boolean isSysout = false;
	StreamGobbler(InputStream is) {
		this.is = is;
	}
	/** 制定一个out流，cmd的输出流就会定向到该流中<br>
	 * 该方法和{@link #setGetInputStream(boolean)} 冲突
	 */
	public void setOutputStream(OutputStream os) {
		this.os = os;
	}
	/** 是否要获取输入流，默认为false<br>
	 * 该方法和{@link #setOutputStream(OutputStream)} 冲突
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
		isFinished = false;
		if (!getInputStream) {
			if (os == null) {
				exhaustInStream(is);
			} else {
				try {
					copyLarge(is, os);
				} catch (IOException ioe) {
					ioe.printStackTrace();
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
					if (isSysout) {
						System.out.println(line);
					}
				}
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
	public static long copyLarge(final InputStream input, final OutputStream output)
					throws IOException {
		byte[] buffer = new byte[1024 * 4];
		long count = 0;
		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
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
