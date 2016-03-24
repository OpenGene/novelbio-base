package com.novelbio.base.cmd;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdPath.ConvertCmdGetFileName;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtil.class)
public class TestCmdPath2 {
	String tmpPath = PathDetail.getTmpPathWithSep();
	
	@PrepareForTest(DateUtil.class)
	@Test
	public void testCmd() {
//		CmdPath.tmpPath = "/home/novelbio/tmp";
		DateUtil dateUtil = new DateUtil();
		PowerMockito.mockStatic(DateUtil.class);
		PowerMockito.when(DateUtil.getDateAndRandom()).thenReturn("2015-03-04-2211");
		
		cmd1();
		cmd2();
		cmd3();
		cmd4();
		cmd5();
		cmd6();
		cmd7();
		cmd8();
		cmdConvertHdfs();
		cmdConvertGetFileName();
	}
	
	private void cmd1() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput("src/test/resources/cmd", false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		assertEquals("bwa-index", ss[0]);

		assertEquals(PathDetail.getRandomWithSep(tmpPath, "resources1") + "testTrinity.fa", ss[1]);		
		
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		
		String resultFileName = "testResult.txt";
		//往结果中写个文件 */
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(ss[1]) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(inFile) + resultFileName, 0));
	}
	
	private void cmd2() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		
		String resultFileName = "testTrinity.fa";
		//往结果中写个文件 */
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(ss[2]) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outFile) + "/testTrinity.fa", 0));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	/** 假设test3文件夹 不 存在 */
	private void cmd3() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testcc";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		
		String resultFileName = "testcc";
		//往结果中写个文件 */
		FileOperate.createFolders(FileOperate.getPathName(ss[2]));
		String outPath = FileOperate.getPathName(ss[2]) + resultFileName;
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outFile) + resultFileName, 0));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	/** 假设test4文件夹存在 */
	private void cmd4() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity3.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(false);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		assertEquals(inFile, ss[1]);
		
		String resultFileName = "testTrinity3.fa";
		//往结果中写个文件 */
		String outPath = FileOperate.getPathName(ss[2]) + resultFileName;
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outFile) + resultFileName, 0));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	/** 假设test5文件夹存在 */
	private void cmd5() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity4.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		
		String resultFileName = "testTrinity4.fa";
		//往结果中写个文件 */
		String outPath = FileOperate.getPathName(ss[2]) + resultFileName;
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outFile) + resultFileName, 0));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	private void cmd6() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut2/test";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile);
		lsCmd.add("--outPath=" + outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		String resultFileName = "test2.fatestResult.txt";
		//往结果中写个文件 */
		String outTmp = ss[2].split("=")[1];
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(outTmp) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outFile) + resultFileName, 0));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	private void cmd7() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile1 = "src/test/resources/testTrinity.fa";
		String inFile2 = "src/test/resources/testTrinity2.fa";
		String outFile = "src/test/CmdOperateOut2/test";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile1 + "," + inFile2);
		lsCmd.add("--outPath=" + outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile1, true);
		cmdPath.addCmdParamInput(inFile2, true);

		cmdPath.addCmdParamOutput(outFile, false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		String inFileTmp1 = PathDetail.getRandomWithSep(tmpPath, "resources1") + "testTrinity.fa";
		String inFileTmp2 = PathDetail.getRandomWithSep(tmpPath, "resources1") + "testTrinity2.fa";
		assertEquals("--inPath="+ inFileTmp1 + "," + inFileTmp2, ss[1]);
		System.out.println(ArrayOperate.cmbString(ss, " "));
		String resultFileName = "test2.fatestResult.txt";
		//往结果中写个文件 */
		String outTmp = ss[2].split("=")[1];
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(outTmp) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		
		assertEquals(true, FileOperate.isFileExistAndBigThan0(FileOperate.getPathName(outFile) + resultFileName));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	private void cmd8() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile1 = "src/test/resources/testTrinity.fa";
		String inFile2 = "src/test/resources/testTrinity2.fa";
		String outFile = "src/test/CmdOperateOut2/test";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile1 + "," + inFile2);
		lsCmd.add(">");
		lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile1, true);
		cmdPath.addCmdParamInput(inFile2, true);
		
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		String inFileTmp1 = PathDetail.getRandomWithSep(tmpPath, "resources1") + "testTrinity.fa";
		String inFileTmp2 = PathDetail.getRandomWithSep(tmpPath, "resources1") + "testTrinity2.fa";
		assertEquals("--inPath="+ inFileTmp1 + "," + inFileTmp2, ss[1]);
		System.out.println(ArrayOperate.cmbString(ss, " "));
		String resultFileName = "test2.fatestResult.txt";
		//往结果中写个文件 */
		String outTmp = cmdPath.getSaveStdTmp();
		Assert.assertEquals(FileOperate.changeFileSuffix(outFile, "_tmp", null), outTmp);
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(outTmp) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		
		assertEquals(true, FileOperate.isFileExistAndBigThan0(FileOperate.getPathName(outFile) + resultFileName));
		FileOperate.DeleteFileFolder(outFile);
	}
	
	private void cmdConvertHdfs() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile1 = "/hdfs:/src/test/resources/testTrinity.fa";
		String inFile2 = "/hdfs:/src/test/resources/testTrinity2.fa";
		String outFile = "/hdfs:/nbCloud/test/testCode/testCmdpath.out";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile1 + "," + inFile2);
		lsCmd.add("--outPath=" + outFile);
		cmdPath.setLsCmd(lsCmd);
		
		String[] ss = cmdPath.getRunCmd();
		String inFileLocal1 = FileHadoop.convertToLocalPath(inFile1);
		String inFileLocal2 = FileHadoop.convertToLocalPath(inFile2);
		String outLocal = FileHadoop.convertToLocalPath(outFile);
		assertEquals("--inPath="+ inFileLocal1 + "," + inFileLocal2, ss[1]);
		assertEquals("--outPath="+ outLocal, ss[2]);
	}
	
	private void cmdConvertGetFileName() {
		String cmd = "/home/novelbio/software/trinityrnaseq-2.1.1/util/support_scripts/../../Trinity --single \"/home/novelbio/tmp/2015-11-28-10-50-43-3610_RNAassembly_result2/Hap-1trinity/read_partitions/Fb_0/CBin_59/c5929.trinity.reads.fa\" --output \"/home/novelbio/tmp/2015-11-28-10-50-43-3610_RNAassembly_result2/Hap-1trinity/read_partitions/Fb_0/CBin_59/c5929.trinity.reads.fa.out\" --CPU 1";
		ConvertCmdGetFileName convertCmdGetFileName = new ConvertCmdGetFileName();
		String result = convertCmdGetFileName.convertCmd(cmd);
		assertEquals("Trinity --single \"c5929.trinity.reads.fa\" --output \"c5929.trinity.reads.fa.out\" --CPU 1", result);
	}
	
}
