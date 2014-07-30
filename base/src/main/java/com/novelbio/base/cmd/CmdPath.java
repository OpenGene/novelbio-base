package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 输入cmdlist，将其整理为相应的cmd string array<br>
 * 并且移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 *
 */
public class CmdPath {
	private static final Logger logger = Logger.getLogger(CmdPath.class);
	List<String> lsCmd;
	
	boolean isRedirectInToTmp = false;
	boolean isRedirectOutToTmp = false;

	Set<String> setInput = new HashSet<>();
	Set<String> setOutput = new HashSet<>();
	
	/**
	 * 输出文件夹路径
	 * key: 文件名
	 * value: 临时文件名
	 */
	Map<String, String> mapName2TmpName = new HashMap<>();
	/** 输入文件夹对应的输出文件夹，可以将输入文件夹里的文件直接复制到输出文件夹中 */
	Map<String, String> mapPath2TmpPath = new HashMap<>();
	
	/** 截获标准输出流的输出文件名 */
	String saveFilePath = null;
	/** 截获标准错误流的错误文件名 */
	String saveErrPath = null;
	
	/** 如果为null就不加入 */
	public void addCmdParam(String param) {
		if (!StringOperate.isRealNull(param)) {
			lsCmd.add(param);
		}
	}
	
	/** 是否将输入文件拷贝到临时文件夹，默认为false */
	public void setRedirectInToTmp(boolean isRedirectInToTmp) {
		this.isRedirectInToTmp = isRedirectInToTmp;
	}
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹，默认为false */
	public void setRedirectOutToTmp(boolean isRedirectOutToTmp) {
		this.isRedirectOutToTmp = isRedirectOutToTmp;
	}
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	public void addCmdParamInput(String input, boolean isAddToLsCmd) {
		if (StringOperate.isRealNull(input)) {
			throw new ExceptionCmd("input is null");
		}
		setInput.add(input);
		if (isAddToLsCmd) {
			lsCmd.add(input);
		}
	}
	
	public void setLsCmd(List<String> lsCmd) {
		this.lsCmd = lsCmd;
	}
	
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数
	 * @param isAddToLsCmd 是否加入参数list<br>
	 * true: 作为一个参数加入lscmd<br>
	 * false: 不加入lsCmd，仅仅标记一下
	 * 
	 * @param output
	 */
	public void addCmdParamOutput(String output, boolean isAddToLsCmd) {
		if (StringOperate.isRealNull(output)) {
			throw new ExceptionCmd("Output is null");
		}
		setOutput.add(output);
		if (isAddToLsCmd) {
			lsCmd.add(output);
		}
	}
	
	/** 如果param为null则返回 */
	public void addCmdParam(List<String> lsCmd) {
		if (lsCmd == null) {
			return;
		}
		String[] param = lsCmd.toArray(new String[0]);
		addCmdParam(param);
	}
	/** 如果param为null则返回 */
	public void addCmdParam(String[] param) {
		if (param == null) {
			return;
		}
		for (String string : param) {
			if (StringOperate.isRealNull(string)) {
				throw new ExceptionCmd("param contains null");
			}
			lsCmd.add(string);
		}
	}
	
	public String getSaveErrPath() {
		return saveErrPath;
	}
	public String getSaveFilePath() {
		return saveFilePath;
	}

