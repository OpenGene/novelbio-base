package com.novelbio.base.cmd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 场景: 在运行task加壳的script时，会连续运行多个task，<br>
 * 并且每个task都会有输出文件，下一个task也会把上一个task的输出当成输入。<br>
 * <br>
 * 目前上一个task会输出到临时文件夹，然后拷贝到最后的结果文件夹。<br>
 * 下一个task会从结果文件夹中再把这个文件拷贝到临时文件夹中。我们<br>
 * 希望这个文件拷出去后又拷贝回了同一个位置。这样我就可以很方便的<br>
 * 设置跳过存在文件。<br>
 * <br>
 * 举例: task1 产生文件 /home/novelbio/tmp/myresult/detail/info.txt<br>
 * task1 把该文件拷贝到文件夹 /nbCloud/project1/task1/myresult/detail/info.txt<br>
 * task2 需要该文件。则把该文件拷贝回 /home/novelbio/tmp/myresult/detail/info.txt<br>
 * <br>
 * 这样在task2拷贝的时候，我只需要检查 /home/novelbio/tmp/myresult/detail/info.txt 是否存在，<br>
 * 如果文件存然后大小一致，在我就可以跳过了。<br>
 * @author zong0jie
 */
public class CmdPathCluster {
	private static final Logger logger = LoggerFactory.getLogger(CmdPathCluster.class);
	
	/** 是否为阿里云 */
	boolean isAliyun;
	
	/** key 实际输出文件夹
	 * value 该实际输出文件夹当时的临时文件夹，以 "/" 结尾
	 */
	Map<String, String> mapOutPath2TmpOutPath = new ConcurrentHashMap<>();
	
	public CmdPathCluster(boolean isAliyun) {
		this.isAliyun = isAliyun;
	}
	
	/** 目前仅用于打印日志 */
	public void printLogMapOutPath2TmpOutPath(boolean isDebug) {
		if (isDebug) {
			logger.debug("start print CmdPathCluster MapOutPath2TmpOutPath");
		} else {
			logger.info("start print CmdPathCluster MapOutPath2TmpOutPath");
		}
		
		for (String outPath : mapOutPath2TmpOutPath.keySet()) {
			if (isDebug) {
				logger.debug("{}={}", outPath, mapOutPath2TmpOutPath.get(outPath));
			} else {
				logger.info("{}={}", outPath, mapOutPath2TmpOutPath.get(outPath));
			}
		}
	}
	
	/** 某个文件从tmp拷贝到输出文件夹中 */
	public synchronized void putTmpOut2Out(String tmpOut, String out) {
		String outTmp = FileOperate.getParentPathNameWithSep(tmpOut);
		String outReal = FileOperate.getParentPathNameWithSep(out);
		mapOutPath2TmpOutPath.put(outReal, outTmp);
		if (isAliyun) {
			mapOutPath2TmpOutPath.put(CmdPathAli.convertAli2Loc(outReal, true), outTmp);
		}
	}
	
	/**
	 * 给定一系列的输出文件路径，返回这些输出文件所对应的临时文件夹
	 * @param setFiles
	 * @param pathTmp
	 * @return
	 */
	protected Map<String, String> getMapOutPath2TmpPath(Set<String> setFiles, String pathTmp) {
		Set<String> setPath = mergeParentPath(setFiles);
		return getMapPath2TmpPath(setPath, pathTmp);
	}
	
	/**
	 * 给定一系列的输入文件，返回这些输入文件所应该对应的临时文件夹
	 * @param setFiles
	 * @param pathTmp
	 * @return
	 */
	protected Map<String, String> getMapInPath2TmpPath(Set<String> setFiles, String pathTmp) {
		Set<String> setPath = mergeParentPath(setFiles);
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		
		Set<String> setPathNotExist = new HashSet<>();
		for (String path : setPath) {
			//如果前面的task已经记录了该文件夹的存储路径，则拷贝到同一个文件夹下
			//这样相同文件名的就可以跳过了
			String tmpPath = getTmpPathAlreadyExist(path);
			if (tmpPath != null) {
				mapPath2TmpPath.put(path, tmpPath);
			} else {
				setPathNotExist.add(path);
			}
		}
		mapPath2TmpPath.putAll(getMapPath2TmpPath(setPathNotExist, pathTmp));
		return mapPath2TmpPath;
	}
	
