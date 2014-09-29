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
	T
}
