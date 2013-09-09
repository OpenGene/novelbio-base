package com.novelbio.base.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好
 * 
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String> {
	public static void main(String[] args) {
		String cmd = "bwa aln -n 5 -o 1 -e 30 -t 2 -l 25 -O 10 /media/hdfs/nbCloud/public/nbcplatform/genome/mouse/mm10_GRCm38/index/bwa_Chr_Index/chrAll.fa /media/hdfs/nbCloud/public/test/DNASeqMap/test_filtered_1.fq.gz > /home/novelbio/桌面/zzzz3.fai";
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.run();
		System.out.println(cmdOperate.isFinishedNormal());
	}

	private static Logger logger = Logger.getLogger(CmdOperate.class);

	/** 是否将pid加2，如果是写入文本然后sh执行，则需要加上2 */
	boolean shPID = false;

	/** 进程 */
	Process process = null;
	/** 待运行的命令 */
	String[] realCmd = null;
	/** 临时文件在文件夹 */
	String scriptFold = "";
	String saveFilePath = null;
	/** 结束标志，0表示正常退出 */
	int info = -1000;
	long runTime = 0;
	/** 标准输出的信息 */
	List<String> lsOutInfo;
	/** 出错输出的信息 */
	List<String> lsErrorInfo;
	StreamGobbler errorGobbler;
	StreamGobbler outputGobbler;

	/**
	 * 直接运行，不写入文本
	 * 
	 * @param cmd
	 */
	public CmdOperate(String cmd) {
		String[] cmds = cmd.trim().split(" ");
		setRealCmd(cmds);
	}

	/**
	 * 初始化后直接开新线程即可 先写入Shell脚本，再运行
	 * 
	 * @param cmd
	 *            输入命令
	 * @param cmdWriteInFileName
	 *            将命令写入的文本
	 */
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		setCmdFile(cmd, cmdWriteInFileName);
	}

	public CmdOperate(ArrayList<String> lsCmd) {
		String[] cmds = new String[lsCmd.size() - 1];
		setRealCmd(cmds);
	}

	public CmdOperate(String[] cmds) {
		setRealCmd(cmds);
	}

	public void setRealCmd(String[] cmds) {
		for (int i = 0;i<cmds.length;i++) {
			cmds[i] = FileHadoop.convertToLocalPath(cmds[i]);
		}
		if (cmds[cmds.length - 2].equals(">")) {
			this.saveFilePath = cmds[cmds.length - 1];
			realCmd = new String[cmds.length - 2];
			for (int i = 0; i < realCmd.length; i++) {
				realCmd[i] = cmds[i];
			}
		} else {
			this.realCmd = cmds;
		}
		shPID = false;
	}

	/**
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * 
	 * @param cmd
	 */
	public void setCmdFile(String cmd, String cmdWriteInFileName) {
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
		realCmd = new String[] { "sh", cmd1SH };
	}

	/** 需要获得标准输出流，用getStdOut获得 */
	public void setGetStdOut() {
		lsOutInfo = new ArrayList<>();
	}

	/** 需要获得标准输出流，用getStdOut获得 */
	public void setGetStdError() {
		lsErrorInfo = new ArrayList<>();
	}

	/** 程序执行完后可以看错误输出 */
	public List<String> getLsErrorInfo() {
		while (true) {
			if (errorGobbler.isFinished()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return lsErrorInfo;
	}

	public List<String> getLsOutInfo() {
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

	/**
	 * 直接运行cmd，可能会出错 返回两个arraylist-string 第一个是Info 第二个是error
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private void doInBackgroundB() throws Exception {
		info = -1000;
		// try {
		// Thread thread = new Thread(guIcmd);
		// thread.start();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(realCmd);
		logger.info("process id : " + CmdOperate.getUnixPID(process));
		// any error message?
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.out);
		errorGobbler.setLsInfo(lsErrorInfo);
		// any output?
		outputGobbler = new StreamGobbler(process.getInputStream(), FileOperate.getOutputStream(saveFilePath, true));
		outputGobbler.setLsInfo(lsOutInfo);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		info = process.waitFor();
		finishAndCloseCmd(info);
	}

	@Deprecated
	private void finishAndCloseCmd(int info) {
		// if (guIcmd != null) {
		// if (info == 0) {
		// guIcmd.closeWindow();
		// } else {
		// guIcmd.appendTxtInfo("error");
		// }
		// }
	}

	@Override
	protected void running() {
		String cmd = "";
		for (String cmd1 : realCmd) {
			cmd += " " + cmd1;
		}
		logger.info("实际运行命令: " + cmd);
		DateUtil dateTime = new DateUtil();
		dateTime.setStartTime();
		try {
			doInBackgroundB();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("cmd cannot executed correctly: " + realCmd);
		}
		runTime = dateTime.getEclipseTime();
	}

	/** 是否正常结束 */
	public boolean isFinishedNormal() {
		if (info == 0) {
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
		int pid = -10;
		try {
			pid = getUnixPID(process);
			if (pid > 0) {
				if (shPID) {
					pid = pid + 2;
				}
				System.out.println(pid);
				Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
				process.destroy();// 无法杀死线程
				process = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static int getUnixPID(Process process) throws Exception {
		// System.out.println(process.getClass().getName());
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			Class cl = process.getClass();
			Field field = cl.getDeclaredField("pid");
			field.setAccessible(true);
			Object pidObject = field.get(process);
			return (Integer) pidObject;
		} else {
			throw new IllegalArgumentException("Needs to be a UNIXProcess");
		}
	}

	/** 添加引号，一般是文件路径需要添加引号 **/
	public static String addQuot(String pathName) {
		return "\"" + pathName + "\"";
	}
}

class StreamGobbler extends Thread {
	Logger logger = Logger.getLogger(StreamGobbler.class);
	InputStream is;
	OutputStream os;
	List<String> lsInfo;
	boolean isFinished = false;

	StreamGobbler(InputStream is, OutputStream outputStream) {
		this.is = is;
		this.os = outputStream;
	}

	public void setLsInfo(List<String> lsInfo) {
		this.lsInfo = lsInfo;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void run() {
		if (os == null) {
			isFinished = true;
			return;
		}
		isFinished = false;
		try {
			IOUtils.copy(is, os);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		isFinished = true;
	}
}

class ProgressData {
	public String strcmdInfo;
	/**
	 * true : info false : error
	 */
	public boolean info;
}
