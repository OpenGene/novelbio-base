package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
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
public class CmdMoveFileAli extends CmdMoveFile {
	private static final Logger logger = LoggerFactory.getLogger(CmdMoveFileAli.class);
	
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
			logger.info("move file from {} to {} ",  filePathTmp, filePathOut);
			FileOperate.moveFile(true, filePathTmp, filePathOut);
			logger.info("link file from  " + filePathOut + "  to  " + filePathTmp);
			FileOperate.linkFile(filePathOut, filePathTmp, false);
		} else {
			//TODO 这里可能全改为move会更好些
			FileOperate.moveFile(true, filePathTmp, filePathOut);
		}
		
		/**
		 * 因为aliyun的输入是inmap，输出是outmap
		 * 因此存在情况：
		 * xml1 对 /home/.inmap./a.fasta建索引为 /home/.outmap./a.fasta.fai
		 * xml 输入参数为  /home/.inmap./a.fasta 并且isCopyToTmp = false
		 * 这时候 我们就要求 /home/.inmap./a.fasta.fai 也必须存在
		 * 所以就要在这里把所有潜在的 setInOutput 的输出文件链接到其对应的 inmap中去
		 */
		String filePathOutInmap = convertAli2Loc(filePathOut, true);
		for (String parentPath : setInOutput) {
			if (filePathOutInmap.startsWith(parentPath)) {
				FileOperate.linkFile(filePathOut, filePathOutInmap, false);
				break;
			}
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
	
	/**
	 * 主要应付aliyun oss的变态特性。<br>
	 * <br>
	 * 一般来说输入文件都要从只读挂载中获取，但是也不排除特殊情况<br>
	 * 譬如有3个xml，A.xml输出文件.outmap./result.bam<br>
	 * B.xml需要 mv .outmap./result.bam --> .outmap./move.result.bam<br>
	 * 如果A.xml运行完接着运行B.xml 这时候输入文件为 .outmap./result.bam<br>
	 * 如果A.xml运行完后task中断，然后重跑task，这时候 .outmap./result.bam是不存在的，反而由于重新挂载， .inmap./result.bam是存在的<br>
	 * <br>
	 * 因此这里需要判断 <br>
	 * 1. 是否为oss，就是比较 pathRead和pathWrite是否相同<br>
	 * 2. pathRead和pathWrite哪个文件存在，哪个存在就用哪个。如果两个都不存在，就用pathRead。当然这种情况也可以抛出异常。<br>
	 * @param path
	 * @return
	 */
	public static String convertPathAliRead(String path) {
		String pathIn = CmdMoveFileAli.convertAli2Loc(path, true);
		String pathOut = CmdMoveFileAli.convertAli2Loc(path, false);
		if (!StringOperate.isEqual(pathIn, pathOut)) {
			boolean isPathInExist = FileOperate.isFileExist(pathIn);
			boolean isPathOutExist = FileOperate.isFileExist(pathOut);
			logger.debug("pathIn {} is exist: {}", pathIn, isPathInExist);
			logger.debug("pathOut {} is exist: {}", pathOut, isPathOutExist);
			path = isPathInExist || !isPathOutExist ? pathIn : pathOut;
			logger.debug("result is {}", path);
		}
		return path;
	}
}
