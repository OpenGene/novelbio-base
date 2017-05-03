package com.novelbio.base.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.cmd.ConvertCmd.ConvertCmdTmp;
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
	
	/** 只读挂载相对路径 */
	public static final String IN_MAP = ".inmap.";
	/** 只写挂载相对路径 */
	public static final String OUT_MAP = ".outmap.";

	
	protected void setStdAndErr(ConvertCmdTmp convertCmdTmp, boolean stdOut, boolean errOut) {
		//不用设置，全都要转成tmp格式
	}
	
	/** 在cmd运行前，将输入文件拷贝到临时文件夹下
	 * 阿里云因为支持软连接，所以就不需要拷贝了，直接作软连接即可
	 */
	protected void copyFileIn() {
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			try {
				logger.info("link file from {} to {}", inFile, inTmpName);
				FileOperate.linkFile(inFile, inTmpName, false);
			} catch (Exception e) {
				logger.error("link file from " + inFile + " to " + inTmpName + "error", e);
			}
		}
	}
		
	protected void moveSingleFileOut(String filePathTmp, String filePathOut) {
		filePathOut = convertAli2Loc(filePathOut, false);
		if (isRetainTmpFiles) {
			logger.info("move file from  " + filePathTmp + "  to  " + filePathOut);
			FileOperate.moveFile(true, filePathTmp, filePathOut);
			logger.info("link file from  " + filePathOut + "  to  " + filePathTmp);
			FileOperate.linkFile(filePathOut, filePathTmp, true);
		} else {
			//TODO 这里可能全改为move会更好些
			FileOperate.moveFile(true, filePathTmp, filePathOut);
		}
	}
	/**
	 * 把oss://bucket/path/to/myfile 转成 /home/novelbio/.inmap./path/to/myfile
	 * @param path
	 * @param isReadMap <br>
	 * true: 只读挂载 <br>
	 * false: 只写挂载 
	 * @return
	 */
	public static String convertAli2Loc(String path, boolean isReadMap) {
		
		String pathLocal = PathDetailOs.changeOsToLocal(path);
		if (pathLocal.startsWith(PathDetailOs.getOsMountPathWithSep())) {		//	/home/novelbio/oss
			pathLocal = pathLocal.replaceFirst(PathDetailOs.getOsMountPathWithSep(), "");
		} else {
			return path;
		}
		if (pathLocal.startsWith(IN_MAP)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(IN_MAP, ""), false);
		} else if (pathLocal.startsWith(OUT_MAP)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(OUT_MAP, ""), false);
		}
		String head = isReadMap? IN_MAP : OUT_MAP;
		pathLocal = PathDetailOs.getOsMountPathWithSep() + head + "/" + pathLocal;
		
		return pathLocal;
	}
}


