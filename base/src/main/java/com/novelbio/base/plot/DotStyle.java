package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import de.erichseifert.gral.util.GraphicsUtils;

/**
 * 将数据的点分成几块，每一块都标记不同的颜色和点的样式
 * 重写了equal但是没有重写hash
 * 最好是clone了使用
 * @author zong0jie
 *
 */
public class DotStyle implements Cloneable {
	/**
	 * 画面积图，在基因组上测试过了，效果不错
	 * Area没有线
	 */
	public static final int STYLE_AREA = 2;
	public static final int STYLE_CYCLE = 4;
	public static final int STYLE_RECTANGLE = 8;
	public static final int STYLE_TRIANGLE = 16;
	public static final int STYLE_LINE = 32;
	public static final int STYLE_BAR = 64;
	public static final int STYLE_BOX = 128;
	
	
	public static final int SIZE_S = 128;
	public static final int SIZE_SM = 256;
	public static final int SIZE_M = 512;
	public static final int SIZE_MB = 1024;
	public static final int SIZE_B = 2048;
	
	Ellipse2D.Double circle = null;
	Rectangle2D.Double rectangele = null;
	Rectangle2D.Double line = null;
	Polygon TRIANGLE = null;
	/**
	 * 颜色
	 */
	Paint color = Color.BLACK;
	/**
	 * 形状
	 */
	int style = STYLE_CYCLE;
	int size = SIZE_M;
	/**
	 * whether the point can be seen
	 */
	boolean valueVisible = false;
	/**
	 * whether the value of a point can be seen
	 * default is false
	 * @param visible
	 */
	public void setValueVisible(boolean valueVisible) {
		this.valueVisible = valueVisible;
	}
	/**
	 * whether the value of a point can be seen
	 * default is false
	 * @param visible
	 */
	public boolean isValueVisible() {
		return valueVisible;
	}
	
	/**
	 * 设定大小
	 * SIZE_M等
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	/**
	 * if want to set the point unvisible, just set color as blank
	 * @param color
	 */
	public void setColor(Paint color) {
		this.color = color;
	}
	public Paint getColor() {
		return color;
	}
	public int getSize() {
		return size;
	}
	public int getStyle() {
		return style;
	}
	/**
	 * whether dot have a string name
	 */
	String dotname = null;
	/**
	 * 设置该点显示的文字，如果不设置，就为该点的y轴坐标
	 * @param dotname
	 */
	public void setName(String dotname) {
		this.dotname = dotname;
	}
	/**
	 * 该点显示的文字，如果不设置，则返回null
	 * @return
	 */
	public String getName() {
		if (dotname == null) {
			return "";
		}
		return dotname;
	}
	/**
	 * 获得想要的图形，目前仅支持line，circle，rectangle
	 * @param 需要扩大的倍数，高精度就扩大个3-5倍
	 * @return
	 * 如果是线型，则需要在外面修正一下
	 */
	public Shape getShape() {
		if (style == STYLE_AREA || style == STYLE_BAR) {
			return null;
		}
		else if (style == STYLE_CYCLE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			circle = new Ellipse2D.Double(x, y, w, h);
			return circle;
		}
		else if (style == STYLE_RECTANGLE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			rectangele = new Rectangle2D.Double(x, y, w, h);
			return rectangele;
		}
		else if (style == STYLE_LINE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			rectangele = new Rectangle2D.Double(x, y, w, h);
			return rectangele;
		}
		return null;
	}
	/**
	 * 获得想要的图形，目前仅支持line，circle，rectangle
	 * @param 需要扩大的倍数，高精度就扩大个3-5倍
	 * @return
	 * 如果是线型，则需要在外面修正一下
	 */
	public BasicStroke getBasicStroke() {
		if (style == STYLE_LINE) {
			double x = 0; double y = 0;
			double w = 0; double h = 0;
			if (size == SIZE_S) {
				w = 0.5; h = 0.5;
			}
			else if (size == SIZE_SM) {
				w = 1.0; h = 1.0;
			}
			else if (size == SIZE_M) {
				w = 2.0; h = 2.0;
			}
			else if (size == SIZE_MB) {
				w = 3.0; h = 3.0;
			}
				
			else if (size == SIZE_B) {
				w = 4.0; h = 4.0;
			}
			return new BasicStroke((float)w);
		}
		return null;
	}
	/**
	 * 复制一个dotstyle
	 */
	@Override
	public DotStyle clone() {
		try {
			DotStyle dotStyle = (DotStyle) super.clone();
			dotStyle.color = color;
			dotStyle.dotname = dotname;
			dotStyle.size = size;
			dotStyle.style = style;
			dotStyle.circle = circle;
			dotStyle.line = line;
			dotStyle.rectangele = rectangele;
			dotStyle.TRIANGLE = TRIANGLE;
			dotStyle.valueVisible = valueVisible;
			return dotStyle;	
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 重写equals
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;
		DotStyle otherObj = (DotStyle) obj;

		if (this.color.equals(otherObj.color)
				&& this.dotname.equals(otherObj.dotname)
				&& this.size == otherObj.size
				&& this.style == otherObj.style
				) {
			return true;
		}
		return false;
	}
	
	public static Paint getGridentColorBrighter(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color, GraphicsUtils.deriveBrighter(color) }
			                      );
	}
	public static Paint getGridentColorBrighterTrans(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { GraphicsUtils.deriveBrighter(color), color }
			                      );
	}
	public static Paint getGridentColorDarker(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color, GraphicsUtils.deriveDarker(color) }
			                      );
	}
	public static Paint getGridentColorDarkerTrans(Color color) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { GraphicsUtils.deriveDarker(color) ,color}
			                      );
	}
	public static Paint getGridentColor(Color color1, Color color2) {
		return new LinearGradientPaint(0f,0f, 0f,1f,
			                                        new float[] { 0.0f, 1.0f },
				                                      new Color[] { color1, color2 }
			                      );
	}
}
