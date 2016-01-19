package com.novelbio.base.fileOperate;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 将HDFS的流包装成Samtools识别的Seekable流
 * @author zong0jie
 *
 */
public class SeekablePathInputStream extends InputStream {
	
	private Path path;
	private SeekableByteChannel seekableByteChannel;
	private long fileLength;
	
	long posMark = 0;
	
	public SeekablePathInputStream(Path path) {
		this.path = path;
		try {
			this.seekableByteChannel= Files.newByteChannel(path);
			this.fileLength = seekableByteChannel.size();
		} catch (IOException e) {
			throw new ExceptionSeekablePathStream("cannot get file ", e);
        }
	}
	
	public long length() {
		return fileLength;
	}

	public boolean eof() throws IOException {
		return fileLength == seekableByteChannel.position();
	}

	public void seek(final long position) throws IOException {
		seekableByteChannel.position(position);
	}

	public long position() throws IOException {
		return seekableByteChannel.position();
	}

    @Override
    public long skip(long n) throws IOException {
        long initPos = position();
        seekableByteChannel.position(initPos + n);
        return position() - initPos;
    }
    
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        if (length < 0) {
            throw new IndexOutOfBoundsException();
        }
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, offset, length);
        int n = 0;
        while (n < length) {
            final int count = seekableByteChannel.read(byteBuffer);
            if (count < 0) {
              if (n > 0) {
                return n;
              } else {
                return count;
              }
            }
            n += count;
        }
        return n;
    }
    
    /**
     * Read enough bytes to fill the input buffer.
     * @param b
     * @throws EOFException If EOF is reached before buffer is filled
     */
	public void readFully(byte b[], int offset, final int length) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(b, offset, length);		
		int n = 0;
		while (n < length) {
			int count = seekableByteChannel.read(byteBuffer);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}
	
    /**
     * Read enough bytes to fill the input buffer.
     * @param b
     * @throws EOFException If EOF is reached before buffer is filled
     */
	public void readFully(byte b[]) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(b);
		int len = b.length;
		
		if (len < 0) throw new IndexOutOfBoundsException();

		int n = 0;
		while (n < len) {
			int count = seekableByteChannel.read(byteBuffer);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}
	
	public int read() throws IOException {
		if (seekableByteChannel.position() >= fileLength) {
			return -1;
		}
		ByteBuffer buffer = ByteBuffer.allocate(1);
		seekableByteChannel.read(buffer);
		return buffer.array()[0] & 0xff;
	}

	public String readLine() throws IOException {
		StringBuffer input = new StringBuffer();
		int c = -1;
		boolean eol = false;

		while (!eol) {
			switch (c = read()) {
			case -1:
			case '\n':
				eol = true;
				break;
			case '\r':
				eol = true;
				long cur = position();
				if ((read()) != '\n') {
					seek(cur);
				}
				break;
			default:
				input.append((char) c);
				break;
			}
		}

		if ((c == -1) && (input.length() == 0)) {
			return null;
		}
		return input.toString();
	}
	
	@Override
	public void close() throws IOException {
		seekableByteChannel.close();
	}
	
	/**
	 * 在sam-1.87版本中，仅用来显示报错信息
	 */
	public String getSource() {
		return path.toString();
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
