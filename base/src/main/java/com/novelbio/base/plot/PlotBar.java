package com.novelbio.base.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.points.PointRenderer;

public class PlotBar extends PlotScatter{
	public PlotBar() {
		super(PlotScatter.PLOT_TYPE_BARPLOT);
	}
	public static void main(String[] args) {
		BarInfo barInfo = new BarInfo(1, 10, "aaa");
		BarInfo barInfo2 = new BarInfo(2, 12, "bbb");
		PlotBar plotBar = new PlotBar();
		ArrayList<BarInfo> lsBarInfos = new ArrayList<BarInfo>();
		lsBarInfos.add(barInfo); lsBarInfos.add(barInfo2);
		BarStyle barStyle = new BarStyle();
		barStyle.setBarWidth(1);
		barStyle.setBasicStroke(0.2f);
		barStyle.setColor(BarStyle.getGridentColorBrighter(Color.BLUE));
		barStyle.setColorEdge(BarStyle.getGridentColorDarker(Color.BLUE));
		plotBar.addBarPlot(lsBarInfos, barStyle);
		plotBar.setAxisX(0, 3);
		plotBar.setAxisY(0, 20);
		plotBar.setInsets(PlotScatter.INSETS_SIZE_ML);
		plotBar.saveToFile("/home/zong0jie/桌面/aaa.jpg", 1000, 1000);
	}
    /**
     * using data to plot the Bar figure, 直接加入plot，不进入hash表
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void addBarPlot(List<BarInfo> lsBarInfos, BarStyle barStyle) {
    	DataTable data = new DataTable(Double.class, Double.class, String.class);
    	for (BarInfo barInfo : lsBarInfos) {
			data.add(barInfo.getX(), barInfo.getHeigth(), barInfo.getBarName());
		}    	
    	plot.add(data);
		
    	
    	
    	if (barStyle.getBarWidth() != 0) {
    		plot.setSetting(BarPlot.BAR_WIDTH, barStyle.getBarWidth());
    	}
    	PointRenderer pointRenderer = plot.getPointRenderer(data);
    	pointRenderer.setSetting(PointRenderer.COLOR, barStyle.getColor());
    	pointRenderer.setSetting(BarPlot.BarRenderer.STROKE, barStyle.getBasicStroke());
    	pointRenderer.setSetting(BarPlot.BarRenderer.STROKE_COLOR, barStyle.getEdgeColor());
    	//规定，dotname在第3列，dotvalue也就是常规value在第二列
    	//the third column is the name column
    	pointRenderer.setSetting(PointRenderer.VALUE_COLUMN, 2);
    	pointRenderer.setSetting(PointRenderer.VALUE_DISPLAYED, barStyle.isValueVisible());
    }
    
    public static class BarInfo {
    	double x = 0;
    	double y = 0;
    	String barName = "";
    	public BarInfo(double x, double high, String name) {
    		this.x = x;
    		this.y = high;
    		this.barName = name;
    	}
    	public void setX(double x) {
    		this.x = x;
    	}
    	public void setY(double y) {
    		this.y = y;
    	}
    	public void setBarName(String barName) {
    		this.barName = barName;
    	}
    	public String getBarName() {
    		return barName;
    	}
    	public double getX() {
    		return x;
    	}
    	public double getHeigth() {
    		return y;
    	}
    }

}
