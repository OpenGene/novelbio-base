package com.novelbio.base.cmd;

public enum EnumProcessStatus {
	/** 休眠状态 */
	S, 
	/** 不可中断的休眠状态 */
	D,
	/** 运行状态 */
	R,
	/** 僵死状态 */
	Z,
	/** 停止或跟踪状态 */
	T,
	/** 退出状态，进程即将被销毁 */
	X,
	
	unKnown;
	
	/** 可以输入 linux 中ps的STAT状态，如RI等
	 * @param processStatus
	 * @return
	 */
	public static EnumProcessStatus getValueOf(String processStatus) {
		return EnumProcessStatus.valueOf(processStatus.toCharArray()[0] + "");
	}
	
}
