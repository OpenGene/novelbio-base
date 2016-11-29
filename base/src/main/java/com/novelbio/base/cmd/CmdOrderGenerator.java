package com.novelbio.base.cmd;

import java.nio.file.Path;
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
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdGetFileName;
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdTmp;
import com.novelbio.base.cmd.ConvertCmd.ConvertHdfs;
import com.novelbio.base.cmd.ConvertCmd.ConvertOss;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.util.ServiceEnvUtil;

/**
 * 输入cmdlist，将其整理为相应的cmd string array<br>
 * 并且移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 *
 */
//TODO 考虑兼容 "<<"
public class CmdOrderGenerator {
	private static final Logger logger = LoggerFactory.getLogger(CmdOrderGenerator.class);
	
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
	Map<String, String> mapPath2TmpPathIn = new HashMap<>();
	/** 输出文件夹对应的临时输出文件夹 */
	Map<String, String> mapPath2TmpPathOut = new HashMap<>();
	
	/** 是否保存stdout文件, <b>默认为true</b><br>
	 * stdout的保存有几种：<br>
	 * 1. 在cmd命令中添加 > 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveFilePath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	boolean isSaveStdFile = true;
	/** 截获标准输出流的输出文件名 */
	String saveFilePath = null;
	/** 输出文件是否为txt，如果命令中含有 >，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayStd = false;
	
	/** 是否保存stderr文件，同 {@link #isSaveStdFile} */
	boolean isSaveErrFile = true;
	/** 截获标准错误流的错误文件名 */
	String saveErrPath = null;
	/** 输出错误文件是否为txt，如果命令中含有 2>，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayErr = false;
	
	String stdInput = null;
	
	/** 是否已经生成了临时文件夹，生成一次就够了 */
	boolean isGenerateTmpPath = false;
	
	/** 是否将hdfs的路径，改为本地路径
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 * 
	 * 也有把oss或者bos路径修改为本地路径
	 */
	boolean isConvertHdfs2Loc = true;
	
	private String tmpPath;
	protected boolean isRetainTmpFiles = false;
	
	/** 临时文件夹中很可能已经存在了一些文件，这些文件不需要被拷贝出去 */
	private Map<String, long[]> mapFileName2LastModifyTimeAndLen = new HashMap<>();
	
	protected CmdOrderGenerator() {};
	
