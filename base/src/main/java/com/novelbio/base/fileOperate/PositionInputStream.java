package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.io.InputStream;

/**
 * 将HDFS的流包装成Samtools识别的Seekable流
 * @author zong0jie
 *
 */
public class PositionInputStream extends InputStream {
	private long pos = 0;
	private volatile InputStream in;

	public PositionInputStream(InputStream in) {
		this.in = in;
		this.pos = 0;
	}

	public int read() throws IOException {
		int info = in.read();
		this.pos++;
		return info;
	}

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		int readLen = in.read(b, off, len);
		pos	+= readLen;
		return readLen;
	}

	public long skip(long n) throws IOException {
		long skipLen = in.skip(n);
		pos += skipLen;
		return skipLen;
	}

	public int available() throws IOException {
		return in.available();
	}

	public void close() throws IOException {
		in.close();
	}

	public boolean markSupported() {
		return in.markSupported();
	}
	
	/** 获得读取到的位置 */
	public long getPos() {
	    return pos;
    }
}
