package com.novelbio.base.plot;

import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.plots.XYPlot;

/**
 * 互动的plot，调用GRAL包实现
 * @author zong0jie
 *
 */
public abstract class PlotNBCInteractive extends PlotNBC{
	   
    /**
     * set whether axis can be moved when moving or zooming
     * @param move true: can move and zoom X axis
     * false: cannot move and zoom X axis
     */
    boolean Xnavigator = true;
    /**
     * set which axis can be moved when moving or zooming
     * @param move true: can move and zoom Y axis
     * false: cannot move and zoom Y axis
     */
    boolean Ynavigator = true;
    
    /** 能否放大缩小 */
    boolean zoom = true;
    /** 能否移动 */
    boolean pannable = true;
    /** check whether just figure the figure or all the picture area(include the axis margn)  */
	boolean plotareaAll = true;
    /**
     * set whether axis can be moved when moving or zooming
     * @param move true: can move and zoom X axis
     * false: cannot move and zoom X axis
     */
    public void setAxisXNavigator(boolean move) {
    	this.Xnavigator = move;
	}
    /**
     * set which axis can be moved when moving or zooming
     * @param move true: can move and zoom Y axis
     * false: cannot move and zoom Y axis
     */
    public void setAxisYNavigator(boolean move) {
    	this.Ynavigator = move;
	}

    /**
     * 默认为true
     * @return
     */
    protected boolean isZoom() {
		return zoom;
	}
    /**
     * 默认为true
     * @return
     */
    public void setZoom(boolean zoom) {
		this.zoom = zoom;
	}
    /**
     * 能否移动，默认为true
     * @param pannable
     */
    public void setPannable(boolean pannable) {
		this.pannable = pannable;
	}
    /**
     * 能否移动，默认为true
     * @param pannable
     */
    public boolean isPannable() {
		return pannable;
	}

	/**
	 * check whether just figure the figure or all the picture area(include the axis margn)
	 * @param plotareaAll default true
	 */
	public void setPlotareaAll(boolean plotareaAll) {
		this.plotareaAll = plotareaAll;
	}
	public boolean isPlotareaAll() {
		return plotareaAll;
	}
	/**
	 * needs check
	 * @return
	 */
	public abstract Drawable getPlot();
	
	
	
	
	
}
