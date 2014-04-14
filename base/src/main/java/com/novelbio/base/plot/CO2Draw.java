package com.novelbio.base.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;

import com.novelbio.base.fileOperate.FileOperate;



public class CO2Draw  extends JFrame { 

	/** 
     * 
     */ 
    private static final long serialVersionUID = 1L; 
    
    public static void main(String[] args) {
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CO2Draw inst = new CO2Draw();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
				inst.setTitle("NovelBio Analysis Platform");
				Image im = Toolkit.getDefaultToolkit().getImage("/home/zong0jie/desktop/logo.png");
				inst.setIconImage(im);
				inst.setResizable(false); 
				inst.draw();
			}
		});
	}
    
    public void draw(){
    	this.setSize(1150, 750);
		XYSeries xyseries1 = new XYSeries("One");  
        xyseries1.add(1987, 50);  
        xyseries1.add(1997, 20);  
        xyseries1.add(2007, 30);  
           
        XYSeries xyseries2 = new XYSeries("Two");  
        xyseries2.add(1987, 20);  
        xyseries2.add(1997, 10D);  
        xyseries2.add(2007, 40D);  
           
   
        XYSeries xyseries3 = new XYSeries("Three");  
        xyseries3.add(1987, 40);  
        xyseries3.add(1997, 30.0008);  
        xyseries3.add(2007, 38.24);  
           
   
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();  
   
        xySeriesCollection.addSeries(xyseries1);  
        xySeriesCollection.addSeries(xyseries2);  
        xySeriesCollection.addSeries(xyseries3);  


    	XYSplineRenderer renderer = new XYSplineRenderer();
    	renderer.setBaseShapesVisible(false); //绘制的线条上不显示图例，如果显示的话，会使图片变得很丑陋
    	renderer.setSeriesPaint(0, Color.GREEN); //设置0号数据的颜色。如果一个图中绘制多条曲线，可以手工设置颜色
    	renderer.setPrecision(10); //设置精度，大概就是在源数据两个点之间插入5个点以拟合出一条平滑曲线
    	renderer.setSeriesShapesVisible(0, true);
    	renderer.setSeriesShapesVisible(1, true);
    	renderer.setSeriesShapesVisible(2, true);
    	NumberAxis xAxis = new NumberAxis("percentage of bam junctions (%)");
    	xAxis.setAutoRangeIncludesZero(false);
    	NumberAxis yAxis = new NumberAxis("percentage of all know junctions(%)");
    	yAxis.setAutoRangeIncludesZero(false);

    	XYPlot plot = new XYPlot(xySeriesCollection, xAxis, yAxis, renderer);
    	plot.setBackgroundPaint(Color.white);
    	plot.setDomainGridlinePaint(Color.white);
    	plot.setRangeGridlinePaint(Color.white);
    	plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4)); //设置坐标轴与绘图区域的距离
//		try {
//			Image src = ImageIO.read(FileOperate.getFile("/home/novelbio/桌面/icon/water1.png"));
//			plot.setBackgroundImage(src);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

    	JFreeChart chart = new JFreeChart("Junction Saturation", //标题
    	JFreeChart.DEFAULT_TITLE_FONT, //标题的字体，这样就可以解决中文乱码的问题
    	plot,
    	true //不在图片底部显示图例
    	);
//    	chart.getLegend().setPosition(RectangleEdge.);
//    	chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER); 
    	chart.getLegend().setPosition(RectangleEdge.BOTTOM);  
         //设置图例在图片中的位置(上中下)  
//    	chart.getLegend().setVerticalAlignment(VerticalAlignment.TOP);  
//    	ImageUtils.saveBufferedImage(chart, outputFile)
        ChartPanel chartpanel = new ChartPanel(chart);
        chartpanel.setPopupMenu(null);
        getContentPane().add(chartpanel);
    }

} 