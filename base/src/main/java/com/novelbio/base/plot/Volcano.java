package com.novelbio.base.plot;

import java.awt.Color;
import java.util.List;

/**
 * 火山图绘制
 * @author zong0jie
 */
public class Volcano {
	/** 设置logFC的边界默认为1 */
	double logFCBorder = 1;
	/** 设置Pvalue的边界默认为1.5 */
	double logPvalueBorder = 1.5;
	/** 是否需要log转换 */
	boolean logTransform = false;
	
	//也可以直接输入logFC和Pvalue的值
	List<double[]> lsLogFC2Pvalue;
	
	/**
	 * 输入一个读好的文本，并制定列，实际列。然后获得待画图的数据
	 * @param lslsExcel
	 * @param colLogFC
	 * @param colPvalue
	 */
	public void setLogFC2Pvalue(List<List<String>> lslsExcel, int colLogFC, int colPvalue) {
		colLogFC--; colPvalue--;
		lsLogFC2Pvalue.clear();
		for (List<String> list : lslsExcel) {
			try {
				double logFC = Double.parseDouble(list.get(colLogFC));
				double pvalue = Double.parseDouble(list.get(colPvalue));
				double[] xy = new double[]{logFC, pvalue};
				lsLogFC2Pvalue.add(xy);
			} catch (Exception e) {
			}
		}
	}
	
	public void setLogFC2Pvalue(List<double[]> lsLogFC2Pvalue) {
		this.lsLogFC2Pvalue = lsLogFC2Pvalue;
	}
	
	/** 设定LogFC的边界 */
	public void setLogFCBorder(double logFCBorder) {
		this.logFCBorder = logFCBorder;
	}

	public void setLogPvalueBorder(double logPvalueBorder) {
		this.logPvalueBorder = logPvalueBorder;
	}
	
	/** 画火山图 */
	public PlotScatter drawVolimage(int logFCcolNum, int pValuecolNum, String YTitle) {
		/* 定义坐标系 */
		PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		plotScatter.setTitle( "Volcano plot", null);
		plotScatter.setTitleX("LogFC", null, 0);
		plotScatter.setTitleY("-Log10(" + YTitle + ")", null, 0);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_L);

		double minX = -5;
		double maxX = 5;
		double maxY = 10;
		plotScatter.setAxisX(minX, maxX);
		plotScatter.setAxisY(0, maxY);
		/* 定义红的的半透明的点 */
		DotStyle dotStyleHalfRed = new DotStyle();
		Color halfRed = new Color(255, 0, 0, 100);
		dotStyleHalfRed.setColor(halfRed);
		dotStyleHalfRed.setStyle(DotStyle.STYLE_CYCLE);
		dotStyleHalfRed.setSize(DotStyle.SIZE_MB);

		/* 定义深绿的的半透明的点 */
		DotStyle dotStyleHalfGreen = new DotStyle();
		Color halfGreen = new Color(34, 139, 34, 100);
		dotStyleHalfGreen.setColor(halfGreen);
		dotStyleHalfGreen.setStyle(DotStyle.STYLE_CYCLE);
		dotStyleHalfGreen.setSize(DotStyle.SIZE_MB);

		/* 定义深灰的的半透明的点 */
		DotStyle dotStyleHalfGrey = new DotStyle();
		Color halfGrey = new Color(77, 77, 77, 190);
		dotStyleHalfGrey.setColor(halfGrey);
		dotStyleHalfGrey.setStyle(DotStyle.STYLE_CYCLE);
		dotStyleHalfGrey.setSize(DotStyle.SIZE_MB);

		/* 定义深蓝的的半透明的点 */
		DotStyle dotStyleHalfBlue = new DotStyle();
		Color halfBlue = new Color(16,78,139, 100);
		dotStyleHalfBlue.setColor(halfBlue);
		dotStyleHalfBlue.setStyle(DotStyle.STYLE_CYCLE);
		dotStyleHalfBlue.setSize(DotStyle.SIZE_MB);
		
		/*画出分界线*/
		DotStyle dotStyleBorder= new DotStyle();
		dotStyleBorder.setColor(Color.black);
		dotStyleBorder.setStyle(DotStyle.STYLE_LINE);
		dotStyleBorder.setSize(DotStyle.SIZE_M);
		
		for (double[] xy : lsLogFC2Pvalue) {
			double x;
			if (logTransform) {
				x = Math.log(xy[0])/Math.log(2);
			} else {
				x = xy[0];
			}
			double y = -Math.log10(xy[1]);
			if (Math.abs(x) > logFCBorder && y > logPvalueBorder) {
				plotScatter.addXY(x, y, dotStyleHalfRed);
			} else if (Math.abs(x) > logFCBorder && y < logPvalueBorder) {
				plotScatter.addXY(x, y, dotStyleHalfBlue);
			} else if (Math.abs(x) < logFCBorder && y > logPvalueBorder) {
				plotScatter.addXY(x, y, dotStyleHalfGreen);
			} else if (Math.abs(x) < logFCBorder && y < logPvalueBorder) {
				plotScatter.addXY(x, y, dotStyleHalfGrey);
			}
		}
		/*添加一条与X平行的边界*/
		double[] xBorder1 = {minX,maxX}; 
		double[] yBorder1 = {logPvalueBorder,logPvalueBorder}; 
	
		plotScatter.addXY(xBorder1, yBorder1, dotStyleBorder);
		
		/*添加两条与Y平行的边界*/
		double[] xBorder2 = {-logFCBorder,-logFCBorder}; 
		double[] yBorder2 = {0,maxY}; 
		plotScatter.addXY(xBorder2, yBorder2, dotStyleBorder.clone());

		double[] xBorder3 = {logFCBorder,logFCBorder};
		double[] yBorder3 = yBorder2;
		plotScatter.addXY(xBorder3, yBorder3, dotStyleBorder.clone());
		return plotScatter;
	}
	
	public void saveAs(PlotScatter plotScatter , int width , int height ,String outImageFile) {
		plotScatter.saveToFile(outImageFile, width, height);
	}
	
}
