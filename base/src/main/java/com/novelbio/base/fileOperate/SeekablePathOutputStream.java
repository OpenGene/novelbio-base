package com.novelbio.base.fileOperate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 将HDFS的流包装成Samtools识别的Seekable流
 * @author zong0jie
 *
 */
public class SeekablePathOutputStream extends OutputStream {
	
	private Path path;
	private SeekableByteChannel seekableByteChannel;
	private long fileLength;
	
	long posMark = 0;
	
	public SeekablePathOutputStream(Path path) {
		this.path = path;
		try {
			this.seekableByteChannel= Files.newByteChannel(path, StandardOpenOption.WRITE);
			this.fileLength = seekableByteChannel.size();
		} catch (IOException e) {
			throw new ExceptionSeekablePathStream("cannot get file ", e);
        }
	}
	public void seekToEnd() throws IOException {
		seekableByteChannel.position(fileLength);
	}
	public long length() {
		return fileLength;
	}

	public long position() throws IOException {
		return seekableByteChannel.position();
	}

	public void seek(long position) throws IOException {
		seekableByteChannel.position(position);
	}
	
	/**
	 * TODO 尚未测试
	 */
	//TODO 尚未测试
	@Override
	public void write(int b) throws IOException {
		seekableByteChannel.write(ByteBuffer.wrap(new byte[]{(byte)b}));		
	}
	public void write(byte[] buffer, int offset, int length) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, length);
		seekableByteChannel.write(byteBuffer);
	}
	
	public void close() throws IOException {
		seekableByteChannel.close();
	}

	public boolean eof() throws IOException {
		return fileLength == seekableByteChannel.position();
	}
	
	/**
	 * 在sam-1.87版本中，仅用来显示报错信息
	 */
	public String getSource() {
		return path.toString();
	}
	
	public long skip(long n) throws IOException {
		long initPos = position();
		seekableByteChannel.position(initPos + n);
		return position() - initPos;
	}
	
	public static class ExceptionSeekablePathStream extends RuntimeException {
		private static final long serialVersionUID = 8873732661819768282L;

		public ExceptionSeekablePathStream(String info) {
			super(info);
		}
		
		public ExceptionSeekablePathStream(String info, Throwable t) {
			super(info, t);
		}
	}


	
}
