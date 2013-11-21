package com.novelbio.base;

import com.novelbio.base.fileOperate.FileOperate;

/** 根据关键词自动创建文件夹的类 */
public class FoldeCreate {
	/** 如果输入的最后一个文件夹名字中包含这个名字，则不创建，直接返回该文件夹<br>
	 * 否则创建一个文件夹<p>
	 *  譬如 pathPrefix = "/your/path/1、GOanalysis/go"<br>
	 *  foldName = "GOanalysis";<br>
	 *  则返回 "/your/path/1、GOanalysis/go"<p>
	 *  
	 *  pathPrefix = "/your/path/Novelbio/go"<br>
	 *  foldName = "GOanalysis";<br>
	 *  则返回 "/your/path/Novelbio/GOanalysis/go"<br>
	 *  @param pathPrefix
	 *  @param foldName 文件名，不能有"/"这种符号
	 *  */
	public static String createAndInFold(String pathPrefix, String foldName) {
		String lastParentFoldName = "";
		String lastParentFoldPath = "";
		String prefix = "";
		if (pathPrefix.endsWith("\\")|| pathPrefix.endsWith("/")) {
			lastParentFoldPath = pathPrefix;
		} else {
			lastParentFoldPath = FileOperate.getParentPathName(pathPrefix);
			prefix = FileOperate.getFileName(pathPrefix);
		}
		lastParentFoldName = FileOperate.getFileName(lastParentFoldPath);

		if (lastParentFoldName.contains(foldName)) {
			return pathPrefix;
		}
		String pathFold = FileOperate.addSep(lastParentFoldPath) + foldName + FileOperate.getSepPath();
		FileOperate.createFolders(pathFold);
		return pathFold + prefix;
	}
	/**
	 * 同{@link #createAndInFold(String, String)}，但是不创建文件夹
	 * @param pathPrefix
	 * @param foldName
	 * @return
	 */
	public static String getInFold(String pathPrefix, String foldName) {
		String lastParentFoldName = "";
		String lastParentFoldPath = "";
		String prefix = "";
		if (pathPrefix.endsWith("\\")|| pathPrefix.endsWith("/")) {
			lastParentFoldPath = pathPrefix;
		} else {
			lastParentFoldPath = FileOperate.getParentPathName(pathPrefix);
			prefix = FileOperate.getFileName(pathPrefix);
		}
		lastParentFoldName = FileOperate.getFileName(lastParentFoldPath);

		if (lastParentFoldName.contains(foldName)) {
			return pathPrefix;
		}
		String pathFold = FileOperate.addSep(lastParentFoldPath) + foldName + FileOperate.getSepPath();
		return pathFold + prefix;
	}
	
}
