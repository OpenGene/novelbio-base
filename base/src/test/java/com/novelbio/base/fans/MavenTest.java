package com.novelbio.base.fans;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MavenTest {

	@Test
	public void mavenVersionTest(){
		List<String> lsnbcfile = new ArrayList<>();
		List<String> lsNBCFileView = new ArrayList<String>();
		lsnbcfile.forEach((file) -> {
			lsNBCFileView.add(new String(file));
		});
		
	}
	
}
