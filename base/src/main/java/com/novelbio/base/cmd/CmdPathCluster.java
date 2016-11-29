package com.novelbio.base.cmd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	/** key 实际输出文件夹
	 * value 该实际输出文件夹当时的临时文件夹
	 */
	Map<String, String> mapOutPath2TmpOutPath = new HashMap<>();
	
	/** 某个文件从tmp拷贝到输出文件夹中 */
	public void putTmpOut2Out(String tmpOut, String out) {
		mapOutPath2TmpOutPath.put(FileOperate.getParentPathNameWithSep(out), FileOperate.getParentPathNameWithSep(tmpOut));
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
			if (mapOutPath2TmpOutPath.containsKey(path)) {
				mapPath2TmpPath.put(path, mapOutPath2TmpOutPath.get(path));
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
	
	/** 获得前面已经生成过的结果文件
	 * 譬如给定的文件路径是 /nbCloud/public/project1/task1/mypath/to/the/file.txt
	 * 然后 mapOutPath2TmpOutPath 中有路径 nbCloud/public/project1/task1/ : /home/novelbio/tmp/mypath/
	 * 则把/nbCloud/public/project1/task1/mypath/to/the/ 对应到 /home/novelbio/tmp/mypath/to/the
	 * @param inputPath
	 * @return
	 */
	private String getTmpPathAlreadyExist(String inputPath) {
		while (!StringOperate.isRealNull(inputPath) && inputPath.length() > 10) {
			
		}
	}
	
	private Map<String, String> getMapPath2TmpPath(Set<String> setPath, String pathTmp) {
		Map<String, String> mapPath2TmpPath = new HashMap<>();
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
	

	
}