	/** 返回执行的具体cmd命令，不会将文件路径删除，仅给相对路径 */
	public String getCmdExeStr() {
		return getCmdExeStrModify();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String getCmdExeStrModify() {
		return getCmdModify(lsCmd);
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String getCmdExeStrReal() {
		return ArrayOperate.cmbString(lsCmd.toArray(new String[0]), " ");
	}
	
	public static String getCmdModify(List<String> lsCmd) {
		String[] cmdArray = lsCmd.toArray(new String[0]);
		return getCmdModify(cmdArray);
	}
	
	public static String getCmdModify(String[] cmdArray) {
		StringBuilder strBuilder = new StringBuilder();
		for (String cmdTmp : cmdArray) {
			String[] subcmd = cmdTmp.split("=");
			strBuilder.append(" ");
			strBuilder.append(FileOperate.getFileName(subcmd[0]));
			for (int i = 1; i < subcmd.length; i++) {
				strBuilder.append("=");
				strBuilder.append(FileOperate.getFileName(subcmd[i]));
			}
		}
		return strBuilder.toString().trim();
	}
	/** 在cmd运行前，将输入文件拷贝到临时文件夹下 */
	public void copyFileIn() {
		generateTmPath();
		if (!isRedirectInToTmp) return;
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			FileOperate.copyFileFolder(inFile, inTmpName, true);
		}
	}
	
	/** 必须先调用{@link #copyFileIn()}，
	 * 等运行cmd结束后还需要调用{@link #copyFileOut()} 来完成运行 */
	public String[] getRunCmd() {
		return generateRunCmd();
	}
	
	private void generateTmPath() {
		Set<String> setPath = new HashSet<>();
		Set<String> setFileNameAll = new HashSet<>();
		mapPath2TmpPath.clear();
		mapName2TmpName.clear();
		
		if (isRedirectInToTmp) {
			for (String inFileName : setInput) {
				String inPath = FileOperate.getParentPathNameWithSep(inFileName);
				setPath.add(inPath);
			}
			setFileNameAll.addAll(setInput);
		}
		if (isRedirectOutToTmp) {
			for (String outFileName : setOutput) {
				String outPath = FileOperate.getParentPathNameWithSep(outFileName);
				setPath.add(outPath);
			}
			setFileNameAll.addAll(setOutput);
		}
				
		int i  = 0;//防止产生同名文件夹的措施
		for (String path : setPath) {
			i++;
			String tmpPath = PathDetail.getTmpPathRandomWithSep(FileOperate.getFileName(path) + i);
			mapPath2TmpPath.put(path, tmpPath);
		}
		
		for (String filePathName : setFileNameAll) {
			String tmpPath = mapPath2TmpPath.get(FileOperate.getParentPathNameWithSep(filePathName));
			String fileName = FileOperate.getFileName(filePathName);
			tmpPath = tmpPath + fileName;
			/** 将已有的输出文件夹在临时文件夹中创建好 */
			if (filePathName.endsWith("/") || filePathName.endsWith("\\")) {
				tmpPath = FileOperate.addSep(tmpPath);
			}
			if ( FileOperate.isFileDirectory(filePathName)) {
				FileOperate.createFolders(tmpPath);
			}
			mapName2TmpName.put(filePathName, tmpPath);
		}
	}
	
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @return
	 */
	private String[] generateRunCmd() {
		List<String> lsReal = new ArrayList<>();
		boolean stdOut = false;
		boolean errOut = false;
		for (String tmpCmd : lsCmd) {
			if (tmpCmd.equals(">")) {
				stdOut = true;
				continue;
			} else if (tmpCmd.equals("2>")) {
				errOut = true;
				continue;
			}
			
			if ((isRedirectInToTmp && setInput.contains(tmpCmd))
					|| 
					(!errOut && !stdOut && isRedirectOutToTmp && setOutput.contains(tmpCmd))) {
				tmpCmd = mapName2TmpName.get(tmpCmd);
			}
			
			if (stdOut) {
				saveFilePath = tmpCmd;
				stdOut = false;
				continue;
			} else if (errOut) {
				saveErrPath = tmpCmd;
				errOut = false;
				continue;
			}
			
			lsReal.add(convertToLocalCmd(tmpCmd));
		}
		String[] realCmd = lsReal.toArray(new String[0]);
		return realCmd;
	}
	
	/** 将cmd中的hdfs路径改为本地路径 */
	private static String convertToLocalCmd(String tmpCmd) {
		String[] subcmd = tmpCmd.split("=");
		subcmd[0] = FileHadoop.convertToLocalPath(subcmd[0]);
		for (int i = 1; i < subcmd.length; i++) {
			subcmd[i] = FileHadoop.convertToLocalPath(subcmd[i]);
		}
		String result = subcmd[0];
		for (int i = 1; i < subcmd.length; i++) {
			result = result + "=" + subcmd[i];
		}
		return result;
	}
		
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中
	 * notmove是不需要移动的文件名
	 * @param tmpPath
	 * @param resultPath
	 * @param notMove
	 * @param isDelFile 如果出错是否删除原来的文件
	 */
	public void copyFileOut() {
		for (String outPath : mapPath2TmpPath.keySet()) {
			String outTmpPath = mapPath2TmpPath.get(outPath);
			
			List<String> lsFilesFinish = FileOperate.getFoldFileNameLs(outTmpPath, "*", "*");
			for (String filePath : lsFilesFinish) {
				String  filePathResult = filePath.replaceFirst(outTmpPath, outPath);
				if (setInput.contains(filePathResult) && FileOperate.isFileExistAndBigThanSize(filePathResult, 0)) {
					continue;
				}
				boolean isSucess = FileOperate.copyFileFolder(filePath, filePathResult, true);
				if (!isSucess) {
					logger.error("cannot copy " + filePath + " to " + filePathResult);
					throw new ExceptionCmd("cannot copy " + filePath + " to " + filePathResult);
				}
			}
		}
		
	}
	
	public void deleteTmpFile() {
		for (String tmpPath : mapPath2TmpPath.values()) {
			FileOperate.DeleteFileFolder(tmpPath);
		}
	}
	
	public void clearLsCmd() {
		lsCmd.clear();
		setInput.clear();
		setOutput.clear();
		mapName2TmpName.clear();
		mapPath2TmpPath.clear();
	}
}
