package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.gui.GUIInfo;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好
 * @author zong0jie
 */
public class CmdOperate extends RunProcess<String> {
	public static void main(String[] args) {
		try {
			test();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void test() throws InterruptedException {
		String cmd = "Rscript /media/winD/fedora/gitNovelbio/Novelbio-Bioinformatics-Analysis-Platform/target/rscript/tmp/TopGO_2012-09-09040524123.R";
		CmdOperate cmdOperate = new CmdOperate(cmd);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		while (cmdOperate.isRunning()) {
			Thread.sleep(100);
		}
		System.out.println("stop");
	}
	private static Logger logger = Logger.getLogger(CmdOperate.class);

	/** 是否将pid加2，如果是写入文本然后sh执行，则需要加上2 */
	boolean shPID = false;
	
	/** 进程 */
	Process process = null;
	/** 待运行的命令 */
	String cmd = "";
	/** 临时文件在文件夹 */
	String scriptFold = "";
	
	GUIInfo guIcmd;
	
	/**
	 * 直接运行，不写入文本
	 * @param cmd
	 */
	public CmdOperate(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}
	/** 
	 * 是否展示GUI，默认不展示
	 */
	public void setDisplayGUI(boolean displayGUI) {
		if (displayGUI) {
			guIcmd = new GUIInfo(this);
		}
		else {
			guIcmd = null;
		}
	}
	/**
	 * 初始化后直接开新线程即可
	 * @param cmd 输入命令
	 * @param cmdWriteInFileName 将命令写入的文本
	 */
	public CmdOperate(String cmd, String cmdWriteInFileName) {
		this.cmd = cmd;
		setCmdFile(cmdWriteInFileName);
	}
	/**
	 * 多行的命令行
	 * @param lsCmd
	 */
	public CmdOperate(ArrayList<String> lsCmd) {
		for (String string : lsCmd) {
			cmd = cmd + string + "\n";
		}
		shPID = false;
	}

	/** 设定需要运行的命令 */
	public void setCmd(String cmd) {
		this.cmd = cmd;
		shPID = false;
	}
	/**
	 * 将cmd写入哪个文本，然后执行，如果初始化输入了cmdWriteInFileName, 就不需要这个了
	 * @param cmd
	 */
	public void setCmdFile(String cmdWriteInFileName) {
		shPID = true;
		logger.info(cmd);
		String cmd1SH = PathDetail.getProjectConfPath() + cmdWriteInFileName.replace("\\", "/") + DateTime.getDateAndRandom() + ".sh";
		TxtReadandWrite txtCmd1 = new TxtReadandWrite(cmd1SH, true);
		txtCmd1.writefile(cmd);
		txtCmd1.close();
		cmd = "sh " + cmd1SH;
	}
	/**
	 * 直接运行cmd，可能会出错 返回两个arraylist-string 第一个是Info 第二个是error
	 * @param fileName
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private void doInBackgroundB() throws Exception {
		try {
			Thread thread = new Thread(guIcmd);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}


		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(cmd);	
        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", guIcmd);            
        // any output?
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", guIcmd);
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
        
		int info = process.waitFor();
		finishAndCloseCmd(info);
	}
	private void finishAndCloseCmd(int info) {
		if (guIcmd != null) {
			if (info == 0) {
				guIcmd.closeWindow();
			}
			else {
				guIcmd.appendTxtInfo("error");
			}
		}
	}

	@Override
	protected void running() {
		logger.info(cmd);
		try {
			doInBackgroundB();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("cmd cannot executed correctly: " + cmd);
		}
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
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	private static int getUnixPID(Process process) throws Exception {
//	    System.out.println(process.getClass().getName());
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
    String type;
    GUIInfo guiCmd;
    StreamGobbler(InputStream is, String type, GUIInfo guicmd) {
        this.is = is;
        this.type = type;
        this.guiCmd = guicmd;
    }
    
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				logger.info(line);
				if (guiCmd != null) {
					guiCmd.appendTxtInfo(line);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}


class ProgressData {
	public String strcmdInfo;
	/**
	 * true : info
	 * false : error
	 */
	public boolean info;
}
