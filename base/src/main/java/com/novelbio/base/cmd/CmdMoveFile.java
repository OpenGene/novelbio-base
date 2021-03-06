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
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdTmp;
import com.novelbio.base.dataStructure.doubleArrayTrie.TrieMapLongFindShort;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 *
 */
public class CmdMoveFile {
	private static final Logger logger = LoggerFactory.getLogger(CmdMoveFile.class);
		
	private boolean isRedirectInToTmp = false;
	private boolean isRedirectOutToTmp = false;
	
	/** 是否已经生成了临时文件夹，生成一次就够了 */
	private boolean isGenerateTmpPath = false;
	
	protected boolean isRetainTmpFiles = false;
	
	/** 输出的临时文件夹路径 */
	private String tmpPath;
	
	/** 全体需要copyToTmp的输入文件 */
	protected Set<String> setInput = new HashSet<>();
	/** 全体需要copyToTmp的输出文件，将文件头部进行合并 */
	protected Set<String> setOutputMerge = new HashSet<>();
	/** 全体需要copyToTmp的输出文件 */
	protected Set<String> setOutput = new HashSet<>();
	
	/** 全体不需要copyToTmp的输入文件 */
	protected Set<String> setInNotCopy = new HashSet<>();
	/** 全体不需要copyToTmp的输入文件 */
	protected Set<String> setOutNotCopy = new HashSet<>();
	/**
	 * key: 输入或输出的文件(夹)全名
	 * value: 临时文件(夹)全名
	 */
	protected Map<String, String> mapName2TmpName = new HashMap<>();
	/** 输入文件夹对应的输出文件夹，可以将输入文件夹里的文件直接复制到输出文件夹中 */
	private Map<String, String> mapPath2TmpPathIn = new HashMap<>();
	/** 输出文件夹对应的临时输出文件夹 */
	private Map<String, String> mapPath2TmpPathOut = new HashMap<>();
	
	/** 临时文件夹中很可能已经存在了一些文件，这些文件不需要被拷贝出去，
	 * 那么就需要在运行前先保存这些文件的大小和时间等信息，运行完毕后把
	 * 结果文件夹中的文件与这里面的信息进行比较。如果一致就不拷贝出去
	 */
	private Map<String, long[]> mapFileName2LastModifyTimeAndLen = new HashMap<>();
	
	private CmdPathCluster cmdPathCluster;
	
	private boolean isNeedDeleteTmpPath = false;
	
	boolean isNeedLog = true;
	
	public void setCmdPathCluster(CmdPathCluster cmdPathCluster) {
		this.cmdPathCluster = cmdPathCluster;
	}
	
	/** 是否输出日志，默认为true */
	public void setNeedLog(boolean isNeedLog) {
		this.isNeedLog = isNeedLog;
	}
	
	/** 设定复制输入输出文件所到的临时文件夹 */
	public void setTmpPath(String tmpPath) {
		if (StringOperate.isRealNull(tmpPath)) {
			throw new ExceptionCmd("tmpPath cannot be null");
		}
		this.tmpPath = FileOperate.addSep(tmpPath);
		isNeedDeleteTmpPath = false;
	}
	public synchronized String getTmpPath() {
		if (StringOperate.isRealNull(tmpPath)) {
			tmpPath = PathDetail.getTmpPathRandom();
			isNeedDeleteTmpPath = true;
		}
		return tmpPath;
	}
	
	public void deleteTmpPath() {
		if (isNeedDeleteTmpPath) {
			FileOperate.deleteFileFolder(tmpPath);
		}
	}
	
