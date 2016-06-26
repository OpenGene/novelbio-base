package com.novelbio.base.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好<p>
 * <b>管道只支持最后的一个 &gt; </b>
 * 例如 "bwa aaa bbb &gt; ccc"，此时会根据ccc的后缀，gz还是bz2，自动选择相应的压缩流<br>
 * <b>不支持这种</b> "bwa aaa bbb | grep sd &gt; ccc"
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String> {
	private static final Logger logger = Logger.getLogger(CmdOperate.class);
	/** 一般都需要打印日志的，除非像 ps -ef 类似的工作就不需要打印日志 */
	boolean needLog = true;
	/** 进程 */
	IntProcess process;
	
	/** 临时文件在文件夹 */
	String scriptFold = "";
	
	//TODO ========在使用docker之后，这两个可以考虑不用了========
	/** 是否产生标准输出信息的文件，只有在将输出文件写入临时文件夹的情况
	 * 下才会产生标准输出流信息文件，目的是给用户反馈目前的软件进度 */
	@Deprecated
	boolean isStdoutInfo = false;
	/** 是否产生标准错误信息的文件，只有在将输出文件写入临时文件夹的情况
	 * 下才会产生标准错误流信息文件，目的是给用户反馈目前的软件进度 */
	@Deprecated
	boolean isStderrInfo = false;
	//====================================================
	
	/** 结束标志，0表示正常退出 */
	FinishFlag finishFlag;
	long runTime = 0;
	/** 标准输出的信息 */
	LinkedList<String> lsOutInfo;
	/** 出错输出的信息 */
	LinkedList<String> lsErrorInfo = new LinkedList<>();
	
	/** 标准输入到cmd命令的流，一个cmd命令一般只有这一个流 */
	StreamIn streamIn;
	StreamOut errorGobbler;
	StreamOut outputGobbler;
	/** 是否将本该输出到控制台的结果依然写入控制台 */
	boolean isOutToTerminate = true;
	boolean isPrintCmd = true;
	
	/** 是否需要获取cmd的标准输出流 */
	boolean getCmdInStdStream = false;
	/** 是否需要获取cmd的标准错误流 */
	boolean getCmdInErrStream = false;
	
	/** 用来传递参数，拷贝输入输出文件夹的类 */
	CmdPath cmdPath = new CmdPath();
	
	/** 如果选择用list来保存结果输出，最多保存500行的输出信息 */
	int lineNumStd = 1000;
	/** 如果选择用list来保存错误输出，最多保存500行的输出信息 */
	int lineNumErr = 5000;//最多保存5000行的输出信息
	
	/** 输出本程序正在运行时的参数等信息，本功能也用docker替换了 */
	@Deprecated
	String outRunInfoFileName;
	
	/** 将cmd写入sh文件的具体文件 */
	String cmd1SH;

	public CmdOperate() {
		process = new ProcessCmd();
	}
	
	/**
	 * 初始化后直接开新线程即可 先写入Shell脚本，再运行。
	 * 一般用不到，只有当直接运行cmd会失败时才考虑使用该方法。
	 * 目前遇到的情况是直接跑hadoop streaming程序会出错，采用该方法跑
	 * 就不会出错了
	 * 
	 * @param cmd
	 *            输入命令
	 * @param cmdWriteInFileName
	 *            将命令写入的文本
	 */
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		process = new ProcessCmd();
		FileOperate.validateFileName(cmdWriteInFileName);
		setCmdFile(cmd, cmdWriteInFileName);
	}
	
	/** 是否将本该输出到控制台的结果依然写入控制台，一般在运行长时间任务的时候，
	 * 譬如 tophat等，需要写入控制台，如果一些譬如获得version之类的命令，就不需要
	 * 往控制台写了
	 * @param isOutToTerminate 默认是true
	 */
	public void setTerminateWriteTo(boolean isOutToTerminate) {
		this.isOutToTerminate = isOutToTerminate;
		this.isPrintCmd = isOutToTerminate;
	}
	
	/**
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * 
	 * @param cmd
	 */
	private void setCmdFile(String cmd, String cmdWriteInFileName) {
		while (true) {
			cmd1SH = cmdPath.getTmp() + cmdWriteInFileName.replace("\\", "/") + DateUtil.getDateAndRandom() + ".sh";
			if (!FileOperate.isFileExist(cmd1SH)) {
				break;
            }
        }
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(cmd);
		txtCmd1.close();
		cmdPath.clearLsCmd();
		cmdPath.addCmdParam("sh");
		cmdPath.addCmdParam(cmd1SH);
	}

	
	public CmdOperate(List<String> lsCmd) {
		process = new ProcessCmd();
		cmdPath.setLsCmd(lsCmd);
	}
	public CmdOperate(String ip, String user, List<String> lsCmd, String idrsa) {
		process = new ProcessRemote(ip, user);
		((ProcessRemote)process).setKeyFile(idrsa);
		cmdPath.setLsCmd(lsCmd);
	}
	
	public CmdOperate(String ip, String user, String idrsa) {
		process = new ProcessRemote(ip, user);
		((ProcessRemote)process).setKeyFile(idrsa);
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

	/** 设定临时文件夹，会把重定向的文件拷贝到这个文件夹中 */
	public void setCmdTmpPath(String tmpPath) {
		cmdPath.setTmpPath(tmpPath);
	}
	/** 是否删除临时文件夹中的文件，如果连续的cmd需要顺序执行，考虑不删除 */
	public void setRetainTmpFiles(boolean isRetainTmpFiles) {
		cmdPath.setRetainTmpFiles(isRetainTmpFiles);
	}
	public void setLsCmd(List<String> lsCmd) {
		cmdPath.setLsCmd(lsCmd);
	}
	protected void setNeedLog(boolean isNeedLog) {
		this.needLog = isNeedLog;
	}
	
	/** 设定输出信息，默认保存在stdout文件夹下
	 * 使用docker remote替换
	 * @param outRunInfoFileName
	 */
	@Deprecated
	public void setOutRunInfoFileName(String outRunInfoFileName) {
		if (outRunInfoFileName == null) {
			this.outRunInfoFileName = "";
		} else {
			this.outRunInfoFileName = outRunInfoFileName;	
		}
	}
	
	/** 标准输入到cmd命令的流，一个cmd命令一般只有这一个流 */
	public void setInputStream(StreamIn streamIn) {
		this.streamIn = streamIn;
	}
	/** 设定本cmd命令的标准输入文件，该文件会通过标准输入流输入cmd命令 */
	public void setInputFile(String inputFile) {
		this.streamIn = new StreamIn();
		streamIn.setInputFile(inputFile);
	}
	/** 设定本cmd命令的标准输入文件，该文件会通过标准输入流输入cmd命令 */
	public void setInputFile(Path inputFile) {
		this.streamIn = new StreamIn();
		streamIn.setInputFile(inputFile);
	}
	
	/**
	 * @deprecated 用yarn来获取log日志<br><br>
	 * 
	 *  设定标准输出流，如果是这里指定，则会即时刷新<br>
	 * 本设置会被cmd中自带的 &gt; 重定向覆盖
	 * @param stdOutPath
	 * @param isSaveTmp 是否先保存为临时文件，等结束后再修改回来。如果只是随便看看结果就设置为false
	 * @param isDelete 完成后是否删除输出文件, 如果需要删除文件，则认为该文件只是展示信息使用，会采用txt模式输出
	 */
	public void setStdOutPath(String stdOutPath, boolean isSaveTmp, boolean isDelete) {
		cmdPath.setSaveFilePath(stdOutPath, isSaveTmp);
		cmdPath.setJustDisplayStd(isDelete);
		this.isStdoutInfo = isDelete;
	}
	/**
	 * @deprecated 用yarn来获取log日志<br><br>
	 * 
	 * 设定标准错误流，如果是这里指定，则会即时刷新<br>
	 * 本设置会被cmd中自带的 2> 重定向覆盖
	 * @param stdErrPath
	 * @param isSaveTmp 是否先保存为临时文件，等结束后再修改回来。如果只是随便看看结果就设置为false
	 * @param isDelete 完成后是否删除输出文件, 如果需要删除文件，则认为该文件只是展示信息使用，会采用txt模式输出
	 */
	public void setStdErrPath(String stdErrPath, boolean isSaveTmp, boolean isDelete) {
		cmdPath.setSaveErrPath(stdErrPath, isSaveTmp);
		cmdPath.setJustDisplayErr(isDelete);
		this.isStderrInfo = isDelete;
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
	@Deprecated
	public void addCmdParamInput(String input, boolean isAddToLsCmd) {
		cmdPath.addCmdParamInput(input, isAddToLsCmd);
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamInput(String input) {
		cmdPath.addCmdParamInput(input, false);
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamInput(List<String> lsInput) {
		for (String path : lsInput) {
			cmdPath.addCmdParamInput(path, false);
		}
	}
	/**
	 * <b>本参数只用来标记需要重定位的文件输出参数，不加入cmd的参数列表</b><br>
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 */
	public void addCmdParamOutput(String output) {
		addCmdParamOutput(output, false);
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamOutput(List<String> lsOut) {
		for (String path : lsOut) {
			cmdPath.addCmdParamOutput(path, false);
		}
	}
	/**
	 * 添加文件输出参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * 由于是需要先将输入文件保存到临时文件夹，等程序结束后再拷贝回实际文件夹，所以应该不是从std走文件了，
	 * 所以在这里把 setJustDisplayErr 和 setJustDisplayStd都设置为true<br>
	 * 本参数不影响 {@link #getStreamStd()} 和 {@link #getStreamErr()} 方法
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	@Deprecated
	public void addCmdParamOutput(String output, boolean isAddToLsCmd) {
//		if (StringOperate.isRealNull(cmdPath.getSaveErrPath())) {
//			isStderrInfo = true;
//			cmdPath.setSaveErrPath(output + "errorInfo.txt", false);
//			cmdPath.setJustDisplayErr(true);
//		}
//		if (StringOperate.isRealNull(cmdPath.getSaveStdPath())) {
//			isStdoutInfo = true;
//			cmdPath.setSaveFilePath(output + "stdInfo.txt", false);
//			cmdPath.setJustDisplayStd(true);
//		}
//		
//		if (outRunInfoFileName == null) {
//			outRunInfoFileName = output + "_RunInfo.txt";
//		}
		cmdPath.addCmdParamOutput(output, isAddToLsCmd);
	}
	
	/** 是否将hdfs的路径，改为本地路径，<b>默认为true</b><br>
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr<br>
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 */
	public void setIsConvertHdfsToLocal(boolean isConvertHdfs2Loc) {
		cmdPath.setConvertHdfs2Loc(isConvertHdfs2Loc);
	}
	
	/** 是否将输入文件拷贝到临时文件夹，默认为false */
	public void setRedirectInToTmp(boolean isRedirectInToTmp) {
		cmdPath.setRedirectInToTmp(isRedirectInToTmp);
	}
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹，默认为false */
	public void setRedirectOutToTmp(boolean isRedirectOutToTmp) {
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
	
	/** 返回执行的具体cmd命令，不会将文件路径删除，给绝对路径 */
	public String getCmdExeStr() {
		return getCmdExeStrReal();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String getCmdExeStrModify() {
		String[] resultCmd = cmdPath.getCmdExeStrModify();
		replaceInputStreamFile(resultCmd);
		return ArrayOperate.cmbString(resultCmd, " ");
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String getCmdExeStrReal() {
		String[] resultCmd = cmdPath.getCmdExeStrReal();
		replaceInputStreamFile(resultCmd);
		return ArrayOperate.cmbString(resultCmd, " ");
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	@VisibleForTesting
	public String getRunCmd() {
		String[] resultCmd = cmdPath.getRunCmd();
		replaceInputStreamFile(resultCmd);
		return ArrayOperate.cmbString(resultCmd, " ");
	}
	
	private void replaceInputStreamFile(String[] resultCmd) {
		if (streamIn != null && !StringOperate.isRealNull(streamIn.getInputFile())) {
			for (int i = 0; i < resultCmd.length; i++) {
				if (resultCmd[i].equals("-")) {
					resultCmd[i] = streamIn.getInputFile();
				}
			}
		}
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


	/** 
	 * 默认不启用
	 * 只有当{@link #setGetCmdInErrStream(boolean)} 为false时才有用 <br>
	 * 用getStdOut获得标准输出的结果， */
	public void setGetLsStdOut() {
		lsOutInfo = new LinkedList<>();
	}
	
	/** 默认不启用，将错误信息输出到标准错误流 */
	public void setPutErrToStderr() {
		lsErrorInfo = null;
	}
	/** 需要获得多少行错误输出流 */
	public void setGetLsErrOut(int lineNum) {
		this.lineNumErr = lineNum;
	}
	
	/** 程序执行完后可以看错误输出<br>
	 * 仅返回最多{@link #lineNum}行的信息
	 * 内部实现为linkedlist
	 * <br/>
	 * <b>调用此方法时,参数{@link #getCmdInStdStream}必须是false.否则不能获得所需结果.</b>
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
		String[] cmdRun = cmdPath.getRunCmd();
		
//		cmdRunInfo = new CmdRunInfo();
//		cmdRunInfo.setOutFile(outRunInfoFileName);
//		cmdRunInfo.setProcess(process);
		
		process.exec(cmdRun);
		
		//等待30ms，如果不等待，某些命令会阻塞输出流，不知道为什么，譬如以下这个命令
		//String cmd="hisat2 -p 3 -5 0 -3 0 --min-intronlen 20 --max-intronlen 500000 -1 /media/nbfs/nbCloud/public/AllProject/project_574ba1fb45ce3ad2541b9de7/task_575e719660b2beecc9ae3422/other_result/S45_07A_150500152_L006_1_part.fq.gz -2 /media/nbfs/nbCloud/public/AllProject/project_574ba1fb45ce3ad2541b9de7/task_575e719660b2beecc9ae3422/other_result/S45_07A_150500152_L006_2_part.fq.gz -S /home/novelbio/tmp/2016-06-14-09-27-3130048_tmp.hisatDateBaseTest1/hisatDateBaseTest.sam";

		Thread.sleep(30);
		Thread threadInStream = setAndGetInStream();
		if (threadInStream != null) {
			threadInStream.start();
		}
		setStdStream();
		setErrorStream();
		
		errorGobbler.setDaemon(true);
		outputGobbler.setDaemon(true);
		errorGobbler.start();
		outputGobbler.start();
//		cmdRunInfo.startWriteRunInfo();

		finishFlag.flag = process.waitFor();
		
		if (needLog) logger.info("finish running cmd, finish flag is: " + finishFlag.flag);
		
		//这几个感觉跟cmd是直接绑定的，如果cmd关闭了似乎这几个都会自动停止
		if (threadInStream != null) {
			threadInStream.join();
		}
		outputGobbler.joinStream();
		errorGobbler.joinStream();
		
		if (needLog) logger.info("close out stream");
		
		closeOutStream();
//		cmdRunInfo.setFinish();

		//不管是否跑成功，都移出文件夹
		cmdPath.moveFileOut();
		cmdPath.deleteTmpFile();
	}
	
	private Thread setAndGetInStream() {
		String inFile = cmdPath.getStdInFile();
		if (streamIn == null && StringOperate.isRealNull(inFile)) {
			return null;
		}
		
		if (streamIn != null && !StringOperate.isRealNull(inFile)) {
			throw new ExceptionCmd("cannot both set stdin and have \">\" in  script");
		}
		if (streamIn == null && !StringOperate.isRealNull(inFile)) {
			streamIn = new StreamIn();
			streamIn.setInputFile(inFile);
		}
		streamIn.setProcessInStream(process.getStdIn());
		Thread threadStreamIn = new Thread(streamIn);
		threadStreamIn.setDaemon(true);
		return threadStreamIn;
	}
	
	/** 是否将std写入外部指定的文件中，而不是写入cmd中的文件，同时会定时--约2秒， 刷新 输出文件<p>
	 * 使用场景：cmd命令在std上会有标准输出和错误输出信息，那么我们希望将输出信息写入一个文本，<br>
	 * 因为标准输出和错误输出的信息量一般不多，所以写入很长时间也不一定会刷新，那么我们这里就会<br>
	 * 定时的刷新文本，以保证能及时看到输出信息结果
	 * @param isWriteStdTxt 是否是写入文本并需要定时刷新
	 * @param isWriteStdTips 是否需要每隔几分钟写一小段话以表示程序还在运行中
	 */
	private void setStdStream() {
		String outPath = cmdPath.getSaveStdTmp(); 
		outputGobbler = new StreamOut(process.getStdOut(), process, isOutToTerminate, true);
		outputGobbler.setDaemon(true);
		if (!getCmdInStdStream) {
			if (outPath != null) {
				FileOperate.createFolders(FileOperate.getPathName(outPath));
				//标准输出流不能被关闭，从txt拿流是因为如果输出写为.gz，txt会给流套上gz流的壳
				TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
				outputGobbler.setOutputStream(txtWrite.getOutputStream(), cmdPath.isJustDisplayStd());
				if (cmdPath.isJustDisplayStd() && lsOutInfo != null) {
					outputGobbler.setLsInfo(lsOutInfo, lineNumStd);
				}
			} else if (lsOutInfo != null) {
				outputGobbler.setLsInfo(lsOutInfo, lineNumStd);
			} else {
				outputGobbler.setOutputStream(System.out, false);
			}
		} else {
			outputGobbler.setGetInputStream(true);
		}
	}
	
	/** 是否将err写入外部指定的文件中，而不是写入cmd中的文件，同时会定时--约2秒， 刷新 输出文件<p>
	 * 使用场景：cmd命令在std上会有标准输出和错误输出信息，那么我们希望将输出信息写入一个文本，<br>
	 * 因为标准输出和错误输出的信息量一般不多，所以写入很长时间也不一定会刷新，那么我们这里就会<br>
	 * 定时的刷新文本，以保证能及时看到输出信息结果
	 * @param isWriteErrTxt 是否是写入文本并需要定时刷新
	 * @param isWriteErrTips 是否需要每隔几分钟写一小段话以表示程序还在运行中
	 */
	private void setErrorStream() {
		String errPath = cmdPath.getSaveErrTmp();
		errorGobbler = new StreamOut(process.getStdErr(), process, isOutToTerminate, false);
		errorGobbler.setDaemon(true);
		if (!getCmdInErrStream) {
			if (errPath != null) {
				FileOperate.createFolders(FileOperate.getPathName(errPath));
				//标准输出流不能被关闭
				TxtReadandWrite txtWrite = new TxtReadandWrite(errPath, true);
				errorGobbler.setOutputStream(txtWrite.getOutputStream(), cmdPath.isJustDisplayErr());
				if (cmdPath.isJustDisplayErr() && lsErrorInfo != null) {
					errorGobbler.setLsInfo(lsErrorInfo, lineNumErr);
				}
			} else if (lsErrorInfo != null) {
				errorGobbler.setLsInfo(lsErrorInfo, lineNumErr);
			} else {
				errorGobbler.setOutputStream(System.err, false);
			}
		} else {
			errorGobbler.setGetInputStream(true);
		}
	}
	
	/** 是否有">" 或 "1>"符号，如果有，返回输出的文件名 */
	public String getSaveStdOutFile() {
		return cmdPath.getSaveStdPath();
	}
	/** 是否有"2>"符号，如果有，返回输出的文件名 */
	public String getSaveStdErrFile() {
		return cmdPath.getSaveStdPath();
	}
	
	/** 关闭输出流 */
	private void closeOutStream() {
		if (!getCmdInStdStream) {
			if (cmdPath.isJustDisplayStd()) {
				if (isFinishedNormal()) {
					outputGobbler.close(DateUtil.getNowTimeStr() + " Task Finish Normally");
				} else {
					outputGobbler.close(DateUtil.getNowTimeStr() + " Task Finish AbNormally");
				}
			} else if(cmdPath.getSaveStdPath() != null) {
				outputGobbler.close();
			}
		}
		
		if (!getCmdInErrStream) {
			if (cmdPath.isJustDisplayErr()) {
				if(isFinishedNormal()) {
					errorGobbler.close("Task Finish Normally");
				} else {
					errorGobbler.close("Task Finish AbNormally");
				}
			} else if(cmdPath.getSaveErrPath() != null) {
				errorGobbler.close();
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

	/** 运行，出错会抛出异常
	 * @param info 输入异常的信息，譬如软件名，类似
	 * bwa error: 这种，后面会附上具体运行的代码等
	 */
	public void runWithExp(String info) {
		run();
		if (!isFinishedNormal()) {
			if (!StringOperate.isRealNull(info) && !info.endsWith("\n")) {
				info = info + "\n";
			}
			throw new ExceptionCmd(info, this);
		}
	}
	
	/** 运行但并不报错，适合获取软件版本信息等。因为有些软件在获取版本时会返回错误，譬如bwa，输入bwa就是返回错误 */
	public void run() {
		cmdPath.generateTmPath();
		cmdPath.copyFileIn();
		super.run();
		if (FileOperate.isFileExist(cmd1SH)) {
			FileOperate.deleteFileFolder(cmd1SH);
        }
	}
	
	/** 运行，出错会抛出异常 */
	public void runWithExp() {
		cmdPath.generateTmPath();
		cmdPath.copyFileIn();
		super.run();
		if (!isFinishedNormal()) {
			throw new ExceptionCmd(this);
		}
		if (FileOperate.isFileExist(cmd1SH)) {
			FileOperate.deleteFileFolder(cmd1SH);
        }
	}
	
	/** 把{@link CmdOperate#runWithExp()} 拆成两个方法
	 * 分别是 {@link CmdOperate#prepare()} 和 {@link CmdOperate#runWithExpNoPrepare()}
	 * 
	 * 本步骤解析cmd命令，并拷贝需要的文件到指定文件夹中
	 */
	public void prepare() {
		cmdPath.generateTmPath();
		cmdPath.generateRunCmd(true);
	}
	
	@Override
	protected void running() {
		String cmd = "";
		String realCmd = getCmdExeStr();
		if (isPrintCmd) {
			logger.info("run cmd: " + realCmd);
		}

		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		try {
			doInBackgroundB();
		} catch (Exception e) {
			try {
				if (streamIn != null) streamIn.threadStop();
				
			} catch (Exception e2) {
				// TODO: handle exception
			}
			e.printStackTrace();
			logger.error("cmd cannot executed correctly: " + cmd, e);
		}
		runTime = dateTime.getElapseTime();
		if (process instanceof ProcessRemote) {
			((ProcessRemote)process).closeSession();
		}
		
		if (isFinishedNormal()) {
			cmdPath.moveResultFile();
			if (isStderrInfo) {
				FileOperate.deleteFileFolder(cmdPath.getSaveErrPath());
			}
			if (isStdoutInfo) {
				FileOperate.deleteFileFolder(cmdPath.getSaveStdPath());
			}
			FileOperate.deleteFileFolder(outRunInfoFileName);
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
//		stopRunInfo();
		
		if (process != null && process.isCmdStarted()) {
			try {
				process.stopProcess();
				process = null;
			} catch (Exception e) {
				logger.error("stop thread error:\n" + getCmdExeStr(), e);
			}
		}
	}
	
//	private void stopRunInfo() {
//		if (cmdRunInfo != null) {
//			cmdRunInfo.setFinish();
//			cmdRunInfo = null;
//		}
//	}
	
	/** 如果是远程cmd，用这个关闭连接 */
	public void closeRemote() {
		if (process instanceof ProcessRemote) {
			((ProcessRemote)process).close();
		}
	}

	/** 去除引号，一般是文件路径需要添加引号 **/
	public static String removeQuot(String pathName) {
		if (pathName.startsWith("\"") && pathName.endsWith("\"")
				|| pathName.startsWith("\'") && pathName.endsWith("\'")
				) {
			pathName = pathName.substring(1, pathName.length() - 1);
		}
		return pathName;
	}
	
	/** 添加引号，一般是文件路径需要添加引号 **/
	public static String addQuot(String pathName) {
		return "\"" + pathName + "\"";
	}
	
	/** 添加引号，一般是文件路径需要添加引号 **/
	public static String addQuotSingle(String pathName) {
		return "'" + pathName + "'";
	}
	
	/** 将输入的被引号--包括英文的单引号和双引号--包围的path信息修改为相对路径 */
	public static String makePathToRelative(String input) {
		PatternOperate patternOperate = new PatternOperate("/{0,1}(hdfs\\:){0,1}/{0,1}([\\w\\.]+?/)+\\w+", false);
		List<String> lsInfo = patternOperate.getPat(input);		
		for (String string : lsInfo) {
			input = input.replace(string, FileOperate.getFileName(string));
		}
		return input;
	}
	
	static class FinishFlag {
		Integer flag = null;
	}
	
	public static String getExceptionInfo(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			t.printStackTrace(pw);
			return sw.toString();
		} finally {
			pw.close();
		}
	}
	
	/** 把一个完整的cmd命令切分成list-Cmd，其中单双引号为一个整体 */
	public static List<String> splitCmd(String cmd) {
		List<String> lsCmdUnit = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		//是否进入了引号，引号内部为一个整体
		boolean isInQuote = false;
		//默认为双引号
		boolean isDoubleQuote = true;
		for (char c : cmd.toCharArray()) {
			if (c == '"' || c == '\'') {
				if (!isInQuote) {
					isInQuote = true;
					isDoubleQuote = c=='"' ? true : false;
				} else {
					isInQuote = !(isDoubleQuote == (c=='"')); 
				}
			}
			
			if ((c == ' ' || c == ';' || c == '"') && !isInQuote) {
				if (c=='"') stringBuilder.append('"');
				
				String info = stringBuilder.toString();
				stringBuilder = new StringBuilder();
				if (!StringOperate.isRealNull(info)) {
					lsCmdUnit.add(info);
				}
			} else {
				stringBuilder.append(c);
			}
		}
		String info = stringBuilder.toString();
		stringBuilder = new StringBuilder();
		if (!StringOperate.isRealNull(info)) {
			lsCmdUnit.add(info);
		}
		return lsCmdUnit;
	}
}

/** 用docker去查看container的  */
@Deprecated
class CmdRunInfo {
	private static final Logger logger = Logger.getLogger(CmdRunInfo.class);
	/** 每分钟写一个信息到文本中，意思该程序还在运行中，主要是针对RNAmapping这种等待时间很长的程序 */
	private static final int timeTxtWiteTips = 120000;
	/** 运行进程的pid */
	IntProcess process;
	String outFile;
	Timer timerWriteTips;
	TxtReadandWrite txtWrite;
			
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	public void setProcess(IntProcess process) {
		this.process = process;
	}
	public void setFinish() {
		if (timerWriteTips != null) {
			logger.info("stop RunInfo timer");
			timerWriteTips.cancel();
		}
		
		if (StringOperate.isRealNull(outFile)) {
			return;
		}
		txtWrite.writefileln("stop get running info");
		txtWrite.close();
	}
	
	public void startWriteRunInfo() {
		if (StringOperate.isRealNull(outFile)) {
			return;
		}
		timerWriteTips = new Timer();
		txtWrite = new TxtReadandWrite(outFile, true);
		timerWriteTips.schedule(new TimerTask() {
			public void run() {
				synchronized (this) {
					try {
						txtWrite.writefileln(DateUtil.getNowTimeStr() + " Program Is Still Running, This Tip Display " + timeTxtWiteTips/1000 + " seconds per time");
						List<ProcessInfo> lsProcInfo = process.getLsProcInfo();
						if (lsProcInfo.isEmpty()) return;
						
						txtWrite.writefileln(ProcessInfo.getTitle());
						for (ProcessInfo processInfo : lsProcInfo) {
							txtWrite.writefileln(processInfo.toString());
						}
						txtWrite.flush();
					} catch (Exception e) {e.printStackTrace(); }
					txtWrite.flush();
				}
			}
		}, 1000, timeTxtWiteTips);		
	}
	
}

