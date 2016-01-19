package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(CmdPath.class);
	static String tmpPath = PathDetail.getTmpPathWithSep();
	
	List<String> lsCmd = new ArrayList<>();
	
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
	
	/** 存储stdout是否为临时文件 */
	boolean isSaveFileTmp = true;
	/** 截获标准输出流的输出文件名 */
	String saveFilePath = null;
	/** 输出文件是否为txt，如果命令中含有 >，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayStd = false;
	
	/** 存储stderr是否为临时文件 */
	boolean isSaveErrTmp = true;
	/** 截获标准错误流的错误文件名 */
	String saveErrPath = null;
	/** 输出错误文件是否为txt，如果命令中含有 2>，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayErr = false;
	
	
	/** 是否已经生成了临时文件夹，生成一次就够了 */
	boolean isGenerateTmpPath = false;
	
	/** 是否将hdfs的路径，改为本地路径
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 */
	boolean isConvertHdfs2Loc = true;
	
	/** 设定复制输入输出文件所到的临时文件夹 */
	public static void setTmpPath(String tmpPath) {
		CmdPath.tmpPath = tmpPath;
	}
	
	/** 如果为null就不加入 */
	public void addCmdParam(String param) {
		if (!StringOperate.isRealNull(param)) {
			lsCmd.add(param);
		}
	}
	
	public void setJustDisplayStd(boolean isJustDisplayStd) {
		this.isJustDisplayStd = isJustDisplayStd;
	}
	public void setJustDisplayErr(boolean isJustDisplayErr) {
		this.isJustDisplayErr = isJustDisplayErr;
	}
	/** 输出文件是否为txt格式的
	 * 如果命令中含有 2>，则认为输出的可能不是txt，为二进制
	 * @return
	 */
	public boolean isJustDisplayErr() {
		return isJustDisplayErr;
	}
	/** 输出的err文件是否为txt格式的
	 * 如果命令中含有 >，则认为输出的可能不是txt，为二进制
	 * @return
	 */
	public boolean isJustDisplayStd() {
		return isJustDisplayStd;
	}
	
	/** 是否将hdfs的路径，改为本地路径，<b>默认为true</b><br>
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr<br>
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 */
	public void setConvertHdfs2Loc(boolean isConvertHdfs2Loc) {
		this.isConvertHdfs2Loc = isConvertHdfs2Loc;
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
	 * @param output 输出文件的哪个参数，如果输入参数类似 "--outPath=/hdfs:/test.fa"，这里填写 "/hdfs:/test.fa"
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
	
	public void setSaveFilePath(String saveFilePath, boolean isSaveFileTmp) {
		this.saveFilePath = saveFilePath;
		this.isSaveFileTmp = isSaveFileTmp;
	}
	public String getSaveStdPath() {
		return saveFilePath;
	}
	protected String getSaveStdTmp() {
		if (saveFilePath == null) {
			return null;
		}
		if (isSaveFileTmp) {
			return FileOperate.changeFileSuffix(saveFilePath, "_tmp", null);
		} else {
			return saveFilePath;
		}
	}

	public void setSaveErrPath(String saveErrPath, boolean isSaveErrTmp) {
		this.saveErrPath = saveErrPath;
		this.isSaveErrTmp = isSaveErrTmp;
	}
	public String getSaveErrPath() {
		return saveErrPath;
	}
	protected String getSaveErrTmp() {
		if (saveErrPath == null) {
			return null;
		}
		if (isSaveErrTmp) {
			return FileOperate.changeFileSuffix(saveErrPath, "_tmp", null);
		} else {
			return saveErrPath;
		}
	}
	
	public void moveResultFile() {
		if (isSaveFileTmp && getSaveStdTmp() != null) {
			FileOperate.moveFile(true, getSaveStdTmp(), saveFilePath);
		}
		if (isSaveErrTmp && getSaveErrPath() != null) {
			FileOperate.moveFile(true, getSaveErrTmp(), saveErrPath);
		}
	}
	
	/** 返回执行的具体cmd命令，不会将文件路径删除，仅给相对路径 */
	public String[] getCmdExeStr() {
		return getCmdExeStrModify();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String[] getCmdExeStrModify() {
		generateTmPath();
		ConvertCmdGetFileName convertGetFileName = new ConvertCmdGetFileName();
		return convertGetFileName.convertCmd(generateRunCmd(false));
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String[] getCmdExeStrReal() {
		generateTmPath();
		return generateRunCmd(false);
	}

	/** 在cmd运行前，将输入文件拷贝到临时文件夹下 */
	public void copyFileIn() {
		generateTmPath();
		createFoldTmp();
		if (!isRedirectInToTmp) return;
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			logger.debug("copy input file from {} to {}", inFile, inTmpName);
			FileOperate.copyFileFolder(inFile, inTmpName, true);
			logger.debug("finish copy {} to {}, size is {}", inFile, inTmpName);
			logger.debug("{} size is {}", inTmpName, FileOperate.getFileSizeLong(inTmpName));
		}
	}
	
	/** 必须先调用{@link #copyFileIn()}，
	 * 等运行cmd结束后还需要调用{@link #moveFileOut()} 来完成运行 */
	public String[] getRunCmd() {
		generateTmPath();
		return generateRunCmd(true);
	}
	
	protected synchronized void generateTmPath() {
		if (isGenerateTmpPath) {
			return;
		}
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
			String tmpPathThis = PathDetail.getRandomWithSep(tmpPath, FileOperate.getFileName(path) + i);
			mapPath2TmpPath.put(path, tmpPathThis);
		}
		
		for (String filePathName : setFileNameAll) {
			String tmpPath = mapPath2TmpPath.get(FileOperate.getParentPathNameWithSep(filePathName));
			String fileName = FileOperate.getFileName(filePathName);
			tmpPath = tmpPath + fileName;
			/** 将已有的输出文件夹在临时文件夹中创建好 */
			if (filePathName.endsWith("/") || filePathName.endsWith("\\")) {
				tmpPath = FileOperate.addSep(tmpPath);
			}
			mapName2TmpName.put(filePathName, tmpPath);
		}
		isGenerateTmpPath = true;
	}
	
	/** 将已有的输出文件夹在临时文件夹中创建好 */
	private void createFoldTmp() {
		for (String filePathName : mapName2TmpName.keySet()) {
			String tmpPath = mapName2TmpName.get(filePathName);
			if ( FileOperate.isFileDirectory(filePathName)) {
				FileOperate.createFolders(tmpPath);
			}
		}
	}
	
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @param redirectStdErr 是否重定向标准输出和错误输出，如果只是获得命令，那不需要重定向<br>
	 * 如果是实际执行的cmd，就需要重定向
	 * @return
	 */
	private String[] generateRunCmd(boolean redirectStdErr) {
		List<String> lsReal = new ArrayList<>();
		boolean stdOut = false;
		boolean errOut = false;
		
		ConvertCmdTmp convertCmdTmp = new ConvertCmdTmp(isRedirectInToTmp, isRedirectOutToTmp, setInput, setOutput, mapName2TmpName);
		ConvertCmd convertCmdHdfs2Local = new ConvertCmd() {
			@Override
			String convert(String subCmd) {
				return FileHadoop.convertToLocalPath(subCmd);
			}
		};
		
		for (String tmpCmd : lsCmd) {
			if (redirectStdErr) {
				if (tmpCmd.equals(">")) {
					stdOut = true;
					setJustDisplayStd(false);
					continue;
				} else if (tmpCmd.equals("2>")) {
					errOut = true;
					setJustDisplayErr(false);
					continue;
				}
			}
			convertCmdTmp.setStdInOut(stdOut, errOut);
			tmpCmd = convertCmdTmp.convertSubCmd(tmpCmd);
			
			if (stdOut) {
				if (StringOperate.isRealNull(saveFilePath)) {
					saveFilePath = tmpCmd;
				}
				stdOut = false;
				continue;
			} else if (errOut) {
				if (StringOperate.isRealNull(saveErrPath)) {
					saveErrPath = tmpCmd;
				}
				errOut = false;
				continue;
			}
			if (isConvertHdfs2Loc) {
				tmpCmd = convertCmdHdfs2Local.convertSubCmd(tmpCmd);
			}
			lsReal.add(tmpCmd);
		}
		String[] realCmd = lsReal.toArray(new String[0]);
		return realCmd;
	}
	
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中
	 * notmove是不需要移动的文件名
	 * @param tmpPath
	 * @param resultPath
	 * @param notMove
	 * @param isDelFile 如果出错是否删除原来的文件
	 */
	public void moveFileOut() {
		if (!mapPath2TmpPath.isEmpty()) {
			logger.info("start move files");
		}
		
		for (String outPath : mapPath2TmpPath.keySet()) {
			String outTmpPath = mapPath2TmpPath.get(outPath);
			
			List<String> lsFilesFinish = FileOperate.getLsFoldFileName(outTmpPath);
			for (String filePath : lsFilesFinish) {
				String  filePathResult = filePath.replaceFirst(outTmpPath, outPath);
				if (setInput.contains(filePathResult) && FileOperate.isFileExistAndBigThanSize(filePathResult, 0)) {
					continue;
				}
				logger.info("move file from  " + filePath + "  to  " + filePathResult);
				FileOperate.moveFile(true, filePath, filePathResult);
			}
		}
		
	}
	
	/** 删除中间文件，会把临时的input文件也删除 */
	public void deleteTmpFile() {
		if (!mapPath2TmpPath.isEmpty()) {
			logger.debug("start delete files");
		}
		for (String tmpPath : mapPath2TmpPath.values()) {
			logger.debug("delete file: " + tmpPath);
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
	
	//============================================================
	//============================================================
	public static abstract class ConvertCmd {
		abstract String convert(String subCmd);
		
		public String convertCmd(String cmd) {
			if (StringOperate.isRealNull(cmd)) {
				return cmd;
			}
			String[] ss = cmd.split(" ");
			List<String> lsCmd = new ArrayList<>();
			for (String subCmd : ss) {
				String subModify = convertSubCmd(subCmd);
				if (StringOperate.isRealNull(subModify)) {
					continue;
				}
				lsCmd.add(subModify);
			}
			return ArrayOperate.cmbString(lsCmd.toArray(new String[0]), " ");
		}
		
		public String[] convertCmd(String[] cmd) {
			if (cmd == null || cmd.length == 0) {
				return cmd;
			}
			List<String> lsCmd = new ArrayList<>();
			for (String subCmd : cmd) {
				String subModify = convertSubCmd(subCmd);
				if (StringOperate.isRealNull(subModify)) {
					continue;
				}
				lsCmd.add(subModify);
			}
			return lsCmd.toArray(new String[0]);
		}
		
		/**
		 *  将cmd中需要定位到临时文件夹的信息修改过来，譬如
		 * -output=/hdfs:/test.txt 修改为
		 * -output=/home/novelbio/test.txt
		 * @param stdOut
		 * @param errOut
		 * @param tmpCmd
		 * @return
		 */
		protected String convertSubCmd(String tmpCmd) {
			tmpCmd = tmpCmd.trim();
			if (tmpCmd.contains("=")) {
				String[] tmpCmd2Path = tmpCmd.split("=", 2);
				tmpCmd = tmpCmd2Path[0] + "=" + convertSubCmd(tmpCmd2Path[1]);
				return tmpCmd;
			} else if (tmpCmd.contains(",")) {
				String[] tmpCmd2Path = tmpCmd.split(",");
				String[] tmpResult = new String[tmpCmd2Path.length];
				for (int i = 0; i < tmpCmd2Path.length; i++) {
					tmpResult[i] = convertSubCmd(tmpCmd2Path[i]);  
	            }
				tmpCmd = ArrayOperate.cmbString(tmpResult, ",");
				return tmpCmd;
			} else if (tmpCmd.contains(";")) {
				String[] tmpCmd2Path = tmpCmd.split(";");
				String[] tmpResult = new String[tmpCmd2Path.length];
				for (int i = 0; i < tmpCmd2Path.length; i++) {
					tmpResult[i] = convertSubCmd(tmpCmd2Path[i]);  
	            }
				tmpCmd = ArrayOperate.cmbString(tmpResult, ";");
				return tmpCmd;
			}
			
			if (tmpCmd.startsWith("\"") && tmpCmd.endsWith("\"")) {
				tmpCmd = tmpCmd.substring(1, tmpCmd.length() - 1);
				tmpCmd = convert(tmpCmd);
				tmpCmd = CmdOperate.addQuot(tmpCmd);
			} else if (tmpCmd.startsWith("\'") && tmpCmd.endsWith("\'")) {
				tmpCmd = tmpCmd.substring(1, tmpCmd.length() - 1);
				tmpCmd = convert(tmpCmd);
				tmpCmd = CmdOperate.addQuotSingle(tmpCmd);
			} else {
				tmpCmd = convert(tmpCmd);
			}
			return tmpCmd;
		}
	}


	public static class ConvertCmdTmp extends ConvertCmd {
		boolean stdOut;
		boolean errOut;
		
		boolean isRedirectOutToTmp;
		boolean isRedirectInToTmp;
		Set<String> setInput;
		Set<String> setOutput;
		Map<String, String> mapName2TmpName;
		
		public ConvertCmdTmp(boolean isRedirectInToTmp, boolean isRedirectOutToTmp, Set<String> setInput, Set<String> setOutput, Map<String, String> mapName2TmpName) {
			this.isRedirectInToTmp = isRedirectInToTmp;
			this.isRedirectOutToTmp = isRedirectOutToTmp;
			this.setInput = setInput;
			this.setOutput = setOutput;
			this.mapName2TmpName = mapName2TmpName;
		}
		
		public void setStdInOut(boolean stdOut, boolean errOut) {
			this.stdOut = stdOut;
			this.errOut = errOut;
		}
		
		@Override
		String convert(String subCmd) {
			if ((isRedirectInToTmp && setInput.contains(subCmd))
					|| 
					(!errOut && !stdOut && isRedirectOutToTmp && setOutput.contains(subCmd))) {
				subCmd = mapName2TmpName.get(subCmd);
			}
			return subCmd;
		}
		
	}

	public static class ConvertCmdGetFileName extends ConvertCmd {
		
		@Override
		String convert(String subCmd) {
			return FileOperate.getFileName(subCmd);
		}
		
	}
}


