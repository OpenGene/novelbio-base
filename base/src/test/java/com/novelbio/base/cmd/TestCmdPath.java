package com.novelbio.base.cmd;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.novelbio.base.dataOperate.DateUtil;
	
@RunWith(PowerMockRunner.class)
@PrepareForTest(CmdPath.class)
public class TestCmdPath {
 
  @Test 
  public void testCallSystemStaticMethod() { 
	  System.out.println("============");
  } 
}