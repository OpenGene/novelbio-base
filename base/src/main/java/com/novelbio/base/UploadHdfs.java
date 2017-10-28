package com.novelbio.base;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class UploadHdfs {
	private static final Logger logger = LoggerFactory.getLogger(UploadHdfs.class);
	public static void main(String[] args) {
		if (ArrayOperate.isEmpty(args)) {
			printHelp();
			System.exit(0);
		}
		
		Options opts = new Options();
		opts.addOption("source", true, "source");
		opts.addOption("target", true, "target");
		opts.addOption("iscover", true, "target");
		
		CommandLine cliParser = null;

		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			logger.error("error params:" + ArrayOperate.cmbString(args, " "));
			printHelp();
			System.exit(1);
		}
		String source = cliParser.getOptionValue("source", "");
		String target = cliParser.getOptionValue("target", "");
		boolean iscover = Boolean.parseBoolean(cliParser.getOptionValue("iscover", "false"));
		FileOperate.copyFileFolder(source, FileOperate.addSep(target) + FileOperate.getFileName(source), iscover);
	}
	
	public static void printHelp() {
		List<String> lsHelp = new ArrayList<>();
		lsHelp.add("java -jar uploadhdfs.jar --iscover false --source data/path --target hdfs_path");
		lsHelp.add("example:\n");
		lsHelp.add("java -jar uploadhdfs.jar -iscover false --source /home/oebiotech/data --target hdfs:/nbCloud/public/data/");
		for (String helpUnit : lsHelp) {
			System.err.println(helpUnit);
		}
	}
}
