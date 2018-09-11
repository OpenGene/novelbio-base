package com.novelbio.base.fileOperate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;

public class PathDecoder {
	String path;
	List<String> lsPaths = new ArrayList<>();
	
	boolean isStartWithSep = false;
	
	boolean isEndWithSep = false;
	
	public PathDecoder(String path) {
		setPaths(path);
		copePath();
	}
	
	public void setPaths(String path) {
		this.path = path;
		path = path.replace("\\", "/");
		if (path.startsWith("/")) {
			isStartWithSep = true;
		}
		char[]	cpath = path.toCharArray();

		StringBuilder stringBuilder = new StringBuilder();
		boolean isSep = isStartWithSep;
		for (char unit : cpath) {
			if (unit == '/') {
				if (!isSep) {
					isSep = true;
					lsPaths.add(stringBuilder.toString());
					stringBuilder = new StringBuilder();
				}
				continue;
			}
			isSep = false;
			stringBuilder.append(unit);
		}
		if (isSep && !lsPaths.isEmpty()) {
			isEndWithSep = true;
		} else {
			lsPaths.add(stringBuilder.toString());
		}
	}
	
	public void copePath() {
		LinkedList<String> linkedPath = new LinkedList<>();
		for (int i = 0; i < lsPaths.size(); i++) {
			String path = lsPaths.get(i);
			if (path.equals(".") && (i!=lsPaths.size()-1 || isEndWithSep)) {
				continue;
			} else if (path.equals("..") && (i!=lsPaths.size()-1 || isEndWithSep)) {
				try {
					linkedPath.removeLast();
				} catch (NoSuchElementException e) {
					throw new ExceptionNbcFile("unsupported path " + path);
				}
			} else {
				linkedPath.add(path);
			}
		}
		lsPaths = new ArrayList<>(linkedPath);
	}
	
	public String getResult() {
		String result = ArrayOperate.cmbString(lsPaths, "/");
		if (isStartWithSep) {
			result = "/"+result;
		}
		if (isEndWithSep && !StringOperate.isRealNull(result)) {
			result = result + "/";
		}
		return result;
	}
}
