package com.novelbio.base.plot;

import java.awt.BasicStroke;
import java.awt.Paint;
/**
 * 最好是clone了使用
 * @author zong0jie
 *
 */
public class BarStyle extends DotStyle {
	Paint barEdgeColor = null;
	/**
	 *  paint the outline of the point shape.
	 */
	BasicStroke basicStroke = null;
	/**
	 * if the style is bar, this value is the bar width info
	 */
	double barWidth = 0;
	/**
	 * always return BAR
	 */
	public int getStyle() {
		return STYLE_BAR;
	}
	
	/**
	 * 连了边框一起设定宽度
	 */
	public void setBarAndStrokeWidth(double barWidth) {
		this.barWidth = barWidth;
		this.basicStroke = new BasicStroke(3f);
	}

	/**
	 * if the style is bar, set the bar width info
	 * @param barWidth
	 */
	public void setBarWidth(double barWidth) {
		this.barWidth = barWidth;
	}
	/**
	 * if the style is bar, get the bar width info
	 * @return
	 */
	public double getBarWidth() {
		return barWidth;
	}
	
	/**
	 *  paint the outline of the point shape.
	 */
	public void setBasicStroke(float width) {
		this.basicStroke = new BasicStroke(width);
	}
	
	public BarStyle clone() {
		BarStyle barStyle = (BarStyle) super.clone();
		barStyle.barEdgeColor = barEdgeColor;
		barStyle.basicStroke = basicStroke;
		barStyle.barWidth = barWidth;
		return barStyle;
	}
	/**
	 *  paint the outline of the point shape.
	 */
	public BasicStroke getBasicStroke() {
		return basicStroke;
	}
	public void setColorEdge(Paint barEdgeColor) {
		this.barEdgeColor = barEdgeColor;
	}
	public Paint getEdgeColor() {
		return barEdgeColor;
	}
}
