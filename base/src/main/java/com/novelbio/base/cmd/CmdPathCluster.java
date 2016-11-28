package com.novelbio.base.cmd;

import java.util.HashMap;
import java.util.Map;

/**
 * 场景: 在运行task加壳的script时，会连续运行多个task，
 * 并且每个task都会有输出文件，下一个task也会把上一个task的输出当成输入。
 * 
 * 目前上一个task会输出到临时文件夹，然后拷贝到最后的结果文件夹。
 * 下一个task会从结果文件夹中再把这个文件拷贝到临时文件夹中。我们
 * 希望这个文件拷出去后又拷贝回了同一个位置。这样我就可以很方便的
 * 设置跳过存在文件。
 * 
 * 举例: task1 产生文件 /home/novelbio/tmp/myresult/detail/info.txt
 * task1 把该文件拷贝到文件夹 /nbCloud/project1/task1/myresult/detail/info.txt
 * task2 需要该文件。则把该文件拷贝回 /home/novelbio/tmp/myresult/detail/info.txt
 * 
 * 这样在task2拷贝的时候，我只需要检查 /home/novelbio/tmp/myresult/detail/info.txt 是否存在，
 * 如果文件存然后大小一致，在我就可以跳过了。
 * @author zong0
 */
public class CmdPathCluster {
	
	Map<String, String> mapOut2TmpOut = new HashMap<>();
	
	/** 某个文件从tmp拷贝到输出文件夹中 */
	public void putTmpOut2Out(String tmpOut, String out) {
		mapOut2TmpOut.put(out, tmpOut);
	}
	
	/**
	 * 把输入文件拷贝到哪个临时文件夹中去
	 * @return
	 */
	public String getCopyToTmpPath(String inPath) {
		if (mapOut2TmpOut.containsKey(inPath)) {
			return mapOut2TmpOut.get(inPath);
		}
		//TODO 如果没有这个文件，我们就直接返回上级文件夹
	}
	
}
