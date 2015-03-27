package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.fs.FSDataOutputStream;

/**
 * 就是重写了flush那个部分
 * @author novelbio
 *
 */
public class OutputStreamHdfs extends OutputStream {
	FSDataOutputStream osHdfs;
	
	public OutputStreamHdfs(FSDataOutputStream osHdfs) {
		this.osHdfs = osHdfs;
	}
	
    public void write(int b) throws IOException {
    	osHdfs.write(b);
    }

    public void write(byte b[]) throws IOException {
    	osHdfs.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
    	osHdfs.write(b, off, len);
    }

    public void flush() throws IOException {
    	//TODO MapR
//    	osHdfs.sync();
    	
		//TODO hadoop2
		osHdfs.hflush();
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     * <p>
     * The <code>close</code> method of <code>OutputStream</code> does nothing.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
    	osHdfs.hflush();
    	osHdfs.close();
    }


}

