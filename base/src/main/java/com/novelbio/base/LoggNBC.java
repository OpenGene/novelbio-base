package com.novelbio.base;

//import org.apache.felix.main.Main;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class LoggNBC {
	TxtReadandWrite txtLog;
	
	public static void main(String[] args) {
		LoggNBC loggNBC = new LoggNBC(LoggNBC.class);
		loggNBC.error("tset");
	}
	Logger logger;
	LoggNBC (Class clazz) {
		logger = Logger.getLogger(clazz);
	}
	LoggNBC (Class clazz, String txtOutFile) {
		logger = Logger.getLogger(clazz);
		setTxtLog(txtOutFile);
	}
	public void setTxtLog(String txtName) {
		txtLog = new TxtReadandWrite(txtName, true, true);
		boolean createNew = true;
		if (FileOperate.isFileExist(txtName)) {
			createNew = false;
		}
	}
	public void error(Object message) {
		logger.error(message);
	}
	public void info(Object message) {
		logger.info(message);
	}
	public void debug(Object message) {
		logger.debug(message);
	}
	public void fatal(Object message) {
		logger.fatal(message);
	}

}
