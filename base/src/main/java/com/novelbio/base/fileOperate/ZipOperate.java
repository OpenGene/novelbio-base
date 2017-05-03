package com.novelbio.base.fileOperate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 可以处理中文文件名
 * update by fans.fan 170503 使用Apache commons-compress 支持大于4G的文件压缩和中文名称
 */
public class  ZipOperate {
	private static final Logger logger = LoggerFactory.getLogger(ZipOperate.class);
	public static void main(String[] args) {
		try {
			zip("/home/novelbio/Downloads/gnome-commander", "/home/novelbio/tmp/gnome-commander.zip");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * zip压缩文件
	 * @param dir 要压缩的文件或文件夹
	 * @param zippath 压缩文件
	 */
	public static void zip(String dir ,String zippath){
		zip(dir, FileOperate.getLsFoldPathRecur(dir, true), zippath);
	}
	
    /**
     * 把文件压缩成zip格式
     * @param dir		lsPaths所在的文件夹
     * @param lsPaths         需要压缩的文件
     * @param zipFilePath 压缩后的zip文件路径   ,如"D:/test/aa.zip";
     */
    private static void zip(String dir, List<Path> lsPaths, String zipFilePath) {
        if(lsPaths == null || lsPaths.size() <= 0) {
            return ;
        }
        try {
        	zip(dir, lsPaths, FileOperate.getOutputStream(zipFilePath));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
    
    /**
     * 该方法主要用于web前端下载文件使用,数据写入outputStream中,流没有关闭
     * 
     * @param dir			lsPaths所在的文件夹
     * @param lsPaths		需要压缩的文件
     * @param outputStream	压缩数据写入的流
     */
    public static void zip(String dir, List<Path> lsPaths, OutputStream outputStream) {
    	 try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(outputStream);) {
             zaos.setUseZip64(Zip64Mode.AsNeeded);
             //将每个文件用ZipArchiveEntry封装
             //再用ZipArchiveOutputStream写到压缩文件中
             for(Path strfile : lsPaths) {
                 String name = getFilePathName(dir,strfile.toString());
                 if(FileOperate.isFileDirectory(strfile)){
                 	ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(name + FileOperate.getSepPath());
                 	zaos.putArchiveEntry(zipArchiveEntry);
                 	zaos.closeArchiveEntry(); 
                 	continue;
                 }
                 ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(name);
                 zaos.putArchiveEntry(zipArchiveEntry);
                 try (InputStream is = new BufferedInputStream(FileOperate.getInputStream(strfile));) {
                 	byte[] buffer = new byte[1024]; 
                 	int len = -1;
                 	while((len = is.read(buffer)) != -1) {
                 		//把缓冲区的字节写入到ZipArchiveEntry
                 		zaos.write(buffer, 0, len);
                 	}
                 	zaos.closeArchiveEntry(); 
                 }catch(Exception e) {
                 	throw new RuntimeException(e);
                 }
             }
             zaos.finish();
         }catch(Exception e){
             throw new RuntimeException(e);
         }
    }
    
    
    /**
     * 把zip文件解压到指定的文件夹
     * @param zipFile zip文件路径, 如 "D:/test/aa.zip"
     * @param tmpPath 解压后的文件存放路径, 如"D:/test/" ()
     */
    public static void unZipFiles(File zipFile, String tmpPath) {
		unzip(zipFile.toString(), tmpPath);
	}
   
    /**
    * 把zip文件解压到指定的文件夹
    * @param zipFilePath zip文件路径, 如 "D:/test/aa.zip"
    * @param saveFileDir 解压后的文件存放路径, 如"D:/test/" ()
    */
	public static void unzip(String zipFilePath, String saveFileDir) {
		if (!FileOperate.isFileExistAndNotDir(zipFilePath)) {
			logger.warn("zip file={} not exist or not dir", zipFilePath);
			return;
		}
		if(!saveFileDir.endsWith("\\") && !saveFileDir.endsWith("/") ){
			saveFileDir += File.separator;
		}
		FileOperate.createFolders(saveFileDir);
		
		InputStream is = null; 
		ZipArchiveInputStream zais = null;
		try {
			is = FileOperate.getInputStream(zipFilePath);
			zais = new ZipArchiveInputStream(is);
			ArchiveEntry archiveEntry = null;
			while ((archiveEntry = zais.getNextEntry()) != null) { 
				// 获取文件名
				String entryFileName = archiveEntry.getName();
				// 构造解压出来的文件存放路径
				String entryFilePath = saveFileDir + entryFileName;
				OutputStream os = null;
				try {
					// 把解压出来的文件写到指定路径
					if(entryFileName.endsWith("/")){
						FileOperate.createFolders(entryFilePath);
					}else{
						FileOperate.createFolders(FileOperate.getPathName(entryFilePath));
						os = new BufferedOutputStream(FileOperate.getOutputStream(entryFilePath));							
						byte[] buffer = new byte[1024]; 
                        int len = -1; 
                        while((len = zais.read(buffer)) != -1) {
                        	os.write(buffer, 0, len); 
                        }
					}
				} catch (IOException e) {
					throw new IOException(e);
				} finally {
					if (os != null) {
						os.flush();
						os.close();
					}
				}
			} 
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (zais != null) {
					zais.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 文件名处理
	 * @param dir
	 * @param path
	 * @return
	 */
	public static String getFilePathName(String dir,String path){
		if (dir != null && !dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		String p = path.replace(dir, "");
		p = p.replace("\\", "/");
		return p;
	}

}
