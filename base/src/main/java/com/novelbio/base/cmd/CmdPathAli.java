package com.novelbio.base.cmd;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.jsr203.bos.PathDetailOs;


/**
 * 输入cmdlist，将其整理为相应的cmd string array<br>
 * 并且移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 */
public class CmdPathAli extends CmdPath {
	private static final Logger logger = LoggerFactory.getLogger(CmdPathAli.class);

	/** 在cmd运行前，将输入文件拷贝到临时文件夹下
	 * 阿里云因为支持软连接，所以就不需要拷贝了，直接作软连接即可
	 */
	public void copyFileIn() {
		createFoldTmp();
		if (!isRedirectInToTmp) return;
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			logger.info("copy file from {} to {}", inFile, inTmpName);
			try {
				FileOperate.linkFile(inFile, inTmpName, false);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/** 将tmpPath文件夹中的内容全部移动到resultPath中 */
	public void moveFileOut() {
		if (!mapPath2TmpPathOut.isEmpty()) {
			logger.info("start move files");
		}
		
		for (String outPath : mapPath2TmpPathOut.keySet()) {
			String outTmpPath = mapPath2TmpPathOut.get(outPath);
			
			List<String> lsFilesFinish = FileOperate.getLsFoldFileName(outTmpPath);
			for (String filePath : lsFilesFinish) {
				String  filePathResult = filePath.replaceFirst(outTmpPath, outPath);
				if (setInput.contains(filePathResult) && FileOperate.isFileExistAndBigThanSize(filePathResult, 0)) {
					continue;
				}
				filePathResult = convertAli2Loc(filePathResult, false);
				logger.info("move file from  " + filePath + "  to  " + filePathResult);
				if (isRetainTmpFiles) {
					FileOperate.copyFileFolder(filePath, filePathResult, true);
				} else {
					FileOperate.moveFile(true, filePath, filePathResult);
				}
			}
		}
	}
	
	public static String convertAli2Loc(String path, boolean isInMap) {
		String inMap = ".inmap.", outMap = ".outmap.";
		
		String pathLocal = PathDetailOs.changeOsToLocal(path);
		if (!pathLocal.startsWith(PathDetailOs.getOsMountPathWithSep())) {
			return pathLocal;
		}
		pathLocal = pathLocal.replaceFirst(PathDetailOs.getOsMountPathWithSep(), "");
		if (pathLocal.startsWith(inMap)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(inMap, ""), false);
		} else if (pathLocal.startsWith(outMap)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(outMap, ""), false);
		}
		String head = isInMap? inMap:outMap;
		pathLocal = PathDetailOs.getOsMountPathWithSep() + head + "/" + pathLocal;
		
		return pathLocal;
	}
}


