package com.novelbio.base.cmd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.novelbio.base.fileOperate.ExceptionFileInputError;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;

/**
 * 需要从标准输入流写入的文件
 * @author zong0jie
 *
 */
public class StreamIn extends RunProcess<Integer> {
	File inputFile;
	protected InputStream inStream;
	/** 输入cmd的流 */
	protected OutputStream processInStream;
	
	public void setInputFile(String inputFile) {
		this.inputFile = FileOperate.getFile(inputFile);
	}
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}
	public String getInputFile() {
		if (inputFile != null) {
			return inputFile.getAbsolutePath();
		}
		return null;
	}
	public void setInStream(InputStream inStream) {
		this.inStream = inStream;
	}
	
	public void setProcessInStream(OutputStream processInStream) {
		this.processInStream = processInStream;
	}

	@Override
	protected void running() {
		try {
			inStream = FileOperate.getInputStream(inputFile);
		} catch (IOException e) {
			throw new ExceptionFileInputError("input file may not exist: " + inputFile, e);
		}
		try {
			copyLarge(inStream, processInStream);
		} catch (Exception e) {
			throw new ExceptionCmd("cmdError", e);
		} finally {
			try {
				inStream.close();
				processInStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private final int EOF = -1;
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
	private long copyLarge(final InputStream input, final OutputStream output) {
		try {
			byte[] buffer = new byte[1024 * 4];
			long count = 0;
			int n = 0;
			while (EOF != (n = input.read(buffer))) {
				if (flagStop) {
					break;
				}
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		} catch (Exception e) {
			throw new RuntimeException("copy info error", e);
		}
	}
}
