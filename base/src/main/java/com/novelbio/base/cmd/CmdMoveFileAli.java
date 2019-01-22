package com.novelbio.base.cmd;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdTmp;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.util.ServiceEnvUtil;
import com.novelbio.jsr203.objstorage.PathDetailObjStorage;


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
		if (isNeedLog) logger.info("start link file from storage to local");
		
		for (String inFile : setInput) {
			String inTmpName = mapName2TmpName.get(inFile);
			if (StringOperate.isRealNull(inTmpName)) {
				continue;
			}
			try {
				//如果是输入文件，文件在只读挂载中，则把文件拷贝过来，因为输入文件的性能不行
				//如果是输出文件，文件在结果文件夹，还没写入对象存储，这个就可以链接过来了
				if (isInmapFile(inFile)) {
					logger.info("copy file from {} to {}", inFile, inTmpName);
					
					if (FileOperate.isFileExistAndBigThan0(inFile) && !FileOperate.isFileExistAndBigThan0(inTmpName)) {
						String tmpFile = inTmpName + ".tmp";
						InputStream input = FileOperate.getInputStream(inFile);
						OutputStream output = FileOperate.getOutputStream(tmpFile);
						IOUtils.copy(input, output);
						IOUtils.closeQuietly(input);
						IOUtils.closeQuietly(output);
						FileOperate.moveFile(true, tmpFile, inTmpName);
					}
//					FileOperate.copyFileFolder(inFile, inTmpName, false);
				} else {
					logger.info("link file from {} to {}", inFile, inTmpName);
					FileOperate.linkFile(inFile, inTmpName, false);
				}
			} catch (Exception e) {
				logger.error("copy file from " + inFile + " to " + inTmpName + "error", e);
			}
		}
	}
	
	protected void moveSingleFileOut(String filePathTmp, String filePathOut) {
		filePathOut = convertAli2Loc(filePathOut, false);
		if (isRetainTmpFiles) {
			logger.info("move file from {} to {} ",  filePathTmp, filePathOut);
			FileOperate.moveFile(true, filePathTmp, filePathOut);
			/**
			 * 这里就直接move到storage即可，虽然说是move，实际上还是copy，只不过move的话会将
			 * 临时文件夹中的文件清空。
			 * 然后move结束后不将结果link回来，因为现在move出去的路径还是 task_result/.tmp./
			 * 如果cmd是正常结束的，会将结果从 task_result/.tmp./ move 至 task_result/ 下
			 * 那么这里将 task_result/.tmp./file 链接到 local/file 这个链接就会失效
			 * 
			 * 反正后面将storage数据移动到临时文件夹时，也是link进来，并不消耗内存
			 */
//			logger.info("link file from  " + filePathOut + "  to  " + filePathTmp);
//			FileOperate.linkFile(filePathOut, filePathTmp, false);
		} else {
			//TODO 这里可能全改为move会更好些
			FileOperate.moveFile(true, filePathTmp, filePathOut);
		}
		
		//aliyun 无法向 .inmap./ 文件夹中写入文件或软链接，该文件夹为只读文件夹
//		/**
//		 * 因为aliyun的输入是inmap，输出是outmap
//		 * 因此存在情况：
//		 * xml1 对 /home/.inmap./a.fasta建索引为 /home/.outmap./a.fasta.fai
//		 * xml 输入参数为  /home/.inmap./a.fasta 并且isCopyToTmp = false
//		 * 这时候 我们就要求 /home/.inmap./a.fasta.fai 也必须存在
//		 * 所以就要在这里把所有潜在的 setInOutput 的输出文件链接到其对应的 inmap中去
//		 */
//		String filePathOutInmap = convertAli2Loc(filePathOut, true);
//		for (String parentPath : setInOutput) {
//			if (filePathOutInmap.startsWith(parentPath)) {
//				FileOperate.linkFile(filePathOut, filePathOutInmap, false);
//				break;
//			}
//		}
		
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
		if (!ServiceEnvUtil.isCloudEnv()) {
			return path;
		}
		String pathLocal = PathDetailObjStorage.changeOsToLocal(path);
		if (pathLocal.startsWith(PathDetailObjStorage.getOsMountPathWithSep())) {		//	/home/novelbio/oss
			pathLocal = pathLocal.replaceFirst(PathDetailObjStorage.getOsMountPathWithSep(), "");
		} else {
			return path;
		}
		if (pathLocal.startsWith(IN_MAP)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(IN_MAP, ""), false);
		} else if (pathLocal.startsWith(OUT_MAP)) {
			pathLocal = FileOperate.removeSplashHead(pathLocal.replaceFirst(OUT_MAP, ""), false);
		}
		String head = isReadMap? IN_MAP : OUT_MAP;
		pathLocal = PathDetailObjStorage.getOsMountPathWithSep() + head + "/" + pathLocal;
		
		return pathLocal;
	}
	
	private boolean isInmapFile(String path) {
		if (!ServiceEnvUtil.isCloudEnv()) {
			return false;
		}
		String pathLocal = PathDetailObjStorage.changeOsToLocal(path);
		if (pathLocal.startsWith(PathDetailObjStorage.getOsMountPathWithSep())) {		//	/home/novelbio/oss
			pathLocal = pathLocal.replaceFirst(PathDetailObjStorage.getOsMountPathWithSep(), "");
		} else {
			return false;
		}
		return pathLocal.startsWith(IN_MAP);
	}
	
	/**
	 * 把/home/novelbio/.inmap./path/to/myfile 转成 oss://bucket/path/to/myfile
	 * 
	 * 
	 * @param path 注意path必须以挂载oss的路径开头
	 * @return
	 */
	public static String convertLoc2Obj(String pathLocal) {
		if (!ServiceEnvUtil.isCloudEnv()) {
			return pathLocal;
		}
		if (!pathLocal.startsWith(PathDetailObjStorage.getOsMountPathWithSep())) {
			return pathLocal;
		}
		
		pathLocal = pathLocal.substring(PathDetailObjStorage.getOsMountPathWithSep().length());
		if (pathLocal.startsWith(IN_MAP)) {
			pathLocal = pathLocal.substring(IN_MAP.length());
		} else if (pathLocal.startsWith(OUT_MAP)) {
			pathLocal = pathLocal.substring(OUT_MAP.length());
		}
		//注意此时pathLocal应该保留了头部的 "/"
		return PathDetailObjStorage.getSymbol() + "://" + PathDetailObjStorage.getBucket() + pathLocal;
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
