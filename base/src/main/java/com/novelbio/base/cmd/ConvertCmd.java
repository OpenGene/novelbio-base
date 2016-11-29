package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 文件夹切分并删除的工作
 * @author zong0jie
 * @data 2016年4月26日
 */
public abstract class ConvertCmd {

	abstract String convert(String subCmd);
	
	public String convertCmd(String cmd) {
		if (StringOperate.isRealNull(cmd)) {
			return cmd;
		}
		List<String> lsCmdSub = CmdOperate.splitCmd(cmd);
		List<String> lsCmd = new ArrayList<>();
		for (String subCmd : lsCmdSub) {
			String subModify = convertSubCmd(subCmd);
			if (StringOperate.isRealNull(subModify)) {
				continue;
			}
			lsCmd.add(subModify);
		}
		return ArrayOperate.cmbString(lsCmd.toArray(new String[0]), " ");
	}
	
	public String[] convertCmd(String[] cmd) {
		if (cmd == null || cmd.length == 0) {
			return cmd;
		}
		List<String> lsCmd = new ArrayList<>();
		for (String subCmd : cmd) {
			String subModify = convertSubCmd(subCmd);
			if (StringOperate.isRealNull(subModify)) {
				continue;
			}
			lsCmd.add(subModify);
		}
		return lsCmd.toArray(new String[0]);
	}
	
	/**
	 *  将cmd中需要定位到临时文件夹的信息修改过来，譬如
	 * -output=/hdfs:/test.txt 修改为
	 * -output=/home/novelbio/test.txt
	 * @param stdOut
	 * @param errOut
	 * @param tmpCmd
	 * @return
	 */
	protected String convertSubCmd(String tmpCmd) {
		tmpCmd = tmpCmd.trim();
		
		if (tmpCmd.startsWith("\"") && tmpCmd.endsWith("\"")) {
			tmpCmd = CmdOperate.removeQuot(tmpCmd);
			tmpCmd = convertSubCmd(tmpCmd);
			tmpCmd = CmdOperate.addQuot(tmpCmd);
		} else if (tmpCmd.startsWith("\'") && tmpCmd.endsWith("\'")) {
			tmpCmd = CmdOperate.removeQuot(tmpCmd);
			tmpCmd = convertSubCmd(tmpCmd);
			tmpCmd = CmdOperate.addQuotSingle(tmpCmd);
		} else {
			if (tmpCmd.contains("=")) {
				String[] tmpCmd2Path = tmpCmd.split("=", 2);
				tmpCmd = tmpCmd2Path[0] + "=" + convertSubCmd(tmpCmd2Path[1]);
				return tmpCmd;
			} else if (tmpCmd.contains(",")) {
				String[] tmpCmd2Path = tmpCmd.split(",");
				String[] tmpResult = new String[tmpCmd2Path.length];
				for (int i = 0; i < tmpCmd2Path.length; i++) {
					tmpResult[i] = convertSubCmd(tmpCmd2Path[i]);  
	            }
				tmpCmd = ArrayOperate.cmbString(tmpResult, ",");
				return tmpCmd;
			} else if (tmpCmd.contains(";")) {
				String[] tmpCmd2Path = tmpCmd.split(";");
				String[] tmpResult = new String[tmpCmd2Path.length];
				for (int i = 0; i < tmpCmd2Path.length; i++) {
					tmpResult[i] = convertSubCmd(tmpCmd2Path[i]);  
	            }
				tmpCmd = ArrayOperate.cmbString(tmpResult, ";");
				return tmpCmd;
			}
			tmpCmd = convert(tmpCmd);
		}
		return tmpCmd;
	}


	public static class ConvertHdfs extends ConvertCmd {
		@Override
		String convert(String subCmd) {
			if (subCmd.length() > 6) {
				if (FileHadoop.isHdfs(subCmd) || FileHadoop.isHdfs(subCmd.substring(1, subCmd.length() - 2))) {
					return FileHadoop.convertToLocalPath(subCmd);
				}
			}
			return subCmd;
		}
	}
	public static class ConvertOss extends ConvertCmd {
		boolean isReadMap = true;
		public void setIsReadMap(boolean isReadMap) {
			this.isReadMap = isReadMap;
		}
		@Override
		String convert(String subCmd) {
			if (subCmd.length() > 6) {
				if (FileHadoop.isHdfs(subCmd) || FileHadoop.isHdfs(subCmd.substring(1, subCmd.length() - 2))) {
					return FileHadoop.convertToLocalPath(subCmd);
				}
			} 
			if(subCmd.startsWith("oss://")) {
				// TODO 这里是有bug的.测试先这么写.
				return CmdPathAli.convertAli2Loc(subCmd, isReadMap);
			} else {
				return subCmd;
			}
		}
	}
	public static class ConvertCmdTmp extends ConvertCmd {
		boolean isRedirectOutToTmp;
		boolean isRedirectInToTmp;
		Set<String> setInput;
		Set<String> setOutput;
		Map<String, String> mapName2TmpName;
		
		public ConvertCmdTmp(boolean isRedirectInToTmp, boolean isRedirectOutToTmp, Set<String> setInput, Set<String> setOutput, Map<String, String> mapName2TmpName) {
			this.isRedirectInToTmp = isRedirectInToTmp;
			this.isRedirectOutToTmp = isRedirectOutToTmp;
			this.setInput = setInput;
			this.setOutput = setOutput;
			this.mapName2TmpName = mapName2TmpName;
		}
		
		@Override
		String convert(String subCmd) {
			if ((isRedirectInToTmp && setInput.contains(subCmd))
					|| 
					(isRedirectOutToTmp && setOutput.contains(subCmd))) {
				subCmd = mapName2TmpName.get(subCmd);
			}
			return subCmd;
		}
		
	}
	
	/** 把cmd中的文件路径删除 */
	public static class ConvertCmdGetFileName extends ConvertCmd {
		
		@Override
		String convert(String subCmd) {
			return FileOperate.getFileName(subCmd);
		}
		
	}
}
