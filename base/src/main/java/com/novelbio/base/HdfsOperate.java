package com.novelbio.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 用于hdfs上传和下载的类
 * 主要是私有云用户不方便直接操作  /media/nbfs 所以提供这个类给客户，让客户可以直接操作 hdfs
 * @author zong0jie
 * @data 2017年12月26日
 */
public class HdfsOperate {
	private static final Logger logger = LoggerFactory.getLogger(HdfsOperate.class);
	public static void main(String[] args) {
		if (ArrayOperate.isEmpty(args)) {
			printHelp();
			System.exit(0);
		}
		String argsAll = ArrayOperate.cmbString(args, " ").trim();
		argsAll.replace("\t", " ");
		argsAll.replace("  ", " ");
		args = argsAll.split(" ");
		
		if (!args[0].equals("cp") && !args[0].equals("mv")) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			printHelp();
			System.exit(1);
		}
		
		Options opts = new Options();
		opts.addOption("n", false, "isNotCover");
		
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			printHelp();
			System.exit(1);
		}
		boolean isCover = !cliParser.hasOption("n");
		List<String> lsSourcePath = new ArrayList<>();
		for (int i = 0; i < args.length-1; i++) {
			String path = args[i];
			if (path.startsWith("-")) {
				continue;
			}
			lsSourcePath.add(FileHadoop.convertToHdfsPath(path));
		}
		String targetPath = FileHadoop.convertToHdfsPath(args[args.length - 1]);
		if (args[0].equals("rm")) {
			lsSourcePath.add(targetPath);
			rm(lsSourcePath);
			System.exit(0);
		}
		if (lsSourcePath.size() > 1 && !FileOperate.isFileDirectory(targetPath)) {
			logger.error("targetPath " + targetPath + " is not a directroy!");
			System.exit(1);
		}
		boolean isCp = args[0].equals("cp");
		
		for (String sourcePath : lsSourcePath) {
			cpOrMv(isCp, sourcePath, targetPath, isCover);
		}
	}
	
	private static void rm(List<String> lsFiles) {
		for (String file : lsFiles) {
			String parentPath = FileOperate.getParentPathNameWithSep(file);
			String fileName = FileOperate.getFileName(file);
			List<String> lsFileNamesDel = FileOperate.getLsFoldFileName(parentPath, fileName);
			for (String fileDel : lsFileNamesDel) {
				logger.info("rm file " + fileDel);
				FileOperate.deleteFileFolder(fileDel);
			}
		}
	}
	
	private static void cpOrMv(boolean isCp, String sourcePath, String targetPath, boolean isCover) {
		String parentPath = FileOperate.getParentPathNameWithSep(sourcePath);
		String fileName = FileOperate.getFileName(sourcePath);
		List<String> lsSrcFileNames = FileOperate.getLsFoldFileName(parentPath, fileName);
		if (lsSrcFileNames.size() == 1) {
			if (FileOperate.isFileDirectory(targetPath)) {
				cpOrMvSingle(isCp, lsSrcFileNames.get(0), FileOperate.addSep(targetPath) + FileOperate.getFileName(lsSrcFileNames.get(0)), isCover);
			} else {
				cpOrMvSingle(isCp, lsSrcFileNames.get(0), targetPath, isCover);
			}
		}
		if (lsSrcFileNames.size() > 1) {
			if (!FileOperate.isFileDirectory(targetPath)) {
				logger.error("targetPath " + targetPath + " is not a directroy!");
				System.exit(1);
			}
			for (String srcFiles : lsSrcFileNames) {
				cpOrMvSingle(isCp, srcFiles, FileOperate.addSep(targetPath) + FileOperate.getFileName(srcFiles), isCover);
			}
		}
	}
	
	private static void cpOrMvSingle(boolean isCp, String source, String target, boolean isCover) {
		if (isCp) {
			logger.info("cp file from {} to {}", source, target);
			FileOperate.copyFileFolder(source, target, isCover);
		} else {
			logger.info("mv file from {} to {}", source, target);
			FileOperate.moveFile(isCover, source, target);
		}
	}
	
	public static void printHelp() {
		List<String> lsHelp = new ArrayList<>();
		lsHelp.add("java -jar hdfsopt.jar mv src trg");
		lsHelp.add("java -jar hdfsopt.jar cp src trg");
		lsHelp.add("java -jar hdfsopt.jar rm trg");
		lsHelp.add("-n\tNot cover exist file\n");
		lsHelp.add("example:");
		lsHelp.add("java -jar hdfsopt.jar cp /home/oebiotech/data/*.fq hdfs:/nbCloud/public/data");
		lsHelp.add("java -jar hdfsopt.jar mv /home/oebiotech/data/*.fq hdfs:/nbCloud/public/data");
		lsHelp.add("java -jar hdfsopt.jar mv hdfs:/nbCloud/public/data/*.fq /home/novelbio/data/");
		lsHelp.add("\nnot support parentpath contains regular expressions, so the cmd below will be error:\n");
		lsHelp.add("java -jar hdfsopt.jar mv hdfs:/nbCloud/public/data/*/1.fq /home/novelbio/data/");
		lsHelp.add("java -jar hdfsopt.jar cp hdfs:/nbCloud/public/data/*/*.fq /home/novelbio/data/");
		lsHelp.add("java -jar hdfsopt.jar rm hdfs:/nbCloud/public/data/*/*.fq");

		for (String helpUnit : lsHelp) {
			System.err.println(helpUnit);
		}
	}
}
