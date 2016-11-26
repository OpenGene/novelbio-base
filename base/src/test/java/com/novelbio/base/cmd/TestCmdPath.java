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
public class TestCmdPath {
	String tmpPath = PathDetail.getTmpPathWithSep();
	
	@PrepareForTest(DateUtil.class)
	@Test
	public void testCmd() {
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
	}
	
	private void cmd1() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile);
		cmdPath.addCmdParamOutput("src/test/resources/cmd");
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
		String[] ss = cmdPath.getRunCmd();
		assertEquals("bwa-index", ss[0]);

		assertEquals(PathDetail.getTmpPathRandom() +  "resources/testTrinity.fa", ss[1]);		
		
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
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile);
		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
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
		FileOperate.deleteFileFolder(outFile);
	}
	
	/** 假设test3文件夹 不 存在 */
	private void cmd3() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testcc";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile);
		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
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
		FileOperate.deleteFileFolder(outFile);
	}
	
	/** 假设test4文件夹存在 */
	private void cmd4() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity3.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(false);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile);
		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
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
		FileOperate.deleteFileFolder(outFile);
	}
	
	/** 假设test5文件夹存在 */
	private void cmd5() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut/testTrinity4.fa";
		lsCmd.add("bwa-index"); lsCmd.add(inFile); lsCmd.add(outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectInToTmp(true);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamInput(inFile);
		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
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
		FileOperate.deleteFileFolder(outFile);
	}
	
	private void cmd6() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile = "src/test/resources/testTrinity.fa";
		String outFile = "src/test/CmdOperateOut2/test";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile);
		lsCmd.add("--outPath=" + outFile);
		cmdPath.setLsCmd(lsCmd);
		cmdPath.setRedirectOutToTmp(true);
		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
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
		FileOperate.deleteFileFolder(outFile);
	}
	
	private void cmd7() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
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
		cmdPath.addCmdParamInput(inFile1);
		cmdPath.addCmdParamInput(inFile2);

		cmdPath.addCmdParamOutput(outFile);
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
		String[] ss = cmdPath.getRunCmd();
		
		String inFileTmp1 = PathDetail.getTmpPathRandom() +  "resources/testTrinity.fa";
		String inFileTmp2 = PathDetail.getTmpPathRandom() +  "resources/testTrinity2.fa";

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
		FileOperate.deleteFileFolder(outFile);
	}
	
	private void cmd8() {
		CmdPath cmdPath = CmdPath.generateCmdPath(true);
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
		cmdPath.addCmdParamInput(inFile1);
		cmdPath.addCmdParamInput(inFile2);
		
		cmdPath.generateTmPath();
		cmdPath.copyFileInAndRecordFiles();
		String[] ss = cmdPath.getRunCmd();

		String inFileTmp1 = PathDetail.getTmpPathRandom() +  "resources/testTrinity.fa";
		String inFileTmp2 = PathDetail.getTmpPathRandom() +  "resources/testTrinity2.fa";

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
		FileOperate.deleteFileFolder(outFile);
	}
	

}
