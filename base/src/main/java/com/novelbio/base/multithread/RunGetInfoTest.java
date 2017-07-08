package com.novelbio.base.multithread;

public class RunGetInfoTest implements RunGetInfo<Double> {

	@Override
	public void setRunningInfo(Double info) {
		System.out.println(info);
	}

	@Override
	public void done(RunProcess runProcess) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threadSuspended(RunProcess runProcess) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threadResumed(RunProcess runProcess) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threadStop(RunProcess runProcess) {
		// TODO Auto-generated method stub
		
	}

}