	/** 临时文件夹中的文件是否删除 */
	public void setRetainTmpFiles(boolean isRetainTmpFiles) {
		this.isRetainTmpFiles = isRetainTmpFiles;
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
	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param input 输入文件的哪个参数
	 */
	public void addCmdParamInput(List<String> input) {
		for (String path : input) {
			addCmdParamInput(path);
		}
	}
	/** 如果部分文件需要拷贝，则要设置不需要拷贝的文件夹 */
	public void addCmdParamInputNotCopy(List<String> input) {
		for (String path : input) {
			if (StringOperate.isRealNull(path)) {
				throw new ExceptionCmd("input is null");
			}
			setInNotCopy.add(path);
		}
	}
	/** 如果部分文件需要拷贝，则要设置不需要拷贝的文件夹 */
	public void addCmdParamOutputNotCopy(List<String> output) {
		for (String path : output) {
			if (StringOperate.isRealNull(path)) {
				throw new ExceptionCmd("input is null");
			}
			setOutNotCopy.add(path);
		}
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
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，如果输入参数类似 "--outPath=/hdfs:/test.fa"，这里填写 "/hdfs:/test.fa"
	 */
	public void addCmdParamOutput(List<String> output) {
		for (String path : output) {
			addCmdParamOutput(path);
		}
	}
	public void prepareAndMoveFileIn() {
		generateTmPath();
		createFoldTmp();
		
		copyFileIn();
		recordFilesWhileRedirectOutToTmp();
	}
	
	//============================ 生成临时文件夹和配对路径 ============================
	/** 生成实际文件和临时文件对照表 */
	public synchronized void generateTmPath() {
  		if (isGenerateTmpPath) {
			return;
		}
		if (cmdPathCluster == null) cmdPathCluster = new CmdPathCluster();
		
		Set<String> setFileNameAll = new HashSet<>();
		
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		String tmpPath = getTmpPath();
		
		if (isNeedLog) {
			logger.debug("tmp path is " + tmpPath);
		}
		
		if (isRedirectInToTmp) {
			mapPath2TmpPathIn = cmdPathCluster.getMapInPath2TmpPath(setInput, tmpPath);
			logger.debug("print mapPath2TmpPathIn");
			
			logMapInfo(mapPath2TmpPathIn);
			setFileNameAll.addAll(setInput);
			mapPath2TmpPath.putAll(mapPath2TmpPathIn);
		}
		
		if (isRedirectOutToTmp) {
			setOutputMerge = CmdPathCluster.mergeParentPath(setOutput);
			mapPath2TmpPathOut = cmdPathCluster.getMapOutPath2TmpPath(setOutputMerge, tmpPath);
			logger.debug("print mapPath2TmpPathOut");

			logMapInfo(mapPath2TmpPathOut);
			setFileNameAll.addAll(setOutputMerge);
			mapPath2TmpPath.putAll(mapPath2TmpPathOut);
		}
  		mapName2TmpName = getMapName2TmpName(setFileNameAll, mapPath2TmpPath);
  		
  		if (isNeedLog) {
  			logger.debug("print mapName2TmpName");
		}
  	
		logMapInfo(mapName2TmpName);
		isGenerateTmpPath = true;
	}
	
	private void logMapInfo(Map<String, String> mapName2TmpName) {
		if (!isNeedLog) {
			return;
		}
		for (String name : mapName2TmpName.keySet()) {
			logger.debug(name + "=" + mapName2TmpName.get(name));
		}
	}
	
	private Map<String, String> getMapName2TmpName(Set<String> setFileNameAll, Map<String, String> mapPath2TmpPath) {
		TrieMapLongFindShort<String> trieMapLongFindShort = new TrieMapLongFindShort<>(mapPath2TmpPath);
		for (String filePathName : setFileNameAll) {
			String path = trieMapLongFindShort.getKeyFirst(filePathName);
			String tmpPath = mapPath2TmpPath.get(path);
			String tmpPathFile = filePathName.replaceFirst(path, tmpPath);
			mapName2TmpName.put(filePathName, tmpPathFile);
		}
		return mapName2TmpName;
	}	
	
	//===========================================================================

	/** 在cmd运行前，将输入文件拷贝到临时文件夹下
	 * 同时记录临时文件夹下有多少文件，用于后面删除时跳过 */
	public void copyFileInAndRecordFiles() {
		copyFileIn();
		recordFilesWhileRedirectOutToTmp();
	}
	
	/** 将已有的输出文件夹在临时文件夹中创建好 */
	protected void createFoldTmp() {
  		if (isNeedLog) {
  			logger.info("start create tmp folder");
		}
		ConvertCmdTmp cmdTmp = generateConvertCmdTmp();
		for (String path : setOutput) {
			String tmpPath = cmdTmp.convert(path);
			if (tmpPath.endsWith("/") || tmpPath.endsWith("\\")) {
				logger.info("create folder " + tmpPath);
				FileOperate.createFolders(tmpPath);
			} else {
				logger.info("create folder " + FileOperate.getParentPathNameWithSep(tmpPath));
				FileOperate.createFolders(FileOperate.getParentPathNameWithSep(tmpPath));
			}
		}
	}
	
	/** 把要输入的文件拷贝到临时文件夹中 */
	protected void copyFileIn() {
		if (!isRedirectInToTmp) return;
		if (isNeedLog) logger.info("start copy file from storage to local");
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			try {
				FileOperate.copyFileFolder(inFile, inTmpName, false);
				logger.info("copy file from {} to {}", inFile, inTmpName);
			} catch (Exception e) {
				logger.error("copy file from " + inFile + " to " + inTmpName + " error", e);
			}
		}
	}

	/**
	 * 记录临时文件夹下有多少文件，主要用于 {@link #isRedirectOutToTmp}的情况
	 * 就是如果需要把结果文件拷贝到临时文件夹下，那么拷贝完成后需要运行该命令
	 */
	protected void recordFilesWhileRedirectOutToTmp() {
		if (!isRedirectOutToTmp) return;
		
		mapFileName2LastModifyTimeAndLen.clear();
		List<Path> lsPaths = FileOperate.getLsFoldPathRecur(getTmpPath(), true);
		lsPaths.forEach((path)->{
			mapFileName2LastModifyTimeAndLen.put(FileOperate.getAbsolutePath(path), 
					getLastModifyTime2Len(path));
		}); 
	}
	
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中 */
	public void moveFileOut() {
		moveFileOut(true);
	}
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中 */
	public void moveFileOutError() {
		moveFileOut(false);
	}
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中
	 * @param isNormal 是否正常结束
	 */
	private void moveFileOut(boolean isNormal) {
		if (!mapPath2TmpPathOut.isEmpty()) {
			logger.info("start move files");
		}
		
		for (String outPath : mapPath2TmpPathOut.keySet()) {
			String outTmpPath = mapPath2TmpPathOut.get(outPath);
			outPath = isNormal? outPath : outPath+"error/";
			//遍历某个输出临时文件夹下的全体文件，看是否是cmd运行之前就保存的文件
			//如果是新生成的文件，就可以拷贝出去
			List<String> lsFilesFinish = FileOperate.getLsFoldFileName(outTmpPath);
			logger.debug("outpath: " + outPath + " outTmpPath: " + outTmpPath + " lsFilesFinish: " + lsFilesFinish.toString());
			for (String fileInTmp : lsFilesFinish) {
				String  filePathResult = fileInTmp.replaceFirst(outTmpPath, outPath);
				if (setInput.contains(filePathResult) ) {
					Path path = FileOperate.getPath(filePathResult);
					if (!FileOperate.isFileDirectory(path) && FileOperate.isFileExistAndBigThanSize(path, 0)) {
						continue;
					}
				}
				
				List<String> lsFilesInTmpNeedMove = getLsFileInTmpNeedMove(FileOperate.getPath(fileInTmp));
				logger.debug("fileInTmp: " + fileInTmp + " filePathResult: " + filePathResult + " lsFilesInTmpNeedMove: " + lsFilesInTmpNeedMove);
				for (String file : lsFilesInTmpNeedMove) {
					String  filePathOut = file.replaceFirst(outTmpPath, outPath);
					moveSingleFileOut(file, filePathOut);
					cmdPathCluster.putTmpOut2Out(file, filePathOut);
				}
			}
		}
	}
	/**
	 * 给定某个临时文件或文件夹，看里面哪些文件需要移动
	 * @param pathInTmp
	 * @return
	 */
	private List<String> getLsFileInTmpNeedMove(Path pathInTmp) {
		List<String> lsFileNeedMove = new ArrayList<>();
		String pathStr = FileOperate.getAbsolutePath(pathInTmp);
		//全新的文件，则直接返回表示需要移动
		if (!mapFileName2LastModifyTimeAndLen.containsKey(pathStr)) {
			logger.debug("file {} not exist, so it is a new file and need to move to result file", pathStr);
			lsFileNeedMove.add(FileOperate.getAbsolutePath(pathInTmp));
			return lsFileNeedMove;
		}
		
		//文件修改过了，修改时间和文件大小都不同了，也直接返回
		long[] lastModifyTime2Len = mapFileName2LastModifyTimeAndLen.get(pathStr);
		long[] lastModifyTime2LenThis = getLastModifyTime2Len(pathInTmp);

		if (lastModifyTime2LenThis[0] != lastModifyTime2Len[0] || lastModifyTime2LenThis[1] != lastModifyTime2Len[1]) {
			lsFileNeedMove.add(FileOperate.getAbsolutePath(pathInTmp));
			return lsFileNeedMove;
		}
		
		//输入的不是文件夹，直接返回
		if (!FileOperate.isFileDirectory(pathInTmp)) {
			return lsFileNeedMove;
		}
		
		List<Path> lsSubPaths = FileOperate.getLsFoldPath(pathInTmp);
		if (lsSubPaths.isEmpty()) {
			lsFileNeedMove.add(FileOperate.getAbsolutePath(pathInTmp));
		}
		//TODO 为什么不移动着个
		for (Path path : lsSubPaths) {
			lsFileNeedMove.addAll(getLsFileInTmpNeedMove(path));
		}
		return lsFileNeedMove;
	}
	
	/**
	 * 文件夹的时间和大小都为0
	 * @param path
	 * @return
	 */
	private long[] getLastModifyTime2Len(Path path) {
		long len = FileOperate.isFileDirectory(path) ? 0 : FileOperate.getFileSizeLong(path);
		long lastModifyTime = len <= 0 ? 0 : FileOperate.getTimeLastModify(path);
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
		setInput.clear();
		setOutput.clear();
		mapName2TmpName.clear();
		mapPath2TmpPathIn.clear();
		mapPath2TmpPathOut.clear();
	}
	
	/** 用于做路径转换 */
	public ConvertCmdTmp generateConvertCmdTmp() {
		return new ConvertCmdTmp(isRedirectInToTmp, isRedirectOutToTmp,
				setInput, setInNotCopy, setOutputMerge, setOutNotCopy, mapName2TmpName);
	}
	
}
