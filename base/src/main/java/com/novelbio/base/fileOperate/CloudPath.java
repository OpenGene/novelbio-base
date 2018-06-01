/**
 *
 * @author novelbio fans.fan
 * @date 2018年5月31日
 */
package com.novelbio.base.fileOperate;

import java.nio.file.FileSystem;

import com.novelbio.jsr203.objstorage.ObjPath;

/**
 *
 * @author novelbio fans.fan
 */
public class CloudPath extends ObjPath {
	
	private boolean directory;
	private long fileSize;
	private long createTime;

	/**
	 * @param bfs
	 * @param path
	 */
	public CloudPath(FileSystem bfs, byte[] path) {
		super(bfs, path);
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean isDirectory) {
		this.directory = isDirectory;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
}
