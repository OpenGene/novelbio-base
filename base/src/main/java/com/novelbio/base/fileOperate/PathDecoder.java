package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 专门用来解析path的类，仅仅做字符串处理，不会接触实际文件<br>
 * 用在{@link FileOperate}中
 * @author zongjie
 *
 */
public class PathDecoder {
	List<String> lsPaths = new ArrayList<>();
	/** 是否为 hdfs:/ 开头 */
	String symbol;
	/** 是否为 hdfs://domain/ 这种 */
	String domain;
	
	boolean isStartWithSep = false;
	
	boolean isEndWithSep = false;
	
	boolean isRoot;
	
	public PathDecoder(String path) {
		setPaths(path);
		copePath();
	}
	
	public PathDecoder(char[] path) {
		setPaths(path);
		copePath();
	}
	
	public String getSymbol() {
		return symbol;
	}
	public String getDomain() {
		return domain;
	}
	public void setPaths(String path) {
		path = path.replace("\\", "/");
		char[]	cpath = path.toCharArray();
		setPaths(cpath);
	}
	public void setPaths(char[] cpath) {
		if (cpath.length > 0 && cpath[0] == '/') {
			isStartWithSep = true;
		}
		StringBuilder stringBuilder = new StringBuilder();
		
		boolean isFirstPathUnit = true;
		boolean isStart = true;
		boolean isSep = false;
		boolean isDomain = false;
		boolean previousIsSep = false; //前一个字节是否是/
		int sepNum = 0;
		
		for (int i = 0; i < cpath.length; i++) {
			char unit = cpath[i];
			if (isStart) {
				if (unit == '/') {
					continue;
				}
				isStart = false;
			}
			
			if (isFirstPathUnit && unit == ':' && cpath.length > i+1 && cpath[i+1] == '/' && sepNum == 0) {
				symbol = stringBuilder.toString();
				int m = i+1;
				stringBuilder = new StringBuilder();
				if (cpath.length > i+2 && cpath[i+2] == '/') {
					isDomain = true;
					m = i+2;
					// file:///的情况
					if (cpath.length > i+3 && cpath[i+3] == '/') {
						domain = "";
						isDomain = false;
						m = i+3;
					}
				}
				i = m;
				continue;
			}
			
			if (unit == '/') {
				isFirstPathUnit = false;
				if (!isSep || previousIsSep) {
					isSep = true;
					if (isDomain) {
						domain = stringBuilder.toString();
						isDomain = false;
					} else {
						lsPaths.add(stringBuilder.toString());
					}
					stringBuilder = new StringBuilder();
				}
				previousIsSep = true;
			} else {
				isSep = false;
				stringBuilder.append(unit);
				previousIsSep = false;
			}
		}
		if (isSep && !lsPaths.isEmpty()) {
			isEndWithSep = true;
		} else {
			lsPaths.add(stringBuilder.toString());
		}
	}
	
	public String getName() {
		if (lsPaths.isEmpty()) {
			return "";
		}
		return lsPaths.get(lsPaths.size()-1);
	}
	/**
	 * 给定路径名，返回其最近一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/
	 * <br>
	 * 给定/wef/tesw/tre/还是返回/wef/tesw/tre/
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String getPathWithSep() {
		return isEndWithSep ? getAbsPathWithSep() : getParentPathNameWithSep();
	}
	public String getParentPathNameWithSep() {
		StringBuilder sBuilder = getHeadDomainWithSep();
		if (lsPaths.size() <= 1) {
			return sBuilder.toString();
		}
		
		for (int i = 0; i < lsPaths.size()-1; i++) {
			sBuilder.append(lsPaths.get(i)).append("/");
		}
		return sBuilder.toString();
	}

	public void copePath() {
		LinkedList<String> linkedPath = new LinkedList<>();
		int startNum = 0;
		for (int i = startNum; i < lsPaths.size(); i++) {
			String path = lsPaths.get(i);
			if (path.equals(".") && (i!=lsPaths.size()-1 || isEndWithSep)) {
				continue;
			} else if (path.equals("..")) {
				if (linkedPath.isEmpty()) {
					if (isAbsPath()) {
						continue;
					}
					throw new ExceptionNbcFile("unsupported path " + path);
				}
				linkedPath.removeLast();
			} else {
				linkedPath.add(path);
			}
		}
		lsPaths = new ArrayList<>(linkedPath);
	}
	
	private boolean isAbsPath() {
		return isStartWithSep || symbol != null;
	}
	
	/**
	 * 如果输入的path含有"/"
	 * 则返回也含有"/"
	 * @return
	 */
	public String getAbsPathWithSep() {
		StringBuilder sBuilder = getAbsPathWithoutEndSepSbuilder();
		if (isEndWithSep && !lsPaths.isEmpty() && sBuilder.length() > 1) {
			sBuilder.append("/");
		}
		return sBuilder.toString();
	}
	
	/**
	 * 除非是"/"这种，否则返回尾部不带"/"
	 * @return
	 */
	public String getAbsPathWithoutEndSep() {
		return getAbsPathWithoutEndSepSbuilder().toString();
	}
	
	/**
	 * 除非是"/"这种，否则返回尾部不带"/"
	 * @return
	 */
	private StringBuilder getAbsPathWithoutEndSepSbuilder() {
		StringBuilder stringBuilder = getHeadDomainWithSep();
		if (lsPaths.isEmpty()) {
			return stringBuilder;
		}
		stringBuilder.append(lsPaths.get(0));
		for (int i = 1; i < lsPaths.size(); i++) {
			stringBuilder.append("/").append(lsPaths.get(i));
		}		
		return stringBuilder;
	}
	
	/** 如果是类似 "/media/nbfs"这种，则返回 "/"
	 * 否则返回 "hdfs:/" 或 "hdfs://domain/"
	 * @return
	 */
	private StringBuilder getHeadDomainWithSep() {
		StringBuilder stringBuilder = new StringBuilder();
		if (isStartWithSep && symbol == null) {
			stringBuilder.append("/");
		}
		
		if (symbol != null) {
			stringBuilder.append(symbol + ":/");
			if (domain != null) {
				stringBuilder.append("/");
				stringBuilder.append(domain);
				stringBuilder.append("/");
			}
		}
		return stringBuilder;
	}
	
	public static boolean isAbsPath(String path) {
		if (path.startsWith("/")) {
			return true;
		}
		char[] cs = path.toCharArray();
		StringBuilder stringBuilder = new StringBuilder();
		for (char c : cs) {
			if (c == '/' || c == '\\') {
				stringBuilder.append("/");
				break;
			}
			stringBuilder.append(c);
		}
		String head = stringBuilder.toString();
		return head.endsWith(":/") && head.length() > 2;
	}
}
