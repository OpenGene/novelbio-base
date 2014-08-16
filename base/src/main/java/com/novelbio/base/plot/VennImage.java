package com.novelbio.base.plot;

import com.novelbio.base.fileOperate.FileHadoop;


/**
 * 韦恩图对象
 * @author novelbio
 *
 */
public class VennImage {
	/** 图的填充色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff */
	private String[] fillColors = {"dodgerblue","seagreen3", "orchid3","goldenrod1", "gray"};
	/** 图内文字的颜色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff  */
	private String[] catcol = {"black"};
	/** 图片宽度 */
	private int width = 1500;
	/** 图片高度 */
	private int height = 1500;
	/** 文件保存全路径 */
	private String savePath;
	/** 主标题 */
	private String main = "";
	/** 副标题 */
	private String sub = "";
	/** 透明度 */
	private double alpha = 0.5;
	/** 旋转角度 */
	private int rotation = 0;
	/** 左右间距 0-1之间  0.05即为5%*/
	private double margin = 0.05;
	/** 调节字体大小 */
	private double cex = 1.5;  
	/** 调节题目字体大小 */
	private double maincex = 2; 
	/** 调节字的位置 */
	private double catpos= 0;
	/** 调节注释字体大小 */
	private double catcex = 1.5;
	/** 数据组数，默认3组 */
	private int dataSize = 3;
	/**
	 * @param savePath 文件保存全路径
	 * @param width 图片宽度
	 * @param height 图片高度
	 */
	public VennImage(String savePath,int width,int height) {
		this.savePath = FileHadoop.convertToLocalPath(savePath.replace("\\", "/"));
		this.width = width;
		this.height = height;
	}
	/** 图的填充色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff */
	public String getFillColors() {
		String fills = "c(";
		for (int i = 0; i < dataSize; i++) {
			if (i != 0) {
				fills += ",";
			}
			int j = i%(fillColors.length);
			fills += "\"" + fillColors[j] + "\"";
		}
		fills += ")";
		return fills;
	}
	/** 图的填充色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff */
	public void setFillColors(String[] fillColors) {
		this.fillColors = fillColors;
	}
	/** 图内文字的颜色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff  */
	public String getCatcol() {
		String fills = "c(";
		for (int i = 0; i < dataSize; i++) {
			if (i != 0) {
				fills += ",";
			}
			int j = i%(catcol.length);
			fills += "\"" + catcol[j] + "\"";
		}
		fills += ")";
		return fills;
	}
	/** 图内文字的颜色，有几组数据返回几种，从数组中按顺序循环选取颜色，不支持rgb(81,117,81)，只支持#FFFFFF和名称,不支持简写#fff  */
	public void setCatcol(String[] catcol) {
		this.catcol = catcol;
	}
	/** 图片宽度 */
	public String getWidth() {
		return width+"";
	}
	/** 图片宽度 */
	public void setWidth(int width) {
		this.width = width;
	}
	/** 图片高度 */
	public String getHeight() {
		return height+"";
	}
	/** 图片高度 */
	public void setHeight(int height) {
		this.height = height;
	}
	/** 文件保存全路径 */
	public String getSavePath() {
		return savePath;
	}
	/** 主标题 */
	public String getMain() {
		return main;
	}
	/** 主标题 */
	public void setMain(String main) {
		this.main = main;
	}
	/** 副标题 */
	public String getSub() {
		return sub;
	}
	/** 副标题 */
	public void setSub(String sub) {
		this.sub = sub;
	}
	/** 透明度 */
	public String getAlpha() {
		return alpha+"";
	}
	/** 透明度 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	/** 旋转角度 */
	public String getRotation() {
		return rotation+"";
	}
	/** 旋转角度 */
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	/** 左右间距 0-1之间  0.05即为5%*/
	public String getMargin() {
		return margin+"";
	}
	/** 左右间距 0-1之间  0.05即为5%*/
	public void setMargin(double margin) {
		this.margin = margin;
	}
	/** 调节字体大小 */
	public String getCex() {
		return cex+"";
	}
	/** 调节字体大小 */
	public void setCex(double cex) {
		this.cex = cex;
	}
	/** 调节题目字体大小 */
	public String getMaincex() {
		return maincex+"";
	}
	/** 调节题目字体大小 */
	public void setMaincex(double maincex) {
		this.maincex = maincex;
	}
	/** 调节字的位置 */
	public String getCatpos() {
		return catpos+"";
	}
	/** 调节字的位置 */
	public void setCatpos(double catpos) {
		this.catpos = catpos;
	}
	/** 调节注释字体大小 */
	public String getCatcex() {
		return catcex+"";
	}
	/** 调节注释字体大小 */
	public void setCatcex(double catcex) {
		this.catcex = catcex;
	}
	/** 数据组数，默认3组 */
	public int getDataSize() {
		return dataSize;
	}
	/** 数据组数，默认3组 */
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}
	
}
