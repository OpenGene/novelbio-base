package com.novelbio.base.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.base.util.ServiceEnvUtil;

/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好<p>
 * <b>管道只支持最后的一个 &gt; </b>
 * 例如 "bwa aaa bbb &gt; ccc"，此时会根据ccc的后缀，gz还是bz2，自动选择相应的压缩流<br>
 * <b>不支持这种</b> "bwa aaa bbb | grep sd &gt; ccc"
 * @author zong0jie
 */
public class CmdOperate extends RunProcess {
	private static final Logger logger = Logger.getLogger(CmdOperate.class);
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
	protected FinishFlag finishFlag;
	long runTime = 0;
	/** 标准输出的信息 */
	LinkedList<String> lsOutInfo = new LinkedList<>();
	/** 出错输出的信息 */
	LinkedList<String> lsErrorInfo = new LinkedList<>();
	/** 如果选择用list来保存结果输出，最多保存500行的输出信息 */
	int lineNumStd = 1000;
	/** 如果选择用list来保存错误输出，最多保存500行的输出信息 */
	int lineNumErr = 5000;//最多保存5000行的输出信息
	
	/** 标准输入到cmd命令的流，一个cmd命令一般只有这一个流 */
	StreamIn streamIn;
	protected StreamOut errorGobbler;
	protected StreamOut outputGobbler;
	/** 
	 * 是否将标准流写入标准流，有些类似获取ip或者获取软件版本，获取进程ps-ef
	 * 这时候我们会截取标准流到list中，如果再写入标准流，在日志中看就会很麻烦。
	 * 
	 * 这个关闭后，就不会打印log日志，也不会把命令写入标准流
	 */
	boolean needLog = true;
	
	/** 是否需要获取cmd的标准输出流 */
	boolean getCmdInStdStream = false;
	/** 是否需要获取cmd的标准错误流 */
	boolean getCmdInErrStream = false;
	
	/** 用来传递参数，拷贝输入输出文件夹的类 */
	protected CmdOrderGenerator cmdOrderGenerator = new CmdOrderGenerator();
	protected CmdMoveFile cmdMoveFile = CmdMoveFile.getInstance(!ServiceEnvUtil.isCloudEnv());

	/** 输出本程序正在运行时的参数等信息，本功能也用docker替换了 */
	@Deprecated
	String outRunInfoFileName;
	
