package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.Equations;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.EnumeratedData;
import de.erichseifert.gral.data.statistics.Histogram1D;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.plots.BarPlot;
import de.erichseifert.gral.plots.BoxPlot;
import de.erichseifert.gral.plots.PlotArea;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.XYPlot.XYNavigationDirection;
import de.erichseifert.gral.plots.areas.AreaRenderer;
import de.erichseifert.gral.plots.areas.DefaultAreaRenderer2D;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.points.DefaultPointRenderer2D;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.util.Insets2D;
import de.erichseifert.gral.util.Orientation;

public class PlotScatter extends PlotNBCInteractive{
	public static final int PLOT_TYPE_BARPLOT = 1;
	public static final int PLOT_TYPE_BOXPLOT = 2;
	public static final int PLOT_TYPE_SCATTERPLOT = 3;
	
    public static final int INSETS_SIZE_S = 100;
    public static final int INSETS_SIZE_SM = 200;
    public static final int INSETS_SIZE_M = 300;
    public static final int INSETS_SIZE_ML = 400;
    public static final int INSETS_SIZE_L = 500;
    
    private static final Logger logger = Logger.getLogger(PlotScatter.class);
    
	HashMap<DotStyle, DataTable> hashDataTable = new HashMap<DotStyle, DataTable>();
	
	XYPlot plot;
	String title = null, titleX = null, titleY = null;
	Double spaceX = null, spaceY = null;
    Font fontTitle = new Font(Font.SANS_SERIF, Font.PLAIN, 15), fontX = null, fontY = null;
    int InsetsSize = 200;

    /** custom axis X's ticks */
    Map<Double, String> mapAxisX = null;
    Font fontTicksX = null;
    /** custom axis Y's ticks */
    Map<Double, String> mapAxisY = null;
    Font fontTicksY = null;
    /** 坐标轴边界 */
    Axis axisX = null, axisY = null;
    /**内部坐标轴边界，如果外部没有设定坐标轴边界，就用内部的 */
    Axis axisXMy = new Axis(Double.MAX_VALUE, Double.MIN_VALUE), axisYMy = new Axis(Double.MAX_VALUE, Double.MIN_VALUE);
    
    /**
     * 图片坐标轴到图片边缘的距离
     */
    double insetsTop = 30, insetsLeft = 90, insetsBottom = 70, insetsRight = 40;
    /** 坐标轴的title到坐标轴的距离 */
    double insetsX = 5, insetsY = 5;
    
    DataTable dataTable;
    //背景是否画成格子的
    boolean isBGgrid = true;
    Color colorBGgridMajor;
    Color colorBGgridMinor;
    
    /** 
     * 哪一种图，是scatter还是盒图还是bar图
     */
    public PlotScatter(int PLOT_TYPE) {
    	if (PLOT_TYPE == PLOT_TYPE_BARPLOT) {
    		plot = new BarPlot(new DataTable(Double.class, Double.class));
		} else if (PLOT_TYPE == PLOT_TYPE_BOXPLOT) {
			plot = new BoxPlot(new DataTable(Double.class, Double.class, Double.class, Double.class, Double.class));
		} else if (PLOT_TYPE == PLOT_TYPE_SCATTERPLOT) {
			plot = new XYPlot(new DataTable(Double.class, Double.class));
		}
    	
    }
    
    /**
     * 设定背景的格子颜色，默认无色
     * @param BGgridColorMajor
     * @param BGgridColorMinor
     */
   public void setColorBGgrid( Color colorBGgridMajor, Color colorBGgridMinor) {
    	this.colorBGgridMajor = colorBGgridMajor;
    	this.colorBGgridMinor = colorBGgridMinor;
    }
    /**
     * add point
     * @param x
     * @param y
     * @param dotStyle 如果是不同名字的点，需要创建新的dotStyle，dotStyle仅按地址保存
     */
    public void addXY(double x, double y, DotStyle dotStyle) {
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		} else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	dataTable.add(x,y,dotStyle.getName());
    }
    /**
     * add point array, x.length must equals y.length
     * @param x
     * @param y
     * @param dotStyle 如果是不同名字的点，需要创建新的dotStyle，dotStyle仅按地址保存
     */
    public void addXY(double[] x, double[] y, DotStyle dotStyle) {
    	if (x.length != y.length) {
			return;
		}
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		} else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	logger.error("add1");
    	for (int i = 0; i < x.length; i++) {
    		dataTable.add(x[i],y[i], dotStyle.getName());
    	  	logger.error("addName" + i);
		}
    	logger.error("add2");
    }
    /**
     * add lsXY, double[0]: x  double[1]: y
     * @param x
     * @param y
     */
    public void addXY(Collection<double[]> lsXY, DotStyle dotStyle) {
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		} else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	logger.error("a");
    	for (double[] ds : lsXY) {
    		dataTable.add(ds[0], ds[1], dotStyle.getName());
    		logger.error("b");
		}
    }
    /**
     * check the method
     * add lsX, lsY
     * @param x
     * @param y
     */
    public void addXY(Collection<? extends Number> lsX, Collection<? extends Number> lsY, DotStyle dotStyle) {
    	if (lsX.size() != lsY.size()) {
			return;
		}
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		} else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	for (Number numberX : lsX) {
			Number numberY = lsY.iterator().next();
			dataTable.add(numberX.doubleValue(), numberY.doubleValue(), dotStyle.getName());
    	}
    }
    /**
     * 除去加入的信息
     * @param dotStyle
     */
    public void removeData(DotStyle dotStyle) {
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		} else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	plot.remove(dataTable);
    }
    //////////////////////////////////////////
    /**
     * 给定一个dotstyle，返回该dotstyle所对应的datatable
     * 同时将该datatable所对应的dataseries装入plot
     * @param dotStyle
     * @return
     */
    protected DataTable getDataTable(DotStyle dotStyle) {
    	dataTable = new DataTable(Double.class, Double.class, String.class);
    	logger.error("get1");
    	if (!hashDataTable.containsKey(dotStyle)) {
    		addPlot(dotStyle, dataTable);
		}
    	else {
			dataTable = hashDataTable.get(dotStyle);
		}
    	logger.error("get3");
    	return dataTable;
    }
   
    private void addPlot(DotStyle dotStyle, DataTable dataTable) {
		hashDataTable.put(dotStyle, dataTable);
		logger.error("get2");
		if (plot == null) {
			if (dotStyle.getStyle() == DotStyle.STYLE_BAR) {
				plot = new BarPlot(dataTable);
			} else {
				plot = new XYPlot(dataTable);
			}
		}
		else {
			plot.add(dataTable);
		}
		setPointStyle(dataTable, dotStyle);
    }
    ////////////////////////////////////////////////////////////////
    /**
     * 以前用某个dotStyle设定的输入值(譬如一条曲线)，
     * 如果想要换显示方式，譬如颜色等，只需要将dotStyle的设置换一下，然后输入进来即可
     * @param dotStyle
     */
    public void changeDotStyle(DotStyle dotStyle) {
		DataTable dataTable = hashDataTable.get(dotStyle);
		setPointStyle(dataTable, dotStyle);
	}
    /**
     * 待修正，将dataTable装入hash表中
     * using data to plot the histogram
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void addHistData(Collection<? extends Number> lsNum, int breakNum, BarStyle dotStyle) {
    	DataTable dataTable = new DataTable(Double.class);
    	for (Number number : lsNum) {
			dataTable.add(number.doubleValue());//(number.doubleValue());
		}
    	addHistData(dataTable, breakNum, dotStyle);
    }
    /**
     * using data to plot the histogram
     * @param lsNum data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    public void addHistData(double[] lsNum, int breakNum, BarStyle dotStyle) {
    	DataTable dataTable = new DataTable(Double.class);
    	for (Number number : lsNum) {
			dataTable.add(number.doubleValue());//(number.doubleValue());
		}
    	addHistData(dataTable, breakNum, dotStyle);
    }
    
    /**
     * using data to plot the histogram
     * @param dataTable data 
     * @param breakNum Number of subdivisions for analysis.
     * @param dotStyle
     */
    private void addHistData(DataTable dataTable, int breakNum, BarStyle barStyle) {
    	Histogram1D histogram = new Histogram1D(dataTable, Orientation.VERTICAL, breakNum);
    	double min = dataTable.getStatistics().get(Statistics.MIN);
    	double max = dataTable.getStatistics().get(Statistics.MAX);
    	double step = (max - min)/breakNum;
    	int allNum = dataTable.getRowCount();
    	DataSource histogram2d = new EnumeratedData(histogram, (min + min - step)/2.0, step);
        
    	if (barStyle.getBarWidth() == 0) {
    		barStyle.setBarWidth(step*0.95);
    	}
    	barStyle.setStyle(DotStyle.STYLE_BAR);
    	DataTable dataTable2 = new DataTable(Double.class, Double.class, Double.class);
    	
    	double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE, ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;
    	
    	for (int i = 0; i < histogram2d.getRowCount(); i++) {
    		double x = Double.parseDouble(histogram2d.get(0, i).toString());
    		double yValue = Double.parseDouble(histogram2d.get(1, i).toString());
    		double yProperty = yValue/allNum;
    		
    		if (x < xmin)
				xmin = x;
    		if (x > xmax)
				xmax = x;
      		if (yProperty < ymin)
      			ymin = yProperty;
      		if (yProperty > ymax)
      			ymax = yProperty;
        		
			dataTable2.add(x, yProperty, yValue);
		}
    	setAxis(xmin, xmax, true, 0.1, 0.1);
    	setAxis(0, ymax, false, 0, 0.1);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	if (plot == null) {
			plot = new BarPlot(dataTable2);
		} else {
			plot.add(dataTable2);
		}
    	
    	setPointStyle(dataTable2, barStyle);
    }
    
    private void setAxis(double min, double max, boolean X, double extendRangeMin, double extendRangeMax) {
    	Axis axis = null;
    	double range = Math.abs(max - min);
    	if (X)
    		axis = axisXMy;
    	else
    		axis = axisYMy;
    	
		if (min < axis.getMin().doubleValue()) {
			axis.setMin(min - range * extendRangeMin);
		}
		if (max > axis.getMax().doubleValue()) {
			axis.setMax(max + range * extendRangeMax);
		}
    }

    
    /**
     *  设定坐标轴边界
     * @param x1
     * @param x2
     */
    public void setAxisX(double x1, double x2) {
    	axisX = new Axis(x1, x2);
    	painted = false;
    }
    /**
     * 设置标题
     * @param title main title
     * @param fontTitle font of the title
     * @param title
     */
    public void setTitle(String title, Font fontTitle)  {
    	if (title != null)
    		this.title = title;
    	if (fontTitle != null)
    		this.fontTitle =fontTitle;
    	
    	painted = false;
    }
    
    /**
     * 
     * 设置标题
     * @param titleX tile on axis x
     * @param fontX font of the title 可以设定为null
     * @param spaceX ticks interval, 0 means not set the space 可以设定为0
     */
    public void setTitleX(String titleX, Font fontX, double spaceX)  {
    	if (titleX != null)
    		this.titleX = titleX;
    	if (fontX != null)
    		this.fontX = fontX;
    	if (spaceX != 0)
    		this.spaceX = spaceX;
    	
    	painted = false;
    }
    
    /**
     * 设置标题
     * @param titleY tile on axis y
     * @param fontY font of the title
     * @param spaceY ticks interval, 0 means not set the space
     */
    public void setTitleY(String titleY, Font fontY, double spaceY)  {
    	if (titleY != null)
    		this.titleY = titleY;
    	if (fontY != null)
    		this.fontY = fontY;
    	if (spaceY != 0)
    		this.spaceY = spaceY;
    }
    
    public void setAxisTicksXMap(Map<Double, String> mapTicks) {
    	if (mapTicks != null) {
    		this.mapAxisX = mapTicks;
		}
    	painted = false;
	}
    public void setAxisTicksXFont(Font fontTicks) {
		if (fontTicks != null) {
			this.fontTicksX = fontTicks;
		}
		painted = false;
	}    
    
    public void setAxisTicksYMap(Map<Double, String> mapTicks) {
    	if (mapTicks != null) {
    		this.mapAxisY = mapTicks;
		}
    	painted = false;
	}
    public void setAxisTicksYFont(Font fontTicks) {
		if (fontTicks != null) {
			this.fontTicksY = fontTicks;
		}
		painted = false;
	}
    /**
     * 设定坐标轴边界
     * @param y1
     * @param y2
     */
    public void setAxisY(double y1, double y2) {
    	axisY = new Axis(y1, y2);
    	painted = false;
    }
    
    /**
     * 设定图片坐标轴到图片边缘的距离,这个一般走默认就好
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setInsets(int left, int top, int right, int bottom) {
    	this.insetsTop = top; this.insetsLeft = left;
    	this.insetsBottom = bottom; this.insetsRight = right;
    	painted = false;
    }
    /**
     * set the marge size of a figure, the bigger the marge be, the font of the tile will also bigger
     * 设定各个地方的字体，包括x，y轴的字体和刻度字体
     * @param int size 
     */
    public void setInsets(int size) {
    	double insetsTop = 20, insetsLeft = 83, insetsBottom = 68, insetsRight = 40;
    	double insetsX = 5, insetsY = 2;
    	double scaleInsets = 1; double scaleFont = 1;
    	if (size == INSETS_SIZE_S) {
    		scaleInsets = 0.8;
    		scaleFont = 0.6;
    	}
    	else if (size == INSETS_SIZE_SM) {
    		scaleInsets = 1;
    		scaleFont = 0.8;
		}
    	else if (size == INSETS_SIZE_M) {
    		scaleInsets = 1.4;
    		scaleFont = 1.2;
		}
    	else if (size == INSETS_SIZE_ML) {
    		scaleInsets = 1.7;
    		scaleFont = 1.5;
		}
    	else if (size == INSETS_SIZE_L) {
    		scaleInsets = 2;
    		scaleFont = 1.8;
		}
    	this.insetsTop = (insetsTop * scaleInsets); this.insetsLeft = (insetsLeft * scaleInsets);
		this.insetsBottom = (insetsBottom * scaleInsets); this.insetsRight = (insetsRight * scaleInsets);	
		this.insetsX = 2; this.insetsY =  2;
    	this.fontX = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(20*scaleFont));
    	this.fontY = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(20*scaleFont));
		this.fontTicksX = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(15*scaleFont));
		this.fontTicksY = new Font(Font.SANS_SERIF, Font.PLAIN, (int)(15*scaleFont));
		this.fontTitle =  new Font(Font.SANS_SERIF, Font.PLAIN, (int)(25*scaleFont));
		painted = false;
    }
    
