package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;


/**
 * 可以处理中文文件名
 */
public class  ZipOperate {

	/**
     * 解压到指定目录
     * @param zipPath
     * @param descDir
     * @author isea533
     */ 
    public static void unZipFiles(String zipPath,String descDir)throws IOException{ 
        unZipFiles(new File(zipPath), descDir); 
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
        ZipFile zip = new ZipFile(zipFile);
        for(Enumeration entries = zip.getEntries();entries.hasMoreElements();){ 
            ZipEntry entry = (ZipEntry)entries.nextElement(); 
            String zipEntryName = entry.getName(); 
            InputStream in = zip.getInputStream(entry); 
            String outPath = (descDir + zipEntryName).replaceAll("\\*", FileOperate.getSepPath());
            //判断路径是否存在,不存在则创建文件路径 
            File file = new File(outPath.substring(0, outPath.lastIndexOf(FileOperate.getSepPath()))); 
            if(!file.exists()){ 
                file.mkdirs(); 
            } 
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压 
            if(new File(outPath).isDirectory()){ 
                continue; 
            } 
            //输出文件路径信息 
            System.out.println(outPath); 
             
            OutputStream out = new FileOutputStream(outPath); 
            byte[] buf1 = new byte[1024]; 
            int len; 
            while((len=in.read(buf1))>0){ 
                out.write(buf1,0,len); 
            } 
            in.close(); 
            out.close(); 
            } 
        System.out.println("******************解压完毕********************"); 
    } 
  
    	/**
    	 * 
    	 * @param inputFileName 输入一个文件夹
    	 * @param zipFileName	输出一个压缩文件夹，打包后文件名字
    	 * @throws Exception
    	 */
    	public static void zip(String inputFileName, String zipFileName) throws Exception {
    		System.out.println(zipFileName);
    		zip(zipFileName, new File(inputFileName));
    	}
    	
    	/**
    	 *
    	 *  @param inputFile 输入一个文件
    	 * @param zipFileName	输出一个压缩文件夹，打包后文件名字
    	 * @throws Exception
    	 * 
    	 */
    	private static void zip(String zipFileName, File inputFile) throws Exception {
    		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
    				zipFileName));
    		zip(out, inputFile, "");
    		System.out.println("zip done");
    		out.close();
    	}
    	
    	/**
    	 * 此方法不给调用，请调用zip(String,String)或zip(String,File)
    	 */
    	private static void zip(ZipOutputStream out, File f, String base) throws Exception {
    		if (f.isDirectory()) {	//判断是否为目录
    			File[] fl = f.listFiles();
    			out.putNextEntry(new org.apache.tools.zip.ZipEntry(base + FileOperate.getSepPath()));
    			base = base.length() == 0 ? "" : base + FileOperate.getSepPath();
    			for (int i = 0; i < fl.length; i++) {
    				zip(out, fl[i], base + fl[i].getName());
    			}
    		} else {				//压缩目录中的所有文件
    			out.putNextEntry(new org.apache.tools.zip.ZipEntry(base));
    			FileInputStream in = new FileInputStream(f);
    			int b;
    			System.out.println(base);
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
