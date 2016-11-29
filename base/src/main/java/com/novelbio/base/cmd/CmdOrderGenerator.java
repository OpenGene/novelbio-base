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
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdGetFileName;
import com.novelbio.base.cmd.ConvertCmd.ConvertCmdTmp;
import com.novelbio.base.cmd.ConvertCmd.ConvertHdfs;
import com.novelbio.base.cmd.ConvertCmd.ConvertOss;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.util.ServiceEnvUtil;

/**
 * 输入cmdlist，将其整理为相应的cmd string array<br>
 * 并且移动输入文件到临时文件夹<br>
 * 以及将输出文件移到目标文件夹<br>
 * 同时还产生相应的cmd命令参数
 * @author zong0jie
 *
 */
//TODO 考虑兼容 "<<"
public class CmdOrderGenerator {
	private static final Logger logger = LoggerFactory.getLogger(CmdOrderGenerator.class);
	
	List<String> lsCmd = new ArrayList<>();
	
	CmdPath cmdPath;
	
	/** 是否保存stdout文件, <b>默认为true</b><br>
	 * stdout的保存有几种：<br>
	 * 1. 在cmd命令中添加 > 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveFilePath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	boolean isSaveStdFile = true;
	/** 截获标准输出流的输出文件名 */
	String saveFilePath = null;
	/** 输出文件是否为txt，如果命令中含有 >，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayStd = false;
	
	/** 是否保存stderr文件，同 {@link #isSaveStdFile} */
	boolean isSaveErrFile = true;
	/** 截获标准错误流的错误文件名 */
	String saveErrPath = null;
	/** 输出错误文件是否为txt，如果命令中含有 2>，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayErr = false;
	
	String stdInput = null;
	
	/** 是否已经生成了临时文件夹，生成一次就够了 */
	boolean isGenerateTmpPath = false;
	
	/** 是否将hdfs的路径，改为本地路径
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 * 
	 * 也有把oss或者bos路径修改为本地路径
	 */
	boolean isConvertHdfs2Loc = true;	
	
	public CmdOrderGenerator(boolean isLocal) {
		cmdPath = CmdPath.getInstance(isLocal);
	}
	/** 设定复制输入输出文件所到的临时文件夹 */
	public void setTmpPath(String tmpPath) {
		cmdPath.setTmpPath(tmpPath);
	}
	public String getTmpPath() {
		return cmdPath.getTmpPath();
	}
	
	/** 临时文件夹中的文件是否删除 */
	public void setRetainTmpFiles(boolean isRetainTmpFiles) {
		cmdPath.setRetainTmpFiles(isRetainTmpFiles);
	}
	public void setCmdPathCluster(CmdPathCluster cmdPathCluster) {
		cmdPath.setCmdPathCluster(cmdPathCluster);
	}
	/** 如果为null就不加入 */
	public void addCmdParam(String param) {
		if (!StringOperate.isRealNull(param)) {
			lsCmd.add(param);
		}
	}
	
	public void setJustDisplayStd(boolean isJustDisplayStd) {
		this.isJustDisplayStd = isJustDisplayStd;
	}
	public void setJustDisplayErr(boolean isJustDisplayErr) {
		this.isJustDisplayErr = isJustDisplayErr;
	}
	/** 输出文件是否为txt格式的
	 * 如果命令中含有 2>，则认为输出的可能不是txt，为二进制
	 * @return
	 */
	public boolean isJustDisplayErr() {
		return isJustDisplayErr;
	}
	/** 输出的err文件是否为txt格式的
	 * 如果命令中含有 >，则认为输出的可能不是txt，为二进制
	 * @return
	 */
	public boolean isJustDisplayStd() {
		return isJustDisplayStd;
	}
	
	public void setLsCmd(List<String> lsCmd) {
		this.lsCmd = lsCmd;
	}
	
	/** 是否将hdfs的路径，改为本地路径，<b>默认为true</b><br>
	 * 如将 /hdfs:/fseresr 改为 /media/hdfs/fseresr<br>
	 * 只有类似varscan这种我们修改了代码，让其兼容hdfs的程序才不需要修改
	 */
	public void setConvertHdfs2Loc(boolean isConvertHdfs2Loc) {
		this.isConvertHdfs2Loc = isConvertHdfs2Loc;
	}
	
