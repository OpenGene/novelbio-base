package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestCmdPath extends TestCase {
	public void testCmd() {
		cmd1();
		cmd2();
		cmd3();
		cmd4();
		cmd5();
	}
	private void cmd1() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/media/winE/test/testCmdPath/test.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile, false);
		cmdPath.addCmdParamOutput("/media/winE/test/testCmdPath/", false);
		cmdPath.copyFileIn();
		String[] ss = cmdPath.getRunCmd();
		System.out.println(ArrayOperate.cmbString(ss, " "));
		boolean isFileExist = FileOperate.isFileExistAndBigThanSize(ss[1], 0);
		assertEquals(true, isFileExist);
		assertEquals(FileOperate.getFileName(ss[1]), FileOperate.getFileName(inFile));
		
		String resultFileName = "testResult.txt";
		//往结果中写个文件 */
		TxtReadandWrite txtWrite = new TxtReadandWrite(FileOperate.getPathName(ss[1]) + resultFileName, true);
		txtWrite.writefileln("testCmdPath");
		txtWrite.close();
		cmdPath.copyFileOut();
		
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(FileOperate.getPathName(inFile) + resultFileName, 0));
	}
	
	private void cmd2() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/media/winE/test/testCmdPath/test.fa";
		String outFile = "/media/winE/test/testCmdPath/test2";
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
		cmdPath.copyFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile, 0));
	}
	
	/** 假设test3文件夹 不 存在 */
	private void cmd3() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/media/winE/test/testCmdPath/test.fa";
		String outFile = "/media/winE/test/testCmdPath/test3/";
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
		cmdPath.copyFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + resultFileName, 0));
	}
	
	/** 假设test4文件夹存在 */
	private void cmd4() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/media/winE/test/testCmdPath/test.fa";
		String outFile = "/media/winE/test/testCmdPath/test4";
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
		cmdPath.copyFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + FileOperate.getSepPath() + resultFileName, 0));
	}
	
	/** 假设test5文件夹存在 */
	private void cmd5() {
		CmdPath cmdPath = new CmdPath();
		List<String> lsCmd = new ArrayList<>();
		String inFile = "/media/winE/test/testCmdPath/test.fa";
		
		String outFile = "/media/winE/test/testCmdPath/test5";
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
		cmdPath.copyFileOut();
		assertEquals(true, FileOperate.isFileExistAndBigThanSize(outFile + FileOperate.getSepPath() + resultFileName, 0));
	}
}
