package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.List;

/** 专门用来收集命令行的类 */
public class CmdAddInfo {
	List<String> lsCmd = new ArrayList<>();
	
	public void addCmd(int index, String cmd) {
		lsCmd.add(index, cmd);
	}
	
	public void addCmd(String cmd) {
		lsCmd.add(cmd);
	}
	
	public void addAllCmd(List<String> lsCmd) {
		lsCmd.addAll(lsCmd);
	}
	
	public void addAllCmd(int index, List<String> lsCmd) {
		lsCmd.addAll(index, lsCmd);
	}
	
	public void clear() {
		lsCmd.clear();
	}
}
