package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 输入cmd，执行完毕后可以将结果输出到界面，目前cmd只支持英文，否则会出错 只要继承后重写process方法即可
 * 如果只是随便用用，那么调用doInBackground方法就好<p>
 * <b>管道只支持最后的一个 &gt; </b>
 * 例如 "bwa aaa bbb &gt; ccc"，此时会根据ccc的后缀，gz还是bz2，自动选择相应的压缩流<br>
 * <b>不支持这种</b> "bwa aaa bbb | grep sd &gt; ccc"
 * @author zong0jie
 */
public class CmdOperateMvCp extends CmdOperate {
	public CmdOperateMvCp(List<String> lsCmd) {
		super(lsCmd);
	}
	@Override
	protected void doInBackgroundB() throws Exception {
		String[] cmdRun = cmdOrderGenerator.getCmdExeStr(cmdMoveFile);
		if (cmdRun == null || cmdRun.length < 2
				|| (!cmdRun[0].equals("mv") && !cmdRun[0].equals("cp") && !cmdRun[0].equals("mkdir"))) {
			super.doInBackgroundB();
			return;
		}
		
		finishFlag.start();
		cmdRun = cmdOrderGenerator.getRunCmd(cmdMoveFile);
		List<String> lsCmd = ArrayOperate.converArray2List(cmdRun);
		try {
			runMvAndCp(lsCmd);
			runMkdir(lsCmd);
			finishFlag.setFlag(0);
		} catch (Exception e) {
			finishFlag.setFlag(1);
			throw e;
		}
	}
	/**
	 * 如果是hadoop环境且如果命令是mv或cp，则调用FileOperate进行，因为直接操作hdfs很可能会导致io问题
	 * @param lsCmdStr
	 * @return
	 */
	@VisibleForTesting
	public static boolean runMvAndCp(List<String> lsCmdStr) {
//		if (!ServiceEnvUtil.isHadoopEnvRun()) return false;
		if (!lsCmdStr.get(0).equals("mv") && !lsCmdStr.get(0).equals("cp"))  return false;
		
		String cmd = ArrayOperate.cmbString(lsCmdStr, " ");
		boolean isCover = true;

		for (String string : lsCmdStr) {
			if (string.equalsIgnoreCase("-n") || string.equalsIgnoreCase("--no-clobber")) {
				isCover = false;
				break;
			}
		}
		
		//获取多个需要移动的文件。
		//譬如 mv a.txt b.txt c.txt /home/novelbio/d/
		//则获取 a.txt b.txt c.txt
		List<String> lsFileNeedMvOrCp = new ArrayList<>();
		boolean isFile = false;
		for (int i = 1; i < lsCmdStr.size() - 1; i++) {
			String fileName = lsCmdStr.get(i);
			if (fileName.startsWith("-")) {
				if (isFile) {
					throw new ExceptionCmd("cmd error: " + cmd);
				}
				continue;
			}
			isFile = true;
			lsFileNeedMvOrCp.add(fileName);
		}
		
		//创建输出文件夹
		String outFile = lsCmdStr.get(lsCmdStr.size() - 1);
		
		/** 仅需考虑hdfs，如果是cos则不需要考虑这个问题 */
		outFile = FileOperate.convertToHdfs(outFile);
		if (StringOperate.isRealNull(outFile)) {
			throw new ExceptionCmd("cannot move or copy file to null: " + cmd);
		}
		
		//是否拷贝到输入文件同一级的文件夹下
		//true cp /home/novelbio/  /mytest/aaa/
		//aaa 存在则为true，结果为/mytest/aaa/novelbio
		//aaa 不存在则为false，结果为 /mytest/aaa/* 其中*为novelbio文件夹中的内容
		//TODO 以上规则有待检查是否与linux命令一致
		boolean isOutFolder = FileOperate.isFileDirectory(outFile);
		if (isOutFolder) outFile = FileOperate.addSep(outFile);
		
		if (lsCmdStr.get(0).equals("mv")) {
			for (String file : lsFileNeedMvOrCp) {
				file = FileOperate.convertToHdfs(file);
				if (!FileOperate.isFileExistAndBigThan0(file)) {
					continue;
				}
				if (isOutFolder) {
					FileOperate.moveFile(isCover, file, outFile + FileOperate.getFileName(file));
				} else {
					FileOperate.moveFile(isCover, file, outFile);
				}
			}
		}
		
		if (lsCmdStr.get(0).equals("cp")) {
			for (String file : lsFileNeedMvOrCp) {
				file = FileOperate.convertToHdfs(file);
				if (!FileOperate.isFileExistAndBigThan0(file)) {
					continue;
				}
				if (isOutFolder) {
					FileOperate.copyFileFolder(file, outFile + FileOperate.getFileName(file), isCover);
				} else {
					FileOperate.copyFileFolder(file, outFile, isCover);
				}
			}
		}

		return true;
	}
	
	/**
	 * 如果是hadoop环境且如果命令是mv或cp，则调用FileOperate进行，因为直接操作hdfs很可能会导致io问题
	 * @param lsCmdStr
	 * @return
	 */
	@VisibleForTesting
	protected static boolean runMkdir(List<String> lsCmdStr) {
//		if (!ServiceEnvUtil.isHadoopEnvRun()) return false;
		if (!lsCmdStr.get(0).equals("mkdir"))  return false;
		
		String cmd = ArrayOperate.cmbString(lsCmdStr, " ");
		
		//获取多个需要移动的文件。
		//譬如 mv a.txt b.txt c.txt /home/novelbio/d/
		//则获取 a.txt b.txt c.txt
		List<String> lsFolderNeedCreate = new ArrayList<>();
		boolean isFile = false;
		for (int i = 1; i <= lsCmdStr.size() - 1; i++) {
			String folderName = lsCmdStr.get(i);
			if (folderName.startsWith("-")) {
				if (isFile) {
					throw new ExceptionCmd("cmd error: " + cmd);
				}
				continue;
			}
			isFile = true;
			lsFolderNeedCreate.add(folderName);
		}
		for (String folder : lsFolderNeedCreate) {
			folder = FileOperate.convertToHdfs(folder);
			FileOperate.createFolders(folder);
		}
		return true;
	}
}

