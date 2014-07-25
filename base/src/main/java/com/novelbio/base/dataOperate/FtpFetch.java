package com.novelbio.base.dataOperate;


//import it.sauronsoftware.ftp4j.FTPException;
//import it.sauronsoftware.ftp4j.FTPFile;
//import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.fileOperate.FileOperate;

public class FtpFetch {
	public static void main(String[] args) throws MalformedURLException {
		FtpFetch fetch = new FtpFetch();
		fetch.setDownLoadUrl("ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene2accession.gz");
		System.out.println("stop");
	}
	FTPClient ftp;
	int port = 21;

	/** ftp服务器网址 */
	String url;
	/** FTP服务器上的相对路径 */
	String remotePath;
	
	String username = "anonymous";
	String password = "a@a.com";
	
	/** 具体的文件名 */
	String ftpFileName;
	/** 保存到的本地路径 */
	String savePath;

	/**
	 * 设定ftp服务器
	 * @param url
	 */
	public void setUrl(String url, String username, String password) {
		URL urlThis = null;
		try {
			urlThis = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		initial(urlThis.getHost(), username, password);
	}
	/**
	 * 设定需要下载的相对路径
	 * @param ftpFileName
	 */
	public void setRemotePath(String remotePath) {
		if (remotePath.endsWith("/")) {
			remotePath = remotePath.substring(0, remotePath.length() - 1);
		}
		this.remotePath = remotePath;
	}
	public void setSavePath(String savePath) {
		this.savePath = FileOperate.addSep(savePath);
	}
	/**
	 * 设定需要下载的文件名
	 * @param ftpFileName
	 */
	public void setFtpFileName(String ftpFileName) {
		this.ftpFileName = ftpFileName;
	}
	
	/** 输入的是具体的文件url */
	public void setDownLoadUrl(String urlAll) {
		try {
			String urlPath = FileOperate.getParentPathNameWithSep(urlAll).replace(":/", "://");
			URL decodeUrl = new URL(urlPath);
			this.ftpFileName = FileOperate.getFileName(urlAll);
			String remotePath = decodeUrl.getPath();
			setRemotePath(remotePath);
			initial(decodeUrl.getHost(), username, password);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/** 是否初始化成功 */
	private boolean initial(String url, String username, String password) {
		//不需要初始化
		if (ftp != null && this.url.equals(url)
				&&
				(username == null || this.username.equals(username)) 
				&& 
				(password == null || this.password.equals(password))
		) {
			return true;
		}
		
		ftp = new FTPClient();
        boolean reply;
		try {
			ftp.connect(url, port);
			// 如果采用默认端口，可以使用 ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(username, password);// 登录
			reply = ftp.isAuthenticated();
			if (!reply) {
				ftp.logout();
				ftp.disconnect(true);
				ftp = null;
				return false;
			}
		} catch (Exception e) {
			try { ftp.disconnect(false); } catch (Exception e1) {}
			ftp = null;
			e.printStackTrace();
			return false;
		}
        return true;
	}
	
	/**
	 * 返回该文件夹下的全体文件名，不包括子文件夹
	 * @return 出错则返回null
	 */
	public List<FTPFile> getLsFiles() {
        try {
			ftp.changeDirectory(remotePath);
			FTPFile[] fs = ftp.list();
			ArrayList<FTPFile> lsFile = new ArrayList<FTPFile>();
			for (FTPFile ftpFile : fs) {
				lsFile.add(ftpFile);
			}
			return lsFile;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//转移到FTP服务器目录  
        return null;
	}
	/** 下载文件 */
	public boolean downloadFile() {
		List<FTPFile> lsAllFiles = getLsFiles();
		if (lsAllFiles == null) {
			return false;
		}
        for(FTPFile ff : lsAllFiles){  
            if(ff.getName().equals(ftpFileName)){  
                File localFile = new File(savePath + ff.getName());  
				try {
					ftp.download(ff.getName(), localFile);
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
            }  
        }
        return false;
	}
}
