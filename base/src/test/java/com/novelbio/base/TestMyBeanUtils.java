package com.novelbio.base;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMyBeanUtils {
	static Logger logger = LoggerFactory.getLogger(TestMyBeanUtils.class);
	public static void main(String[] args) throws URISyntaxException {
		logger.error("test");
		//		URI uri = new URI("hdfs:/nbCloud/NT-01_gvcf.vcf");
//		System.out.println(uri.getScheme());
//		Path file = Paths.get(uri);
//		System.out.println("File [" + file.toString() + "] exists = '" + Files.exists(file) + "'");
    }

}
