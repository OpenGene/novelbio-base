package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFileNBC;
import org.apache.tools.zip.ZipOutputStream;


/**
 * 可以处理中文文件名
 */
public class  ZipOperate {
	private static final Logger logger = Logger.getLogger(ZipOperate.class);
	public static void main(String[] args) {
		try {
			unZipFiles("/hdfs:/nbCloud/public/experiment/RNA0617.zip", "/hdfs:/nbCloud/public/experiment/out");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
     * 解压到指定目录
     * @param zipPath
     * @param descDir
     * @author isea533
     */ 
    public static void unZipFiles(String zipPath,String descDir)throws IOException{ 
        unZipFiles(FileOperate.getFile(zipPath), descDir); 
    } 
    /**
     * 解压文件到指定目录
     * @param zipFile
     * @param descDir
     * @author isea533
     */ 
    @SuppressWarnings("rawtypes") 
    public static void unZipFiles(File zipFile,String descDir)throws IOException{
    	descDir = FileOperate.addSep(descDir);
        FileOperate.createFolders(descDir);
        ZipFileNBC zip = new ZipFileNBC(zipFile);
        for(Enumeration entries = zip.getEntries();entries.hasMoreElements();){ 
            ZipEntry entry = (ZipEntry)entries.nextElement(); 
            String zipEntryName = entry.getName(); 
            InputStream in = zip.getInputStream(entry); 
            String outPath = (descDir + zipEntryName).replaceAll("\\*", FileOperate.getSepPath());
            FileOperate.createFolders(FileOperate.getPathName(outPath));
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压 
            if(FileOperate.isFileDirectory(outPath)){ 
                continue; 
            } 
            //输出文件路径信息 
            logger.info(outPath); 
             
            OutputStream out = FileOperate.getOutputStream(outPath, true); 
            byte[] buf1 = new byte[1024]; 
            int len; 
            while((len=in.read(buf1))>0){ 
                out.write(buf1,0,len); 
            } 
            in.close(); 
            out.close(); 
            } 
        logger.info("finish unzip file " + zipFile.getName()); 
    } 
  
    	/**
    	 * 
    	 * @param inputFileName 输入一个文件夹
    	 * @param zipFileName	输出一个压缩文件夹，打包后文件名字
    	 * @throws Exception
    	 */
    	public static void zip(String inputFileName, String zipFileName) throws Exception {
    		logger.info("start zip file: " + zipFileName);
    		zip(zipFileName, FileOperate.getFile(zipFileName));
    	}
    	
    	/**
    	 *
    	 *  @param inputFile 输入一个文件
    	 * @param zipFileName	输出一个压缩文件夹，打包后文件名字
    	 * @throws Exception
    	 * 
    	 */
    	private static void zip(String zipFileName, File inputFile) throws Exception {
    		ZipOutputStream out = new ZipOutputStream(FileOperate.getOutputStream(zipFileName, true));
    		zip(out, inputFile, "");
    		logger.info("zip done");
    		out.close();
    	}
    	
    	/**
    	 * @param out 输出流
    	 * @param f 文件
    	 * @param base 保存在zip中的路径
    	 * @throws Exception
    	 */
    	private static void zip(ZipOutputStream out, File f, String base) throws Exception {
    		if (FileOperate.isFileDirectory(f)) {	//判断是否为目录
    			File[] fl = f.listFiles();
    			out.putNextEntry(new ZipEntry(base + FileOperate.getSepPath()));
    			base = base.length() == 0 ? "" : base + FileOperate.getSepPath();
    			for (int i = 0; i < fl.length; i++) {
    				zip(out, fl[i], base + fl[i].getName());
    			}
    		} else {				//压缩目录中的所有文件
    			out.putNextEntry(new ZipEntry(base));
    			InputStream in = FileOperate.getInputStream(f);
    			int b;
    			logger.info("zipping " + base);
    			while ((b = in.read()) != -1) {
    				out.write(b);
    			}
    			in.close();
    		}
    	}

//    	public static void main(String[] temp) {
//    		String inputFileName = "/home/gaozhu/桌面/abc";	//你要压缩的文件夹
//    		String zipFileName = "/home/gaozhu/桌面/report.zip";	//压缩后的zip文件
//
//    		try {
//				ZipOperate.zip(inputFileName, zipFileName);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}

}
