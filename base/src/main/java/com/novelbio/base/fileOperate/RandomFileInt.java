package com.novelbio.base.fileOperate;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public interface RandomFileInt {
	public void seek(long site) throws IOException;
	
	int read(byte[] b, int off, int len) throws IOException;

	public int read(byte[] byteinfo) throws IOException;
	
	public void close() throws IOException;
	public void readFully(byte[] signatureBytes) throws IOException;
	public void readFully(byte[] mBuffer, int i, int readLength) throws IOException;
	public long length() throws IOException;
	public int read() throws IOException; 
	public int skipBytes(int n) throws IOException;
	long getFilePointer() throws IOException; 
	String readLine() throws IOException; 
	
	public static class RandomFileFactory {
		/** return corresponding object by judge the input fileName is FileHadoop or local file */
		public static RandomFileInt createInstance(String fileName) {
			return new RandomFileSeekable(FileOperate.getPath(fileName));
		}
		
		/** return corresponding object by judge the input fileName is FileHadoop or local file */
		public static RandomFileInt createInstance(File fileName) {
			return new RandomFileSeekable(FileOperate.getPath(fileName));
		}
		
		public static RandomFileInt createInstance(Path file) {
			return new RandomFileSeekable(file);
		}
	}
}

class RandomFileLocal extends RandomAccessFile implements RandomFileInt {
	public RandomFileLocal(String file) throws FileNotFoundException {
		super(file, "r");
	}
	public RandomFileLocal(File file) throws FileNotFoundException {
		super(file, "r");
	}
}

class RandomFileSeekable implements RandomFileInt {
	SeekablePathInputStream fsDataInputStream;
	Path path;
	public RandomFileSeekable(String file) {
		this.path = FileOperate.getPath(file);
		this.fsDataInputStream = FileOperate.getSeekablePathStream(path);
	}
	
	public RandomFileSeekable(Path path) {
		this.path = path;
		this.fsDataInputStream = FileOperate.getSeekablePathStream(path);
	}
	
	@Override
	public void seek(long site) throws IOException {
		fsDataInputStream.seek(site);
	}

	@Override
	public int read(byte[] byteinfo) throws IOException {
		return fsDataInputStream.read(byteinfo);
	}

	@Override
	public void close() throws IOException {
		fsDataInputStream.close();
	}
	
    /**
     * Reads <code>b.length</code> bytes from this file into the byte
     * array, starting at the current file pointer. This method reads
     * repeatedly from the file until the requested number of bytes are
     * read. This method blocks until the requested number of bytes are
     * read, the end of the stream is detected, or an exception is thrown.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this file reaches the end before reading
     *               all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final void readFully(byte b[]) throws IOException {
    	fsDataInputStream.readFully(b);
    }

    /**
     * Reads exactly <code>len</code> bytes from this file into the byte
     * array, starting at the current file pointer. This method reads
     * repeatedly from the file until the requested number of bytes are
     * read. This method blocks until the requested number of bytes are
     * read, the end of the stream is detected, or an exception is thrown.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this file reaches the end before reading
     *               all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
    	fsDataInputStream.readFully(b, off, len);
    }

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return fsDataInputStream.read(b, off, len);
	}

	@Override
	public long length() throws IOException {
		return fsDataInputStream.length();
	}

	@Override
	public int read() throws IOException {
		return fsDataInputStream.read();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return (int) fsDataInputStream.skip(n);
	}

	@Override
	public long getFilePointer() throws IOException {
		return fsDataInputStream.position();
	}

	@Override
	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return fsDataInputStream.readLine();
	}
    
}

