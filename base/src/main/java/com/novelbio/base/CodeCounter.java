package com.novelbio.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.project.fy.Script;

public class CodeCounter {
	public static void main(String[] args) {
		CodeCounter codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio", "/media/winD/fedora/codestatistics/allCodeLines.txt");
		
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/annotation/functiontest", "/media/winD/fedora/codestatistics/functiontestCodeLines.txt");

		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/project", "/media/winD/fedora/codestatistics/projectMinusCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/test/java", "/media/winD/fedora/codestatistics/testCodeLines.txt");
		
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq", "/media/winD/fedora/codestatistics/seqCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/base", "/media/winD/fedora/codestatistics/baseCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/database", "/media/winD/fedora/codestatistics/databaseCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/nbcgui", "/media/winD/fedora/codestatistics/nbcguiCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/nbcgui/GUI", "/media/winD/fedora/codestatistics/nbcguiGUICodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/annotation", "/media/winD/fedora/codestatistics/annotationCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/diffexpress", "/media/winD/fedora/codestatistics/diffexpressCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/tools", "/media/winD/fedora/codestatistics/toolsCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/resequencing", "/media/winD/fedora/codestatistics/resequencingCodeLines.txt");
		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/resequencing/statistics", "/media/winD/fedora/codestatistics/resequencingstatisticsCodeLines.txt");

//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/blast", "/media/winD/fedora/codestatistics/blastCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/blastZJ", "/media/winD/fedora/codestatistics/blastZJCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/chipseq", "/media/winD/fedora/codestatistics/chipseqCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/genomeNew", "/media/winD/fedora/codestatistics/genomeNewCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/HanYanRebsome", "/media/winD/fedora/codestatistics/HanYanRebsomeCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/mapping", "/media/winD/fedora/codestatistics/mappingCodeLines.txt");
//
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/mirna", "/media/winD/fedora/codestatistics/mirnaCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/reseq", "/media/winD/fedora/codestatistics/reseqCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/resequencing", "/media/winD/fedora/codestatistics/resequencingCodeLines.txt");
//		codeCounter = new CodeCounter("/home/zong0jie/git/Novelbio-Bioinformatics-Analysis-Platform/src/main/java/com/novelbio/analysis/seq/rnaseq", "/media/winD/fedora/codestatistics/rnaseqCodeLines.txt");

	
	}
	TxtReadandWrite txtOut;
	/**普通行数*/
	private long normalLines=0;
	/**注释行数*/
	private long commentLines=0;
	/**空白行数*/
	private long spaceLines=0;
	/**总行数*/
	private long totalLines=0;
	/**普通行数*/
	private long normalLinesAll=0;
	/**注释行数*/
	private long commentLinesAll=0;
	/**空白行数*/
	private long spaceLinesAll=0;
	/**总行数*/
	private long totalLinesAll=0;
	
	/***
	 * 通过java文件路径构造该对象
	 * @param filePath java文件路径
	 */
	public CodeCounter(String filePath, String txtOutFile){
		txtOut = new TxtReadandWrite(txtOutFile, true);
		tree(filePath);
		conclution(filePath);
	}
	/**
	 * 处理文件的方法
	 * @param filePath 文件路径
	 */
	private void tree(String filePath){
		File file=new File(filePath);
		File[] childs=file.listFiles();
		if(childs==null){
			parse(file);
		}else{
		for(int i=0;i<childs.length;i++){
			if(childs[i].isDirectory()){
				//不统计隐藏文件夹
				if (childs[i].getName().startsWith(".")) {
					continue;
				}
//				System.out.println("path:"+childs[i].getPath());
				txtOut.writefileln("path:"+childs[i].getPath());
				tree(childs[i].getPath());
			}else{
				if (!childs[i].getName().matches(".*\\.java$")) {
					continue;
				}
//				System.out.println("当前"+childs[i].getName()+"代码行数:");
				txtOut.writefileln("当前"+childs[i].getName()+"代码行数:");
				parse(childs[i]);
				getCodeCounter();
			}
		}
		}
	}
	private void conclution(String filePath) {
		getCodeCounterAll(filePath);
		txtOut.close();
	}
	/**
	 * 解析文件
	 * @param file 文件对象
	 */
	private void parse(File file){
		BufferedReader br=null;
		boolean comment=false;
		try {
			br=new BufferedReader(new FileReader(file));
			String line="";
			while((line=br.readLine())!=null){
				line=line.trim();//去除空格
				if(line.matches("^[\\s&&[^\\n]]*$")
						|| line.equals("{") || line.equals("}")
						|| line.equals("});")
						|| line.startsWith("import") || line.startsWith("package")
					)
				{
					   spaceLines ++;   spaceLinesAll ++;
				}else if((line.startsWith("/*"))&& !line.endsWith("*/")) {
					   commentLines ++;   commentLinesAll ++;
					   comment = true;   
	            }else if(true == comment) {
			           commentLines ++;    commentLinesAll ++; 
	            if(line.endsWith("*/")) {
					   comment = false;
					   }
				}else if(line.startsWith("//") || (line.startsWith("/*")) && line.endsWith("*/")) {
					    commentLines ++; commentLinesAll ++;
				}else {
					   normalLines ++; normalLinesAll ++;
			          }
			  }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 得到Java文件的代码行数
	 */
	private void getCodeCounter(){
		totalLines=normalLines+spaceLines+commentLines;
//		System.out.println("普通代码行数:"+normalLines);
//		System.out.println("空白代码行数:"+spaceLines);
//		System.out.println("注释代码行数:"+commentLines);
//		System.out.println("代码总行数:"+totalLines);
		txtOut.writefileln("普通代码行数:"+normalLines);
		txtOut.writefileln("空白代码行数:"+spaceLines);
		txtOut.writefileln("注释代码行数:"+commentLines);
		txtOut.writefileln("该文件代码总行数:"+totalLines);

		normalLines=0;
		spaceLines=0;
		commentLines=0;
		totalLines=0;
	}
	
	/**
	 * 得到Java文件的代码行数
	 */
	private void getCodeCounterAll(String filePath){
		System.out.println("");
		System.out.println(filePath + "总代码统计");
		totalLinesAll=normalLinesAll+spaceLinesAll+commentLinesAll;
		System.out.println("普通总代码行数:"+normalLinesAll);
		System.out.println("空白总代码行数:"+spaceLinesAll);
		System.out.println("注释总代码行数:"+commentLinesAll);
		System.out.println("代码总行数:"+totalLinesAll);
		
		txtOut.writefileln();
		txtOut.writefileln(filePath + "总代码统计");
		txtOut.writefileln("普通总代码行数:"+normalLinesAll);
		txtOut.writefileln("空白总代码行数:"+spaceLinesAll);
		txtOut.writefileln("注释总代码行数:"+commentLinesAll);
		txtOut.writefileln("代码总行数:"+totalLinesAll);
	}
}