	/** 设定复制输入输出文件所到的临时文件夹 */
	public void setTmpPath(String tmpPath) {
		this.tmpPath = tmpPath;
	}
	/** 临时文件夹中的文件是否删除 */
	public void setRetainTmpFiles(boolean isRetainTmpFiles) {
		this.isRetainTmpFiles = isRetainTmpFiles;
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
	 * @param input 输入文件的哪个参数
	 */
	public void addCmdParamInput(String input) {
		if (StringOperate.isRealNull(input)) {
			throw new ExceptionCmd("input is null");
		}
		setInput.add(input);
	}
	
	public void setLsCmd(List<String> lsCmd) {
		this.lsCmd = lsCmd;
	}
	
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，如果输入参数类似 "--outPath=/hdfs:/test.fa"，这里填写 "/hdfs:/test.fa"
	 */
	public void addCmdParamOutput(String output) {
		if (StringOperate.isRealNull(output)) {
			throw new ExceptionCmd("Output is null");
		}
		setOutput.add(output);
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
	
	public String getStdInFile() {
		return stdInput;
	}
	
	/** 设定最后保存的标准输出流文件名，注意会设定 {@link #setIsSaveStdFile(boolean)}为true */
	public void setSaveFilePath(String saveFilePath) {
		this.saveFilePath = saveFilePath;
		this.isSaveStdFile = true;
	}
	public String getSaveStdPath() {
		return saveFilePath;
	}
	
	protected String getSaveStdTmp() {
		if (saveFilePath == null) {
			return null;
		}
		if (isSaveStdFile) {
			return FileOperate.changeFileSuffix(saveFilePath, "_tmp", null);
		} else {
			return saveFilePath;
		}
	}

	/** 设定最后保存的标准错误流文件名，注意会设定 {@link #setIsSaveErrFile(boolean)}为true */
	public void setSaveErrPath(String saveErrPath) {
		this.saveErrPath = saveErrPath;
		this.isSaveErrFile = true;
	}

	public String getSaveErrPath() {
		return saveErrPath;
	}
	protected String getSaveErrTmp() {
		if (saveErrPath == null) {
			return null;
		}
		if (isSaveErrFile) {
			return FileOperate.changeFileSuffix(saveErrPath, "_tmp", null);
		} else {
			return saveErrPath;
		}
	}
	
	/** 是否保存stdout文件，<b>默认为true</b><br>
	 * stdout的保存有几种：<br>
	 * 1. 在cmd命令中添加 > 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveFilePath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	protected void setIsSaveStdFile(boolean isSaveStdFile) {
		this.isSaveStdFile = isSaveStdFile;
	}
	
	/** 是否保存stderr文件， <b>默认为true</b><br>
	 * stderr的保存有几种：<br>
	 * 1. 在cmd命令中添加 2> 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveErrPath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	protected void setIsSaveErrFile(boolean isSaveErrFile) {
		this.isSaveErrFile = isSaveErrFile;
	}
	
	public void moveStdFiles() {
		if (isSaveStdFile && getSaveStdTmp() != null) {
			FileOperate.moveFile(true, getSaveStdTmp(), saveFilePath);
		}
		if (isSaveErrFile && getSaveErrPath() != null) {
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

	/** 在cmd运行前，将输入文件拷贝到临时文件夹下
	 * 同时记录临时文件夹下有多少文件，用于后面删除时跳过 */
	public void copyFileInAndRecordFiles() {
		createFoldTmp();
		if (isRedirectInToTmp) {
			copyFileIn();
		}
		if (isRedirectOutToTmp) {
			mapFileName2LastModifyTimeAndLen.clear();
			List<Path> lsPaths = FileOperate.getLsFoldPathRecur(tmpPath, false);
			lsPaths.forEach((path)->{
				mapFileName2LastModifyTimeAndLen.put(FileOperate.getAbsolutePath(path), 
						getLastModifyTime2Len(path));
			}); 
		}
	}
	
	/** 把要输入的文件拷贝到临时文件夹中 */
	protected void copyFileIn() {
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			try {
				FileOperate.copyFileFolder(inFile, inTmpName, false);
				logger.info("copy file from {} to {}", inFile, inTmpName);
			} catch (Exception e) {
				logger.error("copy file from " + inFile + " to " + inTmpName + "error", e);
			}
		}
	}
	
	/** 必须先调用{@link #copyFileInAndRecordFiles()}，
	 * 等运行cmd结束后还需要调用{@link #moveFileOut()} 来完成运行 */
	public String[] getRunCmd() {
		generateTmPath();
		return generateRunCmd(true);
	}
	
	//============================ 生成临时文件夹和配对路径 ============================
	protected synchronized void generateTmPath() {
		if (isGenerateTmpPath) {
			return;
		}
		Set<String> setFileNameAll = new HashSet<>();
		
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		if (isRedirectInToTmp) {
			mapPath2TmpPathIn = getMapPath2TmpPath(setInput, getTmp());
			setFileNameAll.addAll(setInput);
			mapPath2TmpPath.putAll(mapPath2TmpPathIn);
		}
		if (isRedirectOutToTmp) {
			mapPath2TmpPathOut = getMapPath2TmpPath(setOutput, getTmp());
			setFileNameAll.addAll(setOutput);
			mapPath2TmpPath.putAll(mapPath2TmpPathOut);
		}
		
		mapName2TmpName = getMapName2TmpName(setFileNameAll, mapPath2TmpPath);
		isGenerateTmpPath = true;
	}
	
	private Map<String, String> getMapPath2TmpPath(Set<String> setFiles, String pathTmp) {
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		Set<String> setPath = new HashSet<>();
		for (String inFileName : setFiles) {
			String inPath = FileOperate.getParentPathNameWithSep(inFileName);
			setPath.add(inPath);
		}
		
		Set<String> setPathNoDup = new HashSet<>();
		for (String path : setPath) {
			String parentPath = FileOperate.getFileName(path);
			String parentPathFinal = parentPath;
			int i = 1;//防止产生同名文件夹的措施
			while (setPathNoDup.contains(parentPathFinal)) {
				parentPathFinal = parentPath + i++;
			}
			setPathNoDup.add(parentPathFinal);
			String tmpPathThis = pathTmp + parentPathFinal+ FileOperate.getSepPath();
			mapPath2TmpPath.put(path, tmpPathThis);
		}
		return mapPath2TmpPath;
	}
	
	private Map<String, String> getMapName2TmpName(Set<String> setFileNameAll, Map<String, String> mapPath2TmpPath) {
		Map<String, String> mapName2TmpName = new HashMap<>();

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
		return mapName2TmpName;
	}
	
	//===========================================================================
	
	protected String getTmp() {
		if (StringOperate.isRealNull(tmpPath)) {
			tmpPath = PathDetail.getTmpPathRandom();
		}
		return tmpPath;
	}
	/** 将已有的输出文件夹在临时文件夹中创建好 */
	protected void createFoldTmp() {
		for (String filePathName : mapName2TmpName.keySet()) {
			String tmpPath = mapName2TmpName.get(filePathName);
			if ( FileOperate.isFileDirectory(filePathName)) {
				FileOperate.createFolders(tmpPath);
			} else {
				FileOperate.createFolders(FileOperate.getParentPathNameWithSep(tmpPath));
			}
		}
	}
	
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @param redirectStdErr 是否重定向标准输出和错误输出，如果只是获得命令，那不需要重定向<br>
	 * 如果是实际执行的cmd，就需要重定向
	 * @return
	 */
	protected String[] generateRunCmd(boolean redirectStdAndErr) {
		List<String> lsReal = new ArrayList<>();
		boolean stdOut = false;
		boolean errOut = false;
		boolean stdIn = false;
		saveFilePath = null;
		saveErrPath = null;
		
		ConvertCmdTmp convertCmdTmp = new ConvertCmdTmp(isRedirectInToTmp, isRedirectOutToTmp,
				setInput, setOutput, mapName2TmpName);
		ConvertCmd convertOs2Local = getConvertOs2Local();
		
		for (String tmpCmd : lsCmd) {
			if (redirectStdAndErr) {
				if (tmpCmd.equals(">")  || tmpCmd.equals("1>")) {
					stdOut = true;
					setJustDisplayStd(false);
					continue;
				} else if (tmpCmd.equals("2>")) {
					errOut = true;
					setJustDisplayErr(false);
					continue;
				} else if (tmpCmd.equals("<")) {
					stdIn = true;
					continue;
				}
			}
			
			setStdAndErr(convertCmdTmp, stdOut, errOut);
			tmpCmd = convertCmdTmp.convertSubCmd(tmpCmd);
			
			if (stdOut) {
				if (isSaveStdFile && StringOperate.isRealNull(saveFilePath)) {
					saveFilePath = tmpCmd;
				}
				stdOut = false;
				continue;
			} else if (errOut) {
				if (isSaveErrFile && StringOperate.isRealNull(saveErrPath)) {
					saveErrPath = tmpCmd;
				}
				errOut = false;
				continue;
			} else if (stdIn) {
				if (StringOperate.isRealNull(stdInput)) {
					stdInput = tmpCmd;
				}
				stdIn = false;
				continue;
			}
			if (isConvertHdfs2Loc) {
				tmpCmd = convertOs2Local.convertSubCmd(tmpCmd);
			}
			lsReal.add(tmpCmd);
		}
		String[] realCmd = lsReal.toArray(new String[0]);
		return realCmd;
	}
	
	protected void setStdAndErr(ConvertCmdTmp convertCmdTmp, boolean stdOut, boolean errOut) {
		convertCmdTmp.setStdInOut(stdOut, errOut);
	}
	
	protected ConvertCmd getConvertOs2Local() {
		return ServiceEnvUtil.isAliyunEnv() ? new ConvertOss() : new ConvertHdfs();
	}
	
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中 */
	public void moveFileOut() {
		if (!mapPath2TmpPathOut.isEmpty()) {
			logger.info("start move files");
		}
		
		for (String outPath : mapPath2TmpPathOut.keySet()) {
			String outTmpPath = mapPath2TmpPathOut.get(outPath);
			//遍历某个输出临时文件夹下的全体文件，看是否是cmd运行之前就保存的文件
			//如果是新生成的文件，就可以拷贝出去
			List<String> lsFilesFinish = FileOperate.getLsFoldFileName(outTmpPath);
			for (String fileInTmp : lsFilesFinish) {
				String  filePathResult = fileInTmp.replaceFirst(outTmpPath, outPath);
				if (setInput.contains(filePathResult) && FileOperate.isFileExistAndBigThanSize(filePathResult, 0)) {
					continue;
				}
				
				List<String> lsFilesNeedMove = getLsFileNeedMove(fileInTmp);
				for (String file : lsFilesNeedMove) {
					String  filePathOut = fileInTmp.replaceFirst(outTmpPath, outPath);
					moveSingleFileOut(file, filePathOut);
				}
			}
		}
	}
	
	/**
	 * 给定某个文件或文件夹，看里面哪些文件需要移动
	 * @param fileInTmp
	 * @return
	 */
	private List<String> getLsFileNeedMove(String fileInTmp) {
		List<String> lsFileNeedMove = new ArrayList<>();
		List<Path> lsFiles = FileOperate.getLsFoldPathRecur(fileInTmp, false);
		for (Path path : lsFiles) {
			String pathStr = FileOperate.getAbsolutePath(path);
			if (!mapFileName2LastModifyTimeAndLen.containsKey(pathStr)) {
				lsFileNeedMove.add(FileOperate.getAbsolutePath(path));
				continue;
			}
			long[] lastModifyTime2Len = mapFileName2LastModifyTimeAndLen.get(pathStr);
			long[] lastModifyTime2LenThis = getLastModifyTime2Len(path);			
			if (lastModifyTime2LenThis[0] != lastModifyTime2Len[0] || lastModifyTime2LenThis[1] != lastModifyTime2Len[1]) {
				lsFileNeedMove.add(FileOperate.getAbsolutePath(path));
			}
		}
		return lsFileNeedMove;
	
	}
	
	private long[] getLastModifyTime2Len(Path path) {
		long len = FileOperate.getFileSizeLong(path);
		long lastModifyTime = len < 0 ? 0 : FileOperate.getTimeLastModify(path);
		return new long[]{lastModifyTime, len};
	}
	
	protected void moveSingleFileOut(String filePathTmp, String filePathOut) {
		String operate = isRetainTmpFiles? "copy" : "move";
		logger.info(operate + " file from  " + filePathTmp + "  to  " + filePathOut);
		if (isRetainTmpFiles) {
			FileOperate.copyFileFolder(filePathTmp, filePathOut, true);
		} else {
			FileOperate.moveFile(true, filePathTmp, filePathOut);
		}
	}
	
	/** 删除中间文件，会把临时的input文件也删除 */
	public void deleteTmpFile() {
		if (isRetainTmpFiles) {
			return;
		}     
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		mapPath2TmpPath.putAll(mapPath2TmpPathIn);
		mapPath2TmpPath.putAll(mapPath2TmpPathOut);
		if (!mapPath2TmpPath.isEmpty()) {
			logger.debug("start delete files");
		}
		for (String tmpPath : mapPath2TmpPath.values()) {
			logger.debug("delete file: " + tmpPath);
			FileOperate.deleteFileFolder(tmpPath);
		}
	}
	
	public void clearLsCmd() {
		lsCmd.clear();
		setInput.clear();
		setOutput.clear();
		mapName2TmpName.clear();
		mapPath2TmpPathIn.clear();
		mapPath2TmpPathOut.clear();
	}

	public static CmdOrderGenerator getInstance(boolean isLocal) {
		if (isLocal) {
			return new CmdOrderGenerator();
		} else {
			return new CmdOrderGeneratorAli();
		}
	}
	
}