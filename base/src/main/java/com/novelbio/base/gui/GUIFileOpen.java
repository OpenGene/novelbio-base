package com.novelbio.base.gui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class GUIFileOpen  extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7901662414647284213L;
	File currendDir = null;
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls" 如果不设定--譬如null，""，"*"，就显示全部文件
	 * @return
	 */
	public String openFileName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		String[] extensionFinal = filterExtension(extensions);
		if (extensionFinal != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensionFinal);
			chooser.setFileFilter(filter);	
		}
		if (currendDir != null) {
			chooser.setCurrentDirectory(currendDir);
		}
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			currendDir = new File(FileOperate.getParentPathName(path));
			return path;
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls" 如果不设定--譬如null，""，"*"，就显示全部文件
	 * @return
	 */
	public String openFilePathName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		String[] extensionFinal = filterExtension(extensions);
		if (extensionFinal != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensionFinal);
			chooser.setFileFilter(filter);	
		}
		if (currendDir != null) {
			chooser.setCurrentDirectory(currendDir);
		}
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = chooser.getSelectedFile().getAbsolutePath();
			currendDir = new File(FileOperate.getParentPathName(path));
			return path;
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls" 如果不设定--譬如null，""，"*"，就显示全部文件
	 * @return
	 */
	public ArrayList<String> openLsFileName(String  description, String... extensions) {
		ArrayList<String> lsResult = new ArrayList<String>();
		JFileChooser chooser = new JFileChooser();
		String[] extensionFinal = filterExtension(extensions);
		if (extensionFinal != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensionFinal);
			chooser.setFileFilter(filter);	
		}
		if (currendDir != null) {
			chooser.setCurrentDirectory(currendDir);
		}
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			if (files.length > 0) {
				currendDir = files[0].getParentFile();
			}
			for (File file : files) {
				lsResult.add(file.getAbsolutePath());
			}
			return lsResult;
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls" 如果不设定--譬如null，""，"*"，就显示全部文件
	 * @return
	 */
	public String saveFileName(String  description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		String[] extensionFinal = filterExtension(extensions);
		if (extensionFinal != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensionFinal);
			chooser.setFileFilter(filter);
		}
		if (currendDir != null) {
			chooser.setCurrentDirectory(currendDir);
		}
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			currendDir = chooser.getSelectedFile().getParentFile();
			return FileOperate.addSuffix(chooser.getSelectedFile().getAbsolutePath(), extensions[0]);
		}
		return null;
	}
	/**
	 * 打开文本选择器
	 * @param description 如"txt/excel 2003"
	 * @param extensions 如 "txt","xls" 如果不设定--譬如null，""，"*"，就显示全部文件
	 * @return
	 */
	public String saveFileNameAndPath(String description, String... extensions) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		String[] extensionFinal = filterExtension(extensions);
		if (extensionFinal != null) {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensionFinal);
			chooser.setFileFilter(filter);
		}
		if (currendDir != null) {
			chooser.setCurrentDirectory(currendDir);
		}
		int returnVal = chooser.showSaveDialog(getParent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			currendDir = chooser.getSelectedFile().getParentFile();
			String result = FileOperate.addSuffix(chooser.getSelectedFile().getAbsolutePath(),extensions[0]);
			if (FileOperate.isFileDirectory(result)) {
				result = FileOperate.addSep(result);
			}
			return result;
		}
		return null;
	}
	private String[] filterExtension(String... extensions) {
		ArrayList<String> lsExtension = new ArrayList<String>();
		// //////////////将文件名中为空格和*的去除，然后过滤后缀名
		for (String string : extensions) {
			if (string != null && (!string.equals("") && !string.equals("*"))) {
				lsExtension.add(string);
			}
		}
		String[] extensionFinal = null;
		if (lsExtension.size() >= 1) {
			extensionFinal = new String[lsExtension.size()];
			for (int i = 0; i < extensionFinal.length; i++) {
				extensionFinal[i] = lsExtension.get(i);
			}
		}
		return extensionFinal;
	}
}
