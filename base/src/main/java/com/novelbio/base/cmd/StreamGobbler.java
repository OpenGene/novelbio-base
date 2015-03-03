package com.novelbio.base.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class StreamGobbler extends Thread {
	private static final Logger logger = Logger.getLogger(StreamGobbler.class);
	/** 每2000ms刷新一次txt文本，这是因为写入错误行会很慢，刷新就可以做到及时看结果 */
	private static final int timeTxtFlush = 5000;
	
	/** 运行进程的pid */
	IntProcess process;
	InputStream is;
	OutputStream os;
	LinkedList<String> lsInfo;
	/** lsInfo中最多存储500条信息 */
	int lineNum = 500;
	
	boolean isFinished = false;
	boolean getInputStream = false;

	/** 如果将输出信息写入lsInfo中，是否还将这些信息打印到控制台 */
	boolean isSysout = false;

	//==================定时刷新，以表示程序正在运行中============================	
	/** 流中是否有信息，没信息就不刷新 */
	boolean isStartWrite = false;
	
	/** 是否按照写入txt的格式来写入流 */
	boolean isJustDisplay = false;

	
	Timer timerFlush;
	
	StreamGobbler(InputStream is, IntProcess process) {
		this.is = is;
		this.process = process;
	}
	/**
	 *  指定一个out流，cmd的输出流就会定向到该流中<br>
	 * 该方法和{@link #setGetInputStream(boolean)} 冲突
	 * @param os 输出流
	 * @param isJustDisplay 是否仅用来展示。展示用的会以txt的格式来写输出流，并且定时刷新
	 * true则表示会从输入流中逐行读取，然后写入 os，并且会定时刷新os，以保证能够及时看到输出文件中的内容
	 */
	public void setOutputStream(OutputStream os, boolean isJustDisplay) {
		this.os = os;
		this.isJustDisplay = isJustDisplay;
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
		isFinished = false;
		if (!getInputStream) {
			if (os == null) {
				exhaustInStream(is);
			} else {
				if (isJustDisplay) {
					initialTxtFlush();
					
					writeToTxt(is, os);
					
					finishFlush();
					
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
					if (isSysout) {
						logger.info(line);
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/** 定时刷新输出的stdout和stderr文本 */
	private void initialTxtFlush() {
		timerFlush = new Timer();
		timerFlush.schedule(new TimerTask() {
			public void run() {
				if (!isStartWrite) return;
				
				synchronized (this) {
					try { os.flush(); } catch (Exception e) {e.printStackTrace(); }				
				}
			}
		}, timeTxtFlush, timeTxtFlush);
	}
	
	/** 关闭这两个刷新任务 */
	private void finishFlush() {
		if (timerFlush != null) {
			timerFlush.cancel();
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
				if (isSysout) {
					logger.info(line);
				}
				//说明流中有东西
				if (!isStartWrite) isStartWrite = true;
				
				synchronized (this) {
					outputStream.write((line + TxtReadandWrite.ENTER_LINUX).getBytes());
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/** 往stdErr或stdOut中输出结束信息，表示程序运行完毕了
	 * 只有当{@link #setOutputStream(OutputStream, boolean, boolean)}的后两项
	 * isWriteToTxt和isWriteTIPS都设定为True的时候才会写入信息<br>
	 * <b>并且写完后就会关闭流</b>
	 *  */
	protected void writeFinishToTxt(String finishInfo) {
		
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
	public synchronized void close() {
		//不用关闭输入流，因为process会自动关闭该流
		try {
			os.flush();
			os.close();
		} catch (Exception e) { }
	}
	/** 
	 * 关闭输出流，同时
	 * 往stdErr或stdOut中输出结束信息，表示程序运行完毕<p>
	 * 只有当{@link #setOutputStream(OutputStream, boolean, boolean)}的后两项
	 * isWriteToTxt和isWriteTIPS都设定为True的时候才会写入信息<br>
	 * <b>并且写完后就会关闭流</b>
	 * <p>
	 * 如果本输出流没有东西，就不会把文字写进去
	 *  */
	public synchronized void close(String finishInfo) {
		if (isJustDisplay) {
			try {
				if (isStartWrite) {
					os.write((DateUtil.getNowTimeStr() + " " + finishInfo).getBytes());
				}
				os.flush();
				os.close();
				process = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}




class ProgressData {
	public String strcmdInfo;
	/**
	 * true : info false : error
	 */
	public boolean info;
}
