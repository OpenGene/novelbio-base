package com.novelbio.base.cmd;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

@RunWith(PowerMockRunner.class)

public class TestCmdPath2 {
	
	@PrepareForTest(DateUtil.class)
	@Test
	public void testCmd() {
//		CmdPath.tmpPath = "/home/novelbio/tmp";
		PowerMockito.mockStatic(DateUtil.class);
		PowerMockito.when(DateUtil.getDateAndRandom()).thenReturn("2015-03-04-2211");
		
		cmd1();
		cmd2();
		cmd3();
		cmd4();
		cmd5();
		cmd6();
	}
	
	private void cmd1() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput("/home/novelbio/NBCsource/test/CmdOperate/cmdResult/", false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		assertEquals("bwa-index", ss[0]);
		assertEquals("/home/novelbio/tmp/chrALL_orange.fa", ss[1]);		
		
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
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		String outFile = "/home/novelbio/NBCsource/test/CmdOperate/cmdResult/test2";
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
		
		String resultFileName = "test2";
		//往结果中写个文件 */
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(ss[2]) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile, 0));
	}
	
	/** 假设test3文件夹 不 存在 */
	private void cmd3() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		String outFile = "/home/novelbio/NBCsource/test/CmdOperate/cmdResult/test3/";
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
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + resultFileName, 0));
	}
	
	/** 假设test4文件夹存在 */
	private void cmd4() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		String outFile = "/home/novelbio/NBCsource/test/CmdOperate/cmdResult/test4";
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
		
		String resultFileName = "testcc";
		//往结果中写个文件 */
		String outPath = FileOperate.addSep(ss[2]) + resultFileName;
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + FileOperate.getSepPath() + resultFileName, 0));
	}
	
	/** 假设test5文件夹存在 */
	private void cmd5() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		
		String outFile = "/home/novelbio/NBCsource/test/CmdOperate/cmdResult/test5";
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
		String outPath = FileOperate.addSep(ss[2]) + resultFileName;
		TxtReadandWrite txtWrite = new TxtReadandWrite(outPath, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.moveFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + FileOperate.getSepPath() + resultFileName, 0));
	}
	
	private void cmd6() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/home/novelbio/NBCsource/test/CmdOperate/chrALL_orange.fa";
		String outPath = "/home/novelbio/NBCsource/test/CmdOperate/cmdResult/test2.fa";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile);
		lsCmd.add("--outPath=" + outPath);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamOutput(outPath, false);
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
		
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(outPath) + resultFileName, 0));
	}
}