//	@Override
	protected void draw(int width, int heigh) {
		drawPlot();
		toImage(width, heigh);
	}
	/**
	 * needs check
	 * @return
	 */
	public Drawable getPlot() {
		drawPlot();
		return plot;
	}
	public void clearData() {
		plot = null;
	}
//	public void changeSetting() {
//		//TODO 修改配置文件获得图片的改变，初步考虑修改hashDataTable里面的dotstyle
//	}
	/**
	 * @param width
	 * @param heigh
	 */
	protected void drawPlot() {
		plot.setInsets(new Insets2D.Double( insetsTop, insetsLeft, insetsBottom, insetsRight));
		Axis axisxthis = axisXMy, axisythis = axisYMy;
		if (axisX != null)
			axisxthis = axisX;
		if (axisY != null)
			axisythis = axisY;
		
        plot.getAxis(XYPlot.AXIS_X).setRange(axisxthis.getMin() ,axisxthis.getMax());//设置坐标轴
        plot.getAxis(XYPlot.AXIS_Y).setRange(axisythis.getMin() ,axisythis.getMax());//设置坐标轴
        
        setAxisAndTitle();
        //坐标轴在figure最下方
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
	}
	
	protected void toImage(int width, int heigh) {
		int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
		//TODO 一直都是 不透明
    	bufferedImage = new BufferedImage(width, heigh, BufferedImage.TYPE_3BYTE_BGR);
    	if (bg != null && !bg.equals(new Color(0,0,0,0))) {
			setBG(width, heigh);
		}
    	DrawingContext context = new DrawingContext((Graphics2D) bufferedImage.getGraphics());
		plot.setBounds(0, 0, width, heigh);
		plot.draw(context);
	}
	/**
	 * 设定某个已经添加入plot中的dataseries的格式类型
	 * @param dataSeries
	 * @param dotStyle
	 */
	void setPointStyle(DataSource dataSeries, DotStyle dotStyle) {
		if (dotStyle.getStyle() == DotStyle.STYLE_AREA) {
            AreaRenderer area = new DefaultAreaRenderer2D();
            area.setSetting(AreaRenderer.COLOR, dotStyle.getColor());
            plot.setAreaRenderer(dataSeries, area);
            // Style data series
//	        PointRenderer points = new DefaultPointRenderer2D();
//	        points.setSetting(PointRenderer.SHAPE, new Rectangle2D.Double(0, 0, 0, 0));
//	        points.setSetting(PointRenderer.COLOR, new Color(0, 0, 0, 0));
//        	plot.setPointRenderer(dataSeries, points);
            plot.setPointRenderer(dataSeries, null);
		}
		else if (dotStyle.getStyle() == DotStyle.STYLE_LINE) {
			 DefaultLineRenderer2D line = new DefaultLineRenderer2D();
			 line.setSetting(DefaultLineRenderer2D.COLOR, dotStyle.getColor());
			 line.setSetting(DefaultLineRenderer2D.STROKE, dotStyle.getBasicStroke());
			 plot.setLineRenderer(dataSeries, line);
			 plot.setPointRenderer(dataSeries, null);
			 
			//TODO 设置成常规的line
//			plot.setSetting(BarPlot.BAR_WIDTH, 0.04);
//		    plot.getPointRenderer(dataSeries).setSetting(PointRenderer.COLOR, dotStyle.getColor());
		}
		else if (dotStyle.getStyle() == DotStyle.STYLE_BAR) {
			BarStyle barStyle = (BarStyle) dotStyle;
			if (barStyle.getBarWidth() != 0) {
				plot.setSetting(BarPlot.BAR_WIDTH, barStyle.getBarWidth());
			}
			PointRenderer pointRenderer = plot.getPointRenderer(dataSeries);
			pointRenderer.setSetting(PointRenderer.COLOR, barStyle.getColor());
			pointRenderer.setSetting(BarPlot.BarRenderer.STROKE, barStyle.getBasicStroke());
			pointRenderer.setSetting(BarPlot.BarRenderer.STROKE_COLOR, barStyle.getEdgeColor());		
			
			//规定，dotname在第3列，dotvalue也就是常规value在第二列
			//the third column is the name column
			pointRenderer.setSetting(PointRenderer.VALUE_COLUMN, 2);
			pointRenderer.setSetting(PointRenderer.VALUE_DISPLAYED, barStyle.isValueVisible());
			
		} else {
			// Style data series
	        PointRenderer points = new DefaultPointRenderer2D();
	        points.setSetting(PointRenderer.SHAPE, dotStyle.getShape());
	        points.setSetting(PointRenderer.COLOR, dotStyle.getColor());
	        plot.setPointRenderer(dataSeries, points);
		}
		//如果每个点的数值可见
		if ( dotStyle.isValueVisible()) {
			PointRenderer pointRenderer = plot.getPointRenderer(dataSeries);
			//如果没有点的渲染，譬如shape是没有点的，那么就新建透明点
			if (pointRenderer == null) {
				pointRenderer = new DefaultPointRenderer2D();
				pointRenderer.setSetting(PointRenderer.SHAPE,  new Ellipse2D.Double(1, 1, 1, 1));
				pointRenderer.setSetting(PointRenderer.COLOR, new Color(0, 0, 0, 0));
				plot.setPointRenderer(dataSeries, pointRenderer);
			}
			pointRenderer.setSetting(PointRenderer.VALUE_DISPLAYED, dotStyle.isValueVisible());
		}
		//规定，dotname在第3列，dotvalue也就是常规value在第二列
		if (!dotStyle.getName().equals("")) {
			PointRenderer pointRenderer = plot.getPointRenderer(dataSeries);
			//the third column is the name column，从0开始计数的
			pointRenderer.setSetting(PointRenderer.VALUE_COLUMN, 2);
		}
	}
	
	private void setAxisAndTitle() {
		// Style axes
		if (titleX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL, titleX);
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_DISTANCE, insetsX);
		}
		if (titleY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL, titleY);
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_DISTANCE, insetsY);
		}
		if (title != null) {
			plot.setSetting(BarPlot.TITLE, title);
		}
		
		if (spaceX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, spaceX);//坐标轴刻度
		}
		if (spaceY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, spaceY);//坐标轴刻度
		}
		if (mapAxisX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_CUSTOM, mapAxisX);//坐标轴刻度
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_SPACING, (axisX.getMax().doubleValue() - axisX.getMin().doubleValue())*2);//坐标轴刻度
		}
		if (mapAxisY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_CUSTOM, mapAxisY);//坐标轴刻度
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_SPACING, (axisY.getMax().doubleValue() - axisY.getMin().doubleValue())*2);//坐标轴刻度

		}
		
		if (fontTicksX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.TICKS_FONT, fontTicksX);//坐标轴刻度
		}
		if (fontTicksY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.TICKS_FONT, fontTicksY);//坐标轴刻度
		}
		
		if (fontX != null) {
			plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL_FONT, fontX);
		}
		if (fontY != null) {
			plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL_FONT, fontY);
		}
		if (fontTitle != null) {
			plot.setSetting(BarPlot.TITLE_FONT, fontTitle);
		}
		
		//background color
		if (!plotareaAll) {
			 plot.getPlotArea().setSetting(PlotArea.BACKGROUND, bg);
		}
		else {
			 plot.getPlotArea().setSetting(PlotArea.BACKGROUND, new Color(0, 0, 0, 0));
		}
		///////////////他自己提供的方法，限定一个方向的放大或者缩小//////////////////////
		///////////////以下没写完全
		if (Xnavigator && Ynavigator) {
			plot.getNavigator().setDirection(XYNavigationDirection.ARBITRARY);
		} else if (Xnavigator && !Ynavigator) {
			plot.getNavigator().setDirection(XYNavigationDirection.HORIZONTAL);
		} else if (!Xnavigator && Ynavigator) {
			plot.getNavigator().setDirection(XYNavigationDirection.VERTICAL);
		}
	}
	/**
	 * fill the insets with BG color, means fill the marginal area between the frame and the plot border.<br>
	 * because gral will not color the marginal area, so we should color the marginal area using this method
	 * @param width
	 * @param heigh
	 */
	private void setBG(int width, int heigh) {
		if (plotareaAll) {
			Graphics2D graphics = bufferedImage.createGraphics();
			graphics.setColor(bg);
			graphics.fillRect(0, 0, width, heigh);
		}
		//TODO 设置格子颜色
//		if (isBGgrid) {
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.BORDER, null);        // Remove border of plot area
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MAJOR_X, false); // Disable vertical grid
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MAJOR_Y, false); // Disable horizontal grid
//		} else {
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.BORDER, new BasicStroke(1f));        // Remove border of plot area
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MAJOR_X, true);
//			 plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MAJOR_Y, true);
//		}
//		if (colorBGgridMajor != null) {
//			plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MAJOR_COLOR, colorBGgridMajor);
//		}
//		if (colorBGgridMinor != null) {
//			plot.getPlotArea().setSetting(XYPlot.XYPlotArea2D.GRID_MINOR_COLOR, colorBGgridMajor);
//		}
	}
	/**
	 * 映射数字，就是将1-100映射成1000-1000000这种
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	public void setMapNum2ChangeX(double startTick, double startResult, 
			double endTick, double endResult, double intervalNumResult) {
		mapAxisX = mapNum2Change(startTick, startResult, endTick, endResult, intervalNumResult);
		painted = false;
	}
	/**
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	public void setMapNum2ChangeY(double startTick, double startResult, 
			double endTick, double endResult, double intervalNumResult) {
		mapAxisY = mapNum2Change(startTick, startResult, endTick, endResult, intervalNumResult);
		painted = false;
	}
	/**
	 * map the ticks number to actual axis, using the linear transformation 
	 * @return
	 */
	private Map<Double, String> mapNum2Change(double startTick, double startResult, 
			double endTick, double endResult, double intervalNumResult) {
		HashMap<Double, String> mapAxis = new HashMap<Double, String>();
		Equations equations = new Equations();
		ArrayList<double[]> lsXY = new ArrayList<double[]>();
		lsXY.add(new double[]{startResult, startTick});
		lsXY.add(new double[]{endResult, endTick});
		equations.setXY(lsXY);
		boolean decimals = true;//whether the axis ticks have dot, means have decimals
		if (intervalNumResult >= 1 || intervalNumResult <= -1) {
			decimals = false;
		}
		for (double i = startResult; i < endResult; i = i + intervalNumResult) {
			double tick = equations.getY(i);
			String tmpResult = i + "";
			if (!decimals) {
				tmpResult = (int)i + "";
			}
			mapAxis.put(tick, tmpResult);
		}
		return mapAxis;
	}
	
}
