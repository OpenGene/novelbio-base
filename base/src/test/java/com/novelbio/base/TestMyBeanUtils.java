package com.novelbio.base;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.novelbio.base.bootgrid.BGDataGrid;

public class TestMyBeanUtils {

	@Test
	public void test() {
		BGDataGrid computer = new BGDataGrid();
		computer.setCurrent(3);
		
		BGDataGrid newComputer =  new BGDataGrid();
		newComputer.setRows("454");
		MyBeanUtils.copyNotNullProperties(computer, newComputer);
		
		System.out.println(JSON.toJSONString(newComputer));
	}

}