	/** 是否将输入文件拷贝到临时文件夹，默认为false */
	public void setRedirectInToTmp(boolean isRedirectInToTmp) {
		cmdPath.setRedirectInToTmp(isRedirectInToTmp);
	}
	/** 是否将输出先重定位到临时文件夹，再拷贝回实际文件夹，默认为false */
	public void setRedirectOutToTmp(boolean isRedirectOutToTmp) {
		cmdPath.setRedirectOutToTmp(isRedirectOutToTmp);
	}

	/**
	 * 添加输入文件路径的参数，配合{@link #setRedirectInToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param input 输入文件的哪个参数
	 */
	public void addCmdParamInput(String input) {
		cmdPath.addCmdParamInput(input);
	}
	/**
	 * 添加输出文件路径的参数，配合{@link #setRedirectOutToTmp(boolean)}，可设定为将输出先重定位到临时文件夹，再拷贝回实际文件夹
	 * @param output 输出文件的哪个参数，如果输入参数类似 "--outPath=/hdfs:/test.fa"，这里填写 "/hdfs:/test.fa"
	 */
	public void addCmdParamOutput(String output) {
		cmdPath.addCmdParamOutput(output);
	}
	
	/** 如果param为null则返回 */
	public void addCmdParam(List<String> lsCmd) {
		if (lsCmd == null) {
			return;
		}
		String[] param = lsCmd.toArray(new String[0]);
		addCmdParam(param);
	}
	/** 如果param为null则返回 */
	public void addCmdParam(String[] param) {
		if (param == null) {
			return;
		}
		for (String string : param) {
			if (StringOperate.isRealNull(string)) {
				throw new ExceptionCmd("param contains null");
			}
			lsCmd.add(string);
		}
	}
	
	public String getStdInFile() {
		return stdInput;
	}
	
	/** 设定最后保存的标准输出流文件名，注意会设定 {@link #setIsSaveStdFile(boolean)}为true */
	public void setSaveFilePath(String saveFilePath) {
		this.saveFilePath = saveFilePath;
		this.isSaveStdFile = true;
	}
	public String getSaveStdPath() {
		return saveFilePath;
	}
	
	protected String getSaveStdTmp() {
		if (saveFilePath == null) {
			return null;
		}
		if (isSaveStdFile) {
			return FileOperate.changeFileSuffix(saveFilePath, "_tmp", null);
		} else {
			return saveFilePath;
		}
	}

	/** 设定最后保存的标准错误流文件名，注意会设定 {@link #setIsSaveErrFile(boolean)}为true */
	public void setSaveErrPath(String saveErrPath) {
		this.saveErrPath = saveErrPath;
		this.isSaveErrFile = true;
	}

	public String getSaveErrPath() {
		return saveErrPath;
	}
	protected String getSaveErrTmp() {
		if (saveErrPath == null) {
			return null;
		}
		if (isSaveErrFile) {
			return FileOperate.changeFileSuffix(saveErrPath, "_tmp", null);
		} else {
			return saveErrPath;
		}
	}
	
	/** 是否保存stdout文件，<b>默认为true</b><br>
	 * stdout的保存有几种：<br>
	 * 1. 在cmd命令中添加 > 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveFilePath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	protected void setIsSaveStdFile(boolean isSaveStdFile) {
		this.isSaveStdFile = isSaveStdFile;
	}
	
	/** 是否保存stderr文件， <b>默认为true</b><br>
	 * stderr的保存有几种：<br>
	 * 1. 在cmd命令中添加 2> 然后得到标准输出文件<br>
	 * 2. 通过 {@link #setSaveErrPath(String)} 外部设置stdout的文件名<br>
	 * 3. 从cmdOperate中获得标准输出流，自行保存<br>
	 * 其中1,2需要保存std文件，并且在最后需要把std的临时文件修改为最终文件<br>
	 * 3不需要保存std文件<br>
	 *  */
	protected void setIsSaveErrFile(boolean isSaveErrFile) {
		this.isSaveErrFile = isSaveErrFile;
	}
	