	/** 将cmd写入sh文件的具体文件 */
	String cmd1SH;

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
		setCmdFile(cmd, null, cmdWriteInFileName);
	}

	/**
	 * 初始化后直接开新线程即可 先写入Shell脚本，再运行。
	 * 一般用不到，只有当直接运行cmd会失败时才考虑使用该方法。
	 * 目前遇到的情况是直接跑hadoop streaming程序会出错，采用该方法跑
	 * 就不会出错了
	 * 
	 * @param cmd 输入命令
	 * @param param 可以添加类似 -euxo pipefail 这种参数。注意不能包含 \t \n 这种特殊字符
	 * @param cmdWriteInFileName
	 *            将命令写入的文本
	 */
	public CmdOperate(String cmd, String param, String cmdWriteInFileName) {
		process = new ProcessCmd();
		FileOperate.validateFileName(cmdWriteInFileName);
		setCmdFile(cmd, param, cmdWriteInFileName);
	}
	public CmdOperate(List<String> lsCmd) {
		process = new ProcessCmd();
		cmdOrderGenerator.setLsCmd(lsCmd);
	}
	
	/**
	 * @param lsCmd 给定命令
	 * @param path 指定在哪个文件夹下运行命令，注意不支持hdfs和oss
	 */
	public CmdOperate(List<String> lsCmd, String path) {
		process = new ProcessCmd(path);
		cmdOrderGenerator.setLsCmd(lsCmd);
	}
	
	public CmdOperate(String ip, String user, List<String> lsCmd, String idrsa) {
		process = new ProcessRemote(ip, user);
		((ProcessRemote)process).setKeyFile(FileOperate.getPath(idrsa));
		cmdOrderGenerator.setLsCmd(lsCmd);
	}
	
	public CmdOperate(String ip, String user, Path idrsa) {
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
		cmdOrderGenerator.setLsCmd(lsCmd);
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
		cmdOrderGenerator.setLsCmd(lsCmd);
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
		cmdOrderGenerator.setLsCmd(lsCmd);
	}
	
	/**
	 * 远程登录的方式运行cmd命令，<b>cmd命令运行完毕后会断开连接</b>
	 * @param ip
	 * @param usr
	 * @param pwd
	 * @param lsCmd
	 * @param keyFile 私钥文件
	 */
	public CmdOperate(String ip, String usr, String pwd, List<String> lsCmd, Path keyFile) {
		process = new ProcessRemote(ip, usr, pwd);
		((ProcessRemote)process).setKeyFile(keyFile);
		cmdOrderGenerator.setLsCmd(lsCmd);
	}
	
	/** 支持输入空值 */
	public void setMapEnv(Map<String, String> mapEnv) {
		((ProcessCmd)process).setMapEnv(mapEnv);
	}
	public void addEnv(String key, String value) {
		((ProcessCmd)process).addEnv(key, value);
	}
	/**
	 * 如果要外部移动数据，可以把这个配置进来，
	 * 但是必须在{@link CmdOperate}创建完毕后第一时间设置
	 */
	public void setCmdMoveFile(CmdMoveFile cmdMoveFile) {
		this.cmdMoveFile = cmdMoveFile;
	}
	
	/**
	 * 上一个task所输出的文件与临时文件的对照表
	 * @param cmdPathCluster
	 */
	public void setCmdPathCluster(CmdPathCluster cmdPathCluster) {
		cmdMoveFile.setCmdPathCluster(cmdPathCluster);
	}
	/** 是否将本该输出到控制台的结果依然写入控制台，一般在运行长时间任务的时候，
	 * 譬如 tophat等，需要写入控制台，如果一些譬如获得version之类的命令，就不需要
	 * 往控制台写了
	 * @param isOutToTerminate 默认是true
	 */
	public void setTerminateWriteTo(boolean needLog) {
		cmdMoveFile.setNeedLog(needLog);
		this.needLog = needLog;
	}
	
	/**
	 * 
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * @param cmd
	 * @param param 可以添加类似 -euxo pipefail 这种参数。注意不能包含 \t \n 这种特殊字符
	 * @param cmdWriteInFileName
	 */
	private void setCmdFile(String cmd, String param, String cmdWriteInFileName) {
		while (true) {
			cmd1SH = cmdMoveFile.getTmpPath() + cmdWriteInFileName.replace("\\", "/");
			cmd1SH = FileOperate.changeFileSuffix(cmd1SH, "."+DateUtil.getDateAndRandom(), "sh");
			if (!FileOperate.isFileExist(cmd1SH)) {
				break;
            }
        }
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(cmd);
		txtCmd1.close();
		cmdMoveFile.clearLsCmd();
		cmdOrderGenerator.addCmdParam("sh");
		if (!StringOperate.isRealNull(param)) {
			String[] ss = param.trim().split(" ");
			for (String paramUnit : ss) {
				cmdOrderGenerator.addCmdParam(paramUnit);
			}
		}
		cmdOrderGenerator.addCmdParam(cmd1SH);
	}
	
	/** 设定临时文件夹，会把重定向的文件拷贝到这个文件夹中 */
	public void setCmdTmpPath(String tmpPath) {
		cmdMoveFile.setTmpPath(tmpPath);
	}
	/** 是否删除临时文件夹中的文件，如果连续的cmd需要顺序执行，考虑不删除 */
	public void setRetainTmpFiles(boolean isRetainTmpFiles) {
		cmdMoveFile.setRetainTmpFiles(isRetainTmpFiles);
	}
	public void setLsCmd(List<String> lsCmd) {
		cmdOrderGenerator.setLsCmd(lsCmd);
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
	 * 默认false，也就是说当"2>"存在就无法通过{@link #getLsStdOut()}获取信息 <br>
	 * <br>
	 * 当"2>"存在时仍然想要通过{@link #getLsErrOut()}获取信息时设置为true。<br>
	 * 当为true时，则会按照文本格式获取stderr，并且保存起来，最后可以通过{@link #getLsErrOut()}获取错误信息<br>
	 * <br>
	 * 注意如果设置为true，并且 "2>" 输出的是二进制文件譬如bam文件，则会报错。
	 */
	public void setIsStdErrTxt(boolean isStdErrTxt) {
		cmdOrderGenerator.setJustDisplayErr(isStdErrTxt);
	}
	/**
	 * 默认false，也就是说当">"存在就无法通过{@link #getLsStdOut()}获取信息<br>
	 * <br>
	 * 当">"存在时仍然想要通过{@link #getLsStdOut()}获取信息时设置为true。<br>
	 * 当为true时，则会按照文本格式获取stdout，并且保存起来，最后可以通过{@link #getLsStdOut()}获取错误信息<br>
	 * <br>
	 * 注意如果设置为true，并且 ">" 输出的是二进制文件譬如bam文件，则会报错。
	 */
	public void setIsStdoutTxt(boolean isStdoutTxt) {
		cmdOrderGenerator.setJustDisplayStd(isStdoutTxt);
	}
	
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamInput(String input) {
		cmdMoveFile.addCmdParamInput(input);
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamInput(List<String> lsInput) {
		for (String path : lsInput) {
			cmdMoveFile.addCmdParamInput(path);
		}
	}
	/**
	 * 如果存在部分文件需要拷贝，部分文件不需要拷贝
	 * 最好把不需要拷贝的记录下来做过滤用
	 * @param lsInput
	 */
	public void addCmdParamInputNotCopy(List<String> lsInput) {
		cmdMoveFile.addCmdParamInputNotCopy(lsInput);
	}
	/**
	 * 如果存在部分文件需要拷贝，部分文件不需要拷贝
	 * 最好把不需要拷贝的记录下来做过滤用
	 * @param lsInput
	 */
	public void addCmdParamOutputNotCopy(List<String> lsOutput) {
		cmdMoveFile.addCmdParamOutputNotCopy(lsOutput);
	}
	/**
	 * <b>本参数只用来标记需要重定位的文件输出参数，不加入cmd的参数列表</b><br>
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 */
	public void addCmdParamOutput(String output) {
		cmdMoveFile.addCmdParamOutput(output);
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，默认不加入参数list，仅仅标记一下
	 * @param output
	 */
	public void addCmdParamOutput(List<String> lsOut) {
		for (String path : lsOut) {
			cmdMoveFile.addCmdParamOutput(path);
		}
	}
	
	/** 是否将hdfs的路径，改为本地路径，<b>默认为true</b><br>
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr<br>
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 */
	public void setIsConvertHdfsToLocal(boolean isConvertHdfs2Loc) {
		cmdOrderGenerator.setConvertHdfs2Loc(isConvertHdfs2Loc);
	}
	
	/** 是否将输入文件拷贝到临时文件夹，默认为false */
	public void setRedirectInToTmp(boolean isRedirectInToTmp) {
		cmdMoveFile.setRedirectInToTmp(isRedirectInToTmp);
	}
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹，默认为false */
	public void setRedirectOutToTmp(boolean isRedirectOutToTmp) {
		cmdMoveFile.setRedirectOutToTmp(isRedirectOutToTmp);
	}
	
	/** 如果param为null则返回 */
	public void addCmdParam(List<String> lsCmd) {
		cmdOrderGenerator.addCmdParam(lsCmd);
	}
	/** 如果param为null则返回 */
	public void addCmdParam(String[] param) {
		cmdOrderGenerator.addCmdParam(param);
	}
	
	/** 返回执行的具体cmd命令，不会将文件路径删除，给绝对路径 */
	public String getCmdExeStr() {
		return getCmdExeStrReal();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String getCmdExeStrModify() {
		String[] resultCmd = cmdOrderGenerator.getCmdExeStrModify(cmdMoveFile);
		replaceInputStreamFile(resultCmd);
		return ArrayOperate.cmbString(resultCmd, " ");
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String getCmdExeStrReal() {
		String[] resultCmd = cmdOrderGenerator.getCmdExeStrReal(cmdMoveFile);
		replaceInputStreamFile(resultCmd);
		return ArrayOperate.cmbString(resultCmd, " ");
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	@VisibleForTesting
	public String getRunCmd() {
		String[] resultCmd = cmdOrderGenerator.getRunCmd(cmdMoveFile);
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
	 * 默认获取最后1000行标准输出流。
	 * 如果命令中存在 ">, 1>" 或 {@link #setGetCmdInStdStream(boolean)} 为true，则不起作用
	 */
	public void setGetLsStdOut(int lineNum) {
		this.lineNumStd = lineNum;
	}
	/** 需要获得多少行错误输出流，默认可查看5000行
	 * 如果命令中存在 "2>" 或 {@link #setGetCmdInErrStream(boolean)} 为true，则不起作用
	 */
	public void setGetLsErrOut(int lineNum) {
		this.lineNumErr = lineNum;
	}
	
	/**
	 * 程序执行完后可以看错误输出<br>
	 * 仅返回最多{@link #lineNum}行的信息<br>
	 * 内部实现为linkedlist<br>
	 * <br>
	 * 如果命令中存在 "2>" 或 {@link #setGetCmdInErrStream(boolean)} 为true，则不起作用<br>
	 * 此时可以通过设置{@link #setIsStdErrTxt(boolean)}为true强行打开该功能
	 */
	public List<String> getLsErrOut() {
		waitStreamOutErr();
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
	/** null表示没有运行 */
	public Integer getErrorCode() {
		if (finishFlag == null) {
			return 1;
		}
		return finishFlag.getFlag();
	}
	/** 程序执行完后可以看标准输出<br>
	 * 仅返回最多{@link #lineNum}行的信息<br>
	 * 内部实现为linkedlist<br>
	 * <br>
	 * 如果命令中存在 ">" 或 {@link #setGetCmdInStdStream(boolean)} 为true，则不起作用<br>
	 * 此时可以通过设置{@link #setIsStdoutTxt(boolean)}为true强行打开该功能
	 */
	public List<String> getLsStdOut() {
		waitStreamOutStd();
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
		waitStreamOutStd();
		return outputGobbler.getCmdOutStream();
	}
	/** 获得命令行的错误输出流， <br>
	 * 设定了{@link #setGetCmdInErrStream(boolean)} 为true才有用 */
	public InputStream getStreamErr() {
		waitStreamOutErr();
		return errorGobbler.getCmdOutStream();
	}
	
	private void waitStreamOutStd() {
		while (outputGobbler == null || !outputGobbler.isStarted()) {
			if (finishFlag != null && finishFlag.isFinish()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (outputGobbler == null && finishFlag.isStart()) {
			throw new ExceptionCmd("cmd doesn't have output stream: " + getCmdExeStr());
		} else if (outputGobbler.getRunThreadStat() == RunThreadStat.finishAbnormal || outputGobbler.getRunThreadStat() == RunThreadStat.finishInterrupt) {
			throw new ExceptionCmd("get output stream error: " + getCmdExeStr(), outputGobbler.getException());
		}
	}
	private void waitStreamOutErr() {
		while (errorGobbler == null || !errorGobbler.isStarted()) {
			if (finishFlag != null && (!finishFlag.isStart() || finishFlag.isFinish())) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (errorGobbler == null && finishFlag.isStart()) {
			throw new ExceptionCmd("cmd doesn't have stderr stream: " + getCmdExeStr());
		} else if (errorGobbler.getRunThreadStat() == RunThreadStat.finishAbnormal
				|| errorGobbler.getRunThreadStat() == RunThreadStat.finishInterrupt) {
			throw new ExceptionCmd("get output stream error: " + getCmdExeStr(), outputGobbler.getException());
		}
	}
	

	/**
	 * 直接运行cmd，可能会出错 返回两个arraylist-string 第一个是Info 第二个是error
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	protected void doInBackgroundB() throws Exception {
		cmdOrderGenerator.setIsGetStdoutStream(getCmdInStdStream);
		cmdOrderGenerator.setIsGetStderrStream(getCmdInErrStream);
		
		String[] cmdRun = cmdOrderGenerator.getRunCmd(cmdMoveFile);
		process.exec(cmdRun);
		finishFlag.start();

		//等待30ms，如果不等待，某些命令会阻塞输出流，不知道为什么，譬如以下这个命令
		//String cmd="hisat2 -p 3 -5 0 -3 0 --min-intronlen 20 --max-intronlen 500000 -1 /media/nbfs/nbCloud/public/AllProject/project_574ba1fb45ce3ad2541b9de7/task_575e719660b2beecc9ae3422/other_result/S45_07A_150500152_L006_1_part.fq.gz -2 /media/nbfs/nbCloud/public/AllProject/project_574ba1fb45ce3ad2541b9de7/task_575e719660b2beecc9ae3422/other_result/S45_07A_150500152_L006_2_part.fq.gz -S /home/novelbio/tmp/2016-06-14-09-27-3130048_tmp.hisatDateBaseTest1/hisatDateBaseTest.sam";
		
		Thread.sleep(50);
		Thread threadInStream = setAndGetInStream();
		if (threadInStream != null) {
			threadInStream.start();
		}
		try {
			setStdStream();
			setErrorStream();
			
			errorGobbler.setDaemon(true);
			outputGobbler.setDaemon(true);
			errorGobbler.start();
			outputGobbler.start();

			finishFlag.flag = process.waitFor();
			
			if (needLog) logger.info("finish running cmd, finish flag is: " + finishFlag.flag);
			
			//这几个感觉跟cmd是直接绑定的，如果cmd关闭了似乎这几个都会自动停止
			if (threadInStream != null) {
				threadInStream.join();
			}
			outputGobbler.joinStream();
			errorGobbler.joinStream();
			
			if (needLog) logger.info("close out stream");
		} catch (Throwable t) {
			//说明还没运行到  process.waitFor(); 就报错了
			if (!finishFlag.isFinish()) finishFlag.setFinishError();
			throw t;
		} finally {
			closeOutStream();
		}
	}
	
	private Thread setAndGetInStream() {
		String inFile = cmdOrderGenerator.getStdInFile();
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
	 * @throws IOException 
	 */
	protected void setStdStream() throws IOException {
		String outPath = cmdOrderGenerator.getSaveStdTmp(); 
		outputGobbler = new StreamOut(process.getStdOut(), process, needLog, true);
		outputGobbler.setDaemon(true);
		
		if (!getCmdInStdStream) {
			if (outPath != null) {
				FileOperate.createFolders(FileOperate.getPathName(outPath));
				OutputStream os = cmdOrderGenerator.isOutStdWithSuffix() ? FileOperate.getOutputStreamWithSuffix(outPath) : FileOperate.getOutputStream(outPath);
				outputGobbler.setOutputStream(os, cmdOrderGenerator.isJustDisplayStd());
				if (cmdOrderGenerator.isJustDisplayStd() && lsOutInfo != null) {
					outputGobbler.setLsInfo(lsOutInfo, lineNumStd);
				}
			} else if (lsOutInfo != null) {
				outputGobbler.setLsInfo(lsOutInfo, lineNumStd);
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
	 * @throws IOException 
	 */
	protected void setErrorStream() throws IOException {
		String errPath = cmdOrderGenerator.getSaveErrTmp();
		errorGobbler = new StreamOut(process.getStdErr(), process, needLog, false);
		errorGobbler.setDaemon(true);
		
		if (!getCmdInErrStream) {
			if (errPath != null) {
				FileOperate.createFolders(FileOperate.getPathName(errPath));
				OutputStream os = cmdOrderGenerator.isOutErrWithSuffix() ? FileOperate.getOutputStreamWithSuffix(errPath) : FileOperate.getOutputStream(errPath);
				errorGobbler.setOutputStream(os, cmdOrderGenerator.isJustDisplayErr());
				if (cmdOrderGenerator.isJustDisplayErr() && lsErrorInfo != null) {
					errorGobbler.setLsInfo(lsErrorInfo, lineNumErr);
				}
			} else if (lsErrorInfo != null) {
				errorGobbler.setLsInfo(lsErrorInfo, lineNumErr);
			}
		} else {
			errorGobbler.setGetInputStream(true);
		}
	}
	
	/** 是否有">" 或 "1>"符号，如果有，返回输出的文件名 */
	public String getSaveStdOutFile() {
		return cmdOrderGenerator.getSaveStdPath();
	}
	
	/** 是否有"2>"符号，如果有，返回输出的文件名 */
	public String getSaveStdErrFile() {
		return cmdOrderGenerator.getSaveStdPath();
	}
	
	/** 关闭输出流 */
	private void closeOutStream() {
		if (!getCmdInStdStream) {
			if (cmdOrderGenerator.isJustDisplayStd()) {
				if (isFinishedNormal()) {
					outputGobbler.close(DateUtil.getNowTimeStr() + " Task Finish Normally");
				} else {
					outputGobbler.close(DateUtil.getNowTimeStr() + " Task Finish AbNormally");
				}
			} else if(cmdOrderGenerator.getSaveStdPath() != null) {
				outputGobbler.close();
			}
		}
		
		if (!getCmdInErrStream) {
			if (cmdOrderGenerator.isJustDisplayErr()) {
				if(isFinishedNormal()) {
					errorGobbler.close("Task Finish Normally");
				} else {
					errorGobbler.close("Task Finish AbNormally");
				}
			} else if(cmdOrderGenerator.getSaveErrPath() != null) {
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
		validateIsFinishNormally(info);
	}
	
	/** 运行但并不报错，适合获取软件版本信息等。因为有些软件在获取版本时会返回错误，譬如bwa，输入bwa就是返回错误 */
	public void run() {
		super.run();
		if (FileOperate.isFileExistAndNotDir(cmd1SH)) {
			FileOperate.deleteFileFolder(cmd1SH);
        }
	}
	
	/**
	 * 运行，出错会抛出异常
	 * 
	 * 可用于ScriptXml中move  > Bam文件
	 * 因为通过流输出的bam文件是一个独立线程，很可能cmd运行结束后，
	 * bam文件还没处理完。这时候就需要在外部等bam文件线程结束后再移动文件。
	 * 因此这里就选择不把输出文件移出去。
	 * 
	 * @param isCopyFileInAndRecordFiles false 则需要手动调用 {@link #prepareAndMoveFileIn()},  {@link #copyFileIn()} 和 {@link #recordFilesWhileRedirectOutToTmp()}
	 * 或者调用方法 {@link #setCmdMoveFile(CmdMoveFile)} 从外部传入 {@link CmdMoveFile} 对象
	 * @param isMoveFileOut false 则需要手动调用 {@link #moveFileOut()}
	 */
	public void runWithExp(boolean isCopyFileInAndRecordFiles, boolean isMoveFileOut) {
		runWithExp(null, isCopyFileInAndRecordFiles, isMoveFileOut);
	}
	
	/** 运行，出错会抛出异常，不需要调用方法
	 *  {@link #copyFileIn()} 
	 *   {@link #recordFilesWhileRedirectOutToTmp()} 
	 *   和 {@link #moveFileOut()} */
	public void runWithExp() {
		super.run();
		validateIsFinishNormally(null);
	}
	
	@Override
	protected void running() {
		running(true, true);
	}
	
	/**
	 * 可用于Script中move  > Bam文件
	 * 因为通过流输出的bam文件是一个独立线程，很可能cmd运行结束后，
	 * bam文件还没处理完。这时候就需要在外部等bam文件线程结束后再移动文件。
	 * 因此这里就选择不把输出文件移出去。
	 * 
	 * @param isCopyFileInAndRecordFiles false 则需要手动调用 {@link #copyFileIn()} 和 {@link #recordFilesWhileRedirectOutToTmp()}
	 * @param isMoveFileOut false 则需要手动调用 {@link #moveFileOut()}
	 */
	private void runWithExp(String info, boolean isCopyFileInAndRecordFiles, boolean isMoveFileOut) {
		flagStop = false;
		runThreadStat = RunThreadStat.running;
		
		try {
			running(isCopyFileInAndRecordFiles, isMoveFileOut);
			runThreadStat = RunThreadStat.finishNormal;
		} catch (Throwable e) {
			e.printStackTrace();
			exception = e;
			runThreadStat = RunThreadStat.finishAbnormal;
		}
		flagStop = true;
		
		validateIsFinishNormally(info);
	}
	
	private void validateIsFinishNormally(String info) {
		if (!StringOperate.isRealNull(info) && !info.endsWith("\n")) {
			info = info + "\n";
		}
		ExceptionCmd e = null;
		if (runThreadStat == RunThreadStat.finishAbnormal) {
			if (!StringOperate.isRealNull(info)) {
				e = new ExceptionCmd(info, this, exception);
			} else {
				e = new ExceptionCmd(this, exception);
			}
		}
		if (!isFinishedNormal()) {
			if (!StringOperate.isRealNull(info)) {
				e = new ExceptionCmd(info, this);
			} else {
				e = new ExceptionCmd(this);
			}
			e.setErrorCode(finishFlag.getFlag());
		}
		if (e != null) {
			throw e;
		}
		if (FileOperate.isFileExistAndNotDir(cmd1SH)) {
			FileOperate.deleteFileFolder(cmd1SH);
        }
	}
	
	/**
	 * 把{@link CmdOperate#runWithExp()} 拆成两个方法<br>
	 * 分别是 {@link CmdOperate#prepareAndMoveFileIn()} 和 {@link CmdOperate#runWithExpNoPrepare()}<br>
	 * <br>
	 * 本步骤是解析cmd命令，主要目的是获取 > 之后所跟的路径<br>
	 */
	public void prepareAndMoveFileIn() {
		cmdMoveFile.prepareAndMoveFileIn();
	}
	/**
	 * 生成cmd命令，仅用于ScriptBuildFacade中的多线程部分
	 */
	public void generateRunCmd() {
		cmdOrderGenerator.generateRunCmd(true, cmdMoveFile);
	}
	/**
	 * 复制文件到临时文件夹<br>
	 * 当{@link #runWithExp(boolean, boolean)} 第一个参数为false时调用
	 * 包含{@link #prepareAndMoveFileIn()}的功能
	 */
	public void copyFileIn() {
		cmdMoveFile.copyFileIn();
	}
	/** 记录临时文件夹下有多少文件，用于后面删除时跳过 <br>
	 * 当{@link #runWithExp(boolean, boolean)} 第一个参数为false时调用，在 {@link #copyFileIn()} 之后调用
	 */
	public void recordFilesWhileRedirectOutToTmp() {
		cmdMoveFile.recordFilesWhileRedirectOutToTmp();
	}
	
	private void running(boolean isCopyFileInAndRecordFiles, boolean isMoveFileOut) {
		finishFlag = new FinishFlag();
		if (isCopyFileInAndRecordFiles) {
			cmdMoveFile.prepareAndMoveFileIn();
		}
		
		String realCmd = getCmdExeStr();
		if (needLog) {
			logger.info("run cmd: " + realCmd);
		}

		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		
		RuntimeException runtimeException = null;
		try {
			doInBackgroundB();
		} catch (RuntimeException e) {
			finishFlag.flag = 1;
			runtimeException = e;
			try { if (streamIn != null) streamIn.threadStop(); } catch (Exception e2) { }
		} catch (Exception e) {
			finishFlag.flag = 1;
			runtimeException = new ExceptionCmd(e);
			try { if (streamIn != null) streamIn.threadStop(); } catch (Exception e2) { }
		}
		
		if (isFinishedNormal()) {
			cmdOrderGenerator.moveStdFiles();
			if (isStderrInfo) {
				FileOperate.deleteFileFolder(cmdOrderGenerator.getSaveErrPath());
			}
			if (isStdoutInfo) {
				FileOperate.deleteFileFolder(cmdOrderGenerator.getSaveStdPath());
			}
			FileOperate.deleteFileFolder(outRunInfoFileName);
		}
		
		//不管是否跑成功，都移出文件夹
		if (isMoveFileOut) {
			moveFileOut();
		}
		
		runTime = dateTime.getElapseTime();
		if (process instanceof ProcessRemote) {
			((ProcessRemote)process).closeSession();
		}
		if (isMoveFileOut) {
			cmdMoveFile.deleteTmpPath();
		}
		
		//最后才抛出异常
		if (runtimeException != null) {
			throw runtimeException;
		}
		
	}
	
	/** 记录临时文件夹下有多少文件，用于后面删除时跳过 
	 * 当{@link #runWithExp(boolean, boolean)} 第二个参数为false时主动调用<br>
	 * 在 {@link #runWithExp(boolean, boolean)} 之后调用
	 */
	public void moveFileOut() {
		cmdMoveFile.moveFileOut();
		cmdMoveFile.deleteTmpFile();
	}
	
	@Override
	public boolean isRunning() {
		if(finishFlag != null && finishFlag.isStart() && finishFlag.flag == null)
			return true;
		return false;
	}

	public boolean isFinished() {
		if (finishFlag != null && finishFlag.flag != null) {
			return true;
		}
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
	
	public static class FinishFlag {
		boolean isStart = false;
		Integer flag = null;
		
		public void start() {
			this.isStart = true;
		}
		public boolean isStart() {
			return isStart;
		}
		public boolean isFinish() {
			return flag != null;
		}
		/** 设定为运行失败 */
		public void setFinishError() {
			flag = 1;
		}
		public void setFlag(Integer flag) {
			this.flag = flag;
		}
		public Integer getFlag() {
			return flag;
		}
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

class CmdMvCp extends CmdOperate {
	public CmdMvCp(List<String> lsCmd) {
		super(lsCmd);
	}
	@Override
	protected void doInBackgroundB() throws Exception {
		String[] cmdRun = cmdOrderGenerator.getCmdExeStr(cmdMoveFile);
		if (cmdRun == null || cmdRun.length < 2
				|| (!cmdRun[0].equals("mv") && !cmdRun[0].equals("cp") && !cmdRun[0].equals("mkdir"))) {
			super.doInBackgroundB();
			return;
		}
		
		finishFlag.start();
		cmdRun = cmdOrderGenerator.getRunCmd(cmdMoveFile);
		List<String> lsCmd = ArrayOperate.converArray2List(cmdRun);
		try {
			runMvAndCp(lsCmd);
			runMkdir(lsCmd);
			finishFlag.setFlag(0);
		} catch (Exception e) {
			finishFlag.setFlag(1);
			throw e;
		}
	}
	/**
	 * 如果是hadoop环境且如果命令是mv或cp，则调用FileOperate进行，因为直接操作hdfs很可能会导致io问题
	 * @param lsCmdStr
	 * @return
	 */
	@VisibleForTesting
	protected static boolean runMvAndCp(List<String> lsCmdStr) {
//		if (!ServiceEnvUtil.isHadoopEnvRun()) return false;
		if (!lsCmdStr.get(0).equals("mv") && !lsCmdStr.get(0).equals("cp"))  return false;
		
		String cmd = ArrayOperate.cmbString(lsCmdStr, " ");
		boolean isCover = true;

		for (String string : lsCmdStr) {
			if (string.equalsIgnoreCase("-n") || string.equalsIgnoreCase("--no-clobber")) {
				isCover = false;
				break;
			}
		}
		
		//获取多个需要移动的文件。
		//譬如 mv a.txt b.txt c.txt /home/novelbio/d/
		//则获取 a.txt b.txt c.txt
		List<String> lsFileNeedMvOrCp = new ArrayList<>();
		boolean isFile = false;
		for (int i = 1; i < lsCmdStr.size() - 1; i++) {
			String fileName = lsCmdStr.get(i);
			if (fileName.startsWith("-")) {
				if (isFile) {
					throw new ExceptionCmd("cmd error: " + cmd);
				}
				continue;
			}
			isFile = true;
			lsFileNeedMvOrCp.add(fileName);
		}
		
		//创建输出文件夹
		String outFile = lsCmdStr.get(lsCmdStr.size() - 1);
		
		/** 仅需考虑hdfs，如果是cos则不需要考虑这个问题 */
		outFile = FileOperate.convertToHdfs(outFile);
		if (StringOperate.isRealNull(outFile)) {
			throw new ExceptionCmd("cannot move or copy file to null: " + cmd);
		}
		
		//是否拷贝到输入文件同一级的文件夹下
		//true cp /home/novelbio/  /mytest/aaa/
		//aaa 存在则为true，结果为/mytest/aaa/novelbio
		//aaa 不存在则为false，结果为 /mytest/aaa/* 其中*为novelbio文件夹中的内容
		//TODO 以上规则有待检查是否与linux命令一致
		boolean isOutFolder = FileOperate.isFileDirectory(outFile);
		if (isOutFolder) outFile = FileOperate.addSep(outFile);
		
		if (lsCmdStr.get(0).equals("mv")) {
			for (String file : lsFileNeedMvOrCp) {
				file = FileOperate.convertToHdfs(file);
				if (!FileOperate.isFileExistAndBigThan0(file)) {
					continue;
				}
				if (isOutFolder) {
					FileOperate.moveFile(isCover, file, outFile + FileOperate.getFileName(file));
				} else {
					FileOperate.moveFile(isCover, file, outFile);
				}
			}
		}
		
		if (lsCmdStr.get(0).equals("cp")) {
			for (String file : lsFileNeedMvOrCp) {
				file = FileOperate.convertToHdfs(file);
				if (!FileOperate.isFileExistAndBigThan0(file)) {
					continue;
				}
				if (isOutFolder) {
					FileOperate.copyFileFolder(file, outFile + FileOperate.getFileName(file), isCover);
				} else {
					FileOperate.copyFileFolder(file, outFile, isCover);
				}
			}
		}

		return true;
	}
	
	/**
	 * 如果是hadoop环境且如果命令是mv或cp，则调用FileOperate进行，因为直接操作hdfs很可能会导致io问题
	 * @param lsCmdStr
	 * @return
	 */
	@VisibleForTesting
	protected static boolean runMkdir(List<String> lsCmdStr) {
//		if (!ServiceEnvUtil.isHadoopEnvRun()) return false;
		if (!lsCmdStr.get(0).equals("mkdir"))  return false;
		
		String cmd = ArrayOperate.cmbString(lsCmdStr, " ");
		
		//获取多个需要移动的文件。
		//譬如 mv a.txt b.txt c.txt /home/novelbio/d/
		//则获取 a.txt b.txt c.txt
		List<String> lsFolderNeedCreate = new ArrayList<>();
		boolean isFile = false;
		for (int i = 1; i <= lsCmdStr.size() - 1; i++) {
			String folderName = lsCmdStr.get(i);
			if (folderName.startsWith("-")) {
				if (isFile) {
					throw new ExceptionCmd("cmd error: " + cmd);
				}
				continue;
			}
			isFile = true;
			lsFolderNeedCreate.add(folderName);
		}
		for (String folder : lsFolderNeedCreate) {
			folder = FileOperate.convertToHdfs(folder);
			FileOperate.createFolders(folder);
		}
		return true;
	}
}

