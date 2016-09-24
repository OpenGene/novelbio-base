package com.novelbio.base.cmd;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.fileOperate.FileOperate;


/**
 * 输入cmdlist，将其整理为相应的cmd string array<br>
 * 并且移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 */
public class CmdPathAli extends CmdPath {
	private static final Logger logger = LoggerFactory.getLogger(CmdPathAli.class);
	
	/** 在cmd运行前，将输入文件拷贝到临时文件夹下 */
	public void copyFileIn() {
		createFoldTmp();
		if (!isRedirectInToTmp) return;
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			logger.info("copy file from {} to {}", inFile, inTmpName);
			try {
				FileOperate.copyFileFolder(inFile, inTmpName, false);

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
				logger.info("move file from  " + filePath + "  to  " + filePathResult);
				if (isRetainTmpFiles) {
					FileOperate.copyFileFolder(filePath, filePathResult, true);
				} else {
					FileOperate.moveFile(true, filePath, filePathResult);
				}
			}
		}
	}

	public static String convertAli2Loc(String path, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
}


