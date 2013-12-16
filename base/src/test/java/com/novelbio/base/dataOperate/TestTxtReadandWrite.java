package com.novelbio.base.dataOperate;

public class TestTxtReadandWrite {
	public static void main(String[] args) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(System.out);
		for (int i = 0; i < 10; i++) {
			txtWrite.writefileln("sss" + i);
		}
		txtWrite.close();
	}
}