	/**
	 * 合并相同输入文件的父文件夹
	 * @param setFiles
	 * @return
	 */
	private Set<String> mergeParentPath(Set<String> setFiles) {
		Set<String> setPath = new HashSet<>();
		for (String inFileName : setFiles) {
			String inPath = FileOperate.getParentPathNameWithSep(inFileName);
			setPath.add(inPath);
		}
		return setPath;
	}
	
	/** 获得前面已经生成过的结果文件<br>
	 * 譬如给定的文件路径是 /nbCloud/public/project1/task1/mypath/to/the/file.txt<br>
	 * 然后 mapOutPath2TmpOutPath 中有路径 nbCloud/public/project1/task1/ : /home/novelbio/tmp/mypath/<br>
	 * 则把/nbCloud/public/project1/task1/mypath/to/the/ 对应到 /home/novelbio/tmp/mypath/to/the
	 * @param inputPath 输入的文件夹，以"/"结尾
	 * @return
	 */
	@VisibleForTesting
	protected String getTmpPathAlreadyExist(String inputPath) {
		//inputPath=/media/nbfs/nbCloud/public/task/mytest/result
		//map: /media/nbfs/nbCloud/public/=/home/novelbio/tmp/
		//loop...
		//resultPath=/home/novelbio/tmp/task/mytest/result
		//remainPath=task/mytest/result
		String lastPath = null;//上一层文件夹
		String resultPath = null;//对照得到的临时文件夹
		String remainPath = "";//之后的路径
		while (!StringOperate.isRealNull(inputPath) && !StringOperate.isEqual(inputPath, lastPath)) {
			if (mapOutPath2TmpOutPath.containsKey(inputPath)) {
				resultPath = mapOutPath2TmpOutPath.get(inputPath) + remainPath;
				break;
			}
			lastPath = inputPath;
			//获取当前文件的文件名，如果以"/"结尾，也把这个加上
			String tmpFileName = FileOperate.getFileName(inputPath);
			if (inputPath.endsWith("/") || inputPath.endsWith("\\")) {
				tmpFileName = FileOperate.addSep(tmpFileName);
			}
			remainPath = tmpFileName + remainPath;
			inputPath = FileOperate.getParentPathNameWithSep(inputPath);
		}
		return resultPath;
	}
	
	private Map<String, String> getMapPath2TmpPath(Set<String> setPath, String pathTmp) {
		//输入的set中 /home/novelbio/oss/.inmap./test 和 /home/novelbio/oss/.outmap./test 是两个不同的文件夹
		//如果直接跑会产生 一个 test文件夹， 一个test1文件夹
		//而实际上这两个应该只产生一个test文件夹，所以在这里做一个合并的工作
		ArrayListMultimap<String, String> mapPath2LsConvertPath = ArrayListMultimap.create();
		for (String path : setPath) {
			mapPath2LsConvertPath.put(CmdPathAli.convertAli2Loc(path, true), path);
		}
		//====================
		Map<String, String> mapPath2TmpPath = new HashMap<>();
		Set<String> setPathNoDup = new HashSet<>();
		for (String path : mapPath2LsConvertPath.keySet()) {
			String parentPath = FileOperate.getFileName(path);
			String parentPathFinal = parentPath;
			int i = 1;//防止产生同名文件夹的措施
			while (setPathNoDup.contains(parentPathFinal)) {
				parentPathFinal = parentPath + i++;
			}
			setPathNoDup.add(parentPathFinal);
			String tmpPathThis = pathTmp + parentPathFinal+ FileOperate.getSepPath();
			List<String> lsPaths = mapPath2LsConvertPath.get(path);
			for (String pathRaw : lsPaths) {
				mapPath2TmpPath.put(pathRaw, tmpPathThis);
			}
		}
		return mapPath2TmpPath;
	}
	
}