	public void moveStdFiles() {
		if (isSaveStdFile && getSaveStdTmp() != null) {
			FileOperate.moveFile(true, getSaveStdTmp(), saveFilePath);
		}
		if (isSaveErrFile && getSaveErrPath() != null) {
			FileOperate.moveFile(true, getSaveErrTmp(), saveErrPath);
		}
	}
	
	/** 返回执行的具体cmd命令，不会将文件路径删除，仅给相对路径 */
	public String[] getCmdExeStr() {
		return getCmdExeStrModify();
	}
	
	/** 返回执行的具体cmd命令，会将文件路径删除，仅给相对路径 */
	public String[] getCmdExeStrModify() {
		cmdPath.generateTmPath();
		ConvertCmdGetFileName convertGetFileName = new ConvertCmdGetFileName();
		return convertGetFileName.convertCmd(generateRunCmd(false));
	}
	
	public void generateTmPath() {
		cmdPath.generateTmPath();
	}
	
	/** 返回执行的具体cmd命令，实际cmd命令 */
	public String[] getCmdExeStrReal() {
		cmdPath.generateTmPath();
		return generateRunCmd(false);
	}

	/** 在cmd运行前，将输入文件拷贝到临时文件夹下
	 * 同时记录临时文件夹下有多少文件，用于后面删除时跳过 */
	public void copyFileInAndRecordFiles() {
		cmdPath.copyFileInAndRecordFiles();
	}
	
	/** 必须先调用{@link #copyFileInAndRecordFiles()}，
	 * 等运行cmd结束后还需要调用{@link #moveFileOut()} 来完成运行 */
	public String[] getRunCmd() {
		cmdPath.generateTmPath();
		return generateRunCmd(true);
	}
		
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @param redirectStdErr 是否重定向标准输出和错误输出，如果只是获得命令，那不需要重定向<br>
	 * 如果是实际执行的cmd，就需要重定向
	 * @return
	 */
	protected String[] generateRunCmd(boolean redirectStdAndErr) {
		List<String> lsReal = new ArrayList<>();
		boolean stdOut = false;
		boolean errOut = false;
		boolean stdIn = false;
		saveFilePath = null;
		saveErrPath = null;
		
		ConvertCmdTmp convertCmdTmp = cmdPath.generateConvertCmdTmp();
		ConvertCmd convertOs2Local = getConvertOs2Local();
		
		for (String tmpCmd : lsCmd) {
			if (redirectStdAndErr) {
				if (tmpCmd.equals(">")  || tmpCmd.equals("1>")) {
					stdOut = true;
					setJustDisplayStd(false);
					continue;
				} else if (tmpCmd.equals("2>")) {
					errOut = true;
					setJustDisplayErr(false);
					continue;
				} else if (tmpCmd.equals("<")) {
					stdIn = true;
					continue;
				}
			}
			
			tmpCmd = convertCmdTmp.convertSubCmd(tmpCmd);
			
			if (stdOut) {
				if (isSaveStdFile && StringOperate.isRealNull(saveFilePath)) {
					saveFilePath = tmpCmd;
				}
				stdOut = false;
				continue;
			} else if (errOut) {
				if (isSaveErrFile && StringOperate.isRealNull(saveErrPath)) {
					saveErrPath = tmpCmd;
				}
				errOut = false;
				continue;
			} else if (stdIn) {
				if (StringOperate.isRealNull(stdInput)) {
					stdInput = tmpCmd;
				}
				stdIn = false;
				continue;
			}
			if (isConvertHdfs2Loc) {
				tmpCmd = convertOs2Local.convertSubCmd(tmpCmd);
			}
			lsReal.add(tmpCmd);
		}
		String[] realCmd = lsReal.toArray(new String[0]);
		return realCmd;
	}
	
	protected ConvertCmd getConvertOs2Local() {
		return ServiceEnvUtil.isAliyunEnv() ? new ConvertOss() : new ConvertHdfs();
	}
	
	/**
	 * 将tmpPath文件夹中的内容全部移动到resultPath中 */
	public void moveFileOut() {
		cmdPath.moveFileOut();
	}
	
	/** 删除中间文件，会把临时的input文件也删除 */
	public void deleteTmpFile() {
		cmdPath.deleteTmpFile();
	}
	
	public void clearLsCmd() {
		lsCmd.clear();
		cmdPath.clearLsCmd();
	}

}
