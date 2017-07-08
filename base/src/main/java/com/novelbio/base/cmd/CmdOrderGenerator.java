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
	
	/** 是否在外部获取stdout的流文件, <b>默认为false</b>*/
	boolean isGetStdoutStream = false;
	/** 截获标准输出流的输出文件名 */
	String saveFilePath = null;
	/** 输出文件是否为txt，如果命令中含有 >，则认为输出的可能不是txt，为二进制 */
	boolean isJustDisplayStd = false;
	
	/** 是否在外部获取stderr的流文件，同 {@link #isGetStdoutStream} */
	boolean isGetStderrStream = true;
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
	/** 结束后删除临时文件夹，仅当tmp文件夹为随机生成的时候才会删除 */
	public void deleletTmpPath() {
		cmdPath.deleteTmpPath();
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
	
	/** 如果命令存在 ">"或"1>"，则返回之后根的文件名 */
	public String getSaveStdPath() {
		return saveFilePath;
	}
	
	/** 如果命令存在 ">"或"1>"，则返回之后根的文件名的临时文件 */
	protected String getSaveStdTmp() {
		if (saveFilePath == null) {
			return null;
		}
		if (!isGetStdoutStream) {
			return FileOperate.changeFileSuffix(saveFilePath, "_tmp", null);
		} else {
			return saveFilePath;
		}
	}
	
	/** 如果命令存在"2>"，则返回之后根的文件名  */
	public String getSaveErrPath() {
		return saveErrPath;
	}
	/** 如果命令存在 "2>"，则返回之后根的文件名的临时文件 */
	protected String getSaveErrTmp() {
		if (saveErrPath == null) {
			return null;
		}
		if (!isGetStderrStream) {
			return FileOperate.changeFileSuffix(saveErrPath, "_tmp", null);
		} else {
			return saveErrPath;
		}
	}
	
	/** 是否从cmdOperate中获得标准输出流，自行保存 */
	protected void setIsGetStdoutStream(boolean isSaveStdFile) {
		this.isGetStdoutStream = isSaveStdFile;
	}
	
	/** 是否 从cmdOperate中获得标准错误流，自行保存 */
	protected void setIsGetStderrStream(boolean isSaveErrFile) {
		this.isGetStderrStream = isSaveErrFile;
	}
	
	public void moveStdFiles() {
		if (!isGetStdoutStream && getSaveStdTmp() != null) {
			FileOperate.moveFile(true, getSaveStdTmp(), saveFilePath);
		}
		if (!isGetStderrStream && getSaveErrPath() != null) {
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
	/** 在cmd运行前，将输入文件拷贝到临时文件夹下 */
	public void copyFileIn() {
		cmdPath.copyFileInTmp();
	}
	/** 记录临时文件夹下有多少文件，用于后面删除时跳过 */
	public void recordFilesWhileRedirectOutToTmp() {
		cmdPath.recordFilesWhileRedirectOutToTmp();
	}
	
	/** 必须先调用{@link #copyFileInAndRecordFiles()}，
	 * 等运行cmd结束后还需要调用{@link #moveFileOut()} 来完成运行 */
	public String[] getRunCmd() {
		cmdPath.generateTmPath();
		return generateRunCmd(true);
	}
		
	/** 返回实际运行的cmd string数组
	 * 必须设定好lcCmd后才能使用
	 * @param redirectStdAndErr 是否重定向标准输出和错误输出，如果只是获得命令，那不需要重定向<br>
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
					continue;
				} else if (tmpCmd.equals("2>")) {
					errOut = true;
					continue;
				} else if (tmpCmd.equals("<")) {
					stdIn = true;
					continue;
				}
			}
			
			tmpCmd = convertCmdTmp.convertSubCmd(tmpCmd);
			
			if (stdOut) {
				if (!isGetStdoutStream && StringOperate.isRealNull(saveFilePath)) {
					saveFilePath = tmpCmd;
				}
				stdOut = false;
				continue;
			} else if (errOut) {
				if (!isGetStderrStream && StringOperate.isRealNull(saveErrPath)) {
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
