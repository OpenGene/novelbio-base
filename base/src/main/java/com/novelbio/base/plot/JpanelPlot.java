package com.novelbio.base.plot;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;

import de.erichseifert.gral.graphics.Drawable;
import de.erichseifert.gral.graphics.DrawableContainer;
import de.erichseifert.gral.graphics.DrawingContext;
import de.erichseifert.gral.graphics.TableLayout;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.ui.InteractivePanel;
/**
 * using as Jpannel, and can add plot images such as heatmap and scatterplot and so on
 * @author zong0jie
 *
 */
public class JpanelPlot extends JPanel{
	public static final int PLOT_NORM = 100;
	public static final int PLOT_INTERACT = 200;
	
	private static final long serialVersionUID = -2731809645384427146L;
	Logger logger = Logger.getLogger(JpanelPlot.class);
	/**
	 * normal plot
	 */
	PlotNBC plotNBC;
	/**
	 * GRAL interactive pannel
	 */
	InteractivePanel inPanel;
	/**
	 * GRAL plot
	 */
	DrawableContainer plots;
	/**
	 * which figure type to plot
	 */
	int plotType = PLOT_NORM;
	
	public JpanelPlot() {
		super(new BorderLayout());
	}

	public void setPlotNBCInteractive(PlotNBCInteractive plotNBCInteractive) {
		if (plotNBCInteractive.isPlotareaAll()) {
			setBackground(plotNBCInteractive.getBg());
		}
		plotType = PLOT_INTERACT;
		if (inPanel == null) {
			plots = new DrawableContainer(new TableLayout(1));
			plots.add(plotNBCInteractive.getPlot());
			inPanel = new InteractivePanel(plots);
			inPanel.setZoomable(plotNBCInteractive.isZoom());
			inPanel.setPannable(plotNBCInteractive.isPannable());
			add(inPanel);
			repaint();
			return;
		}
		plots.add(plotNBCInteractive.getPlot());
		repaint();
	}
	/**
	 * add a drawable and set whether connect to other figure
	 * @param plotNBCInteractive 
	 * @param linked only xyplot canbe connected
	 */
	public void addPlotNBCInteractive(PlotNBCInteractive plotNBCInteractive, boolean linked) {
		if (plotNBCInteractive.isPlotareaAll()) {
			setBackground(plotNBCInteractive.getBg());
		}
		plotType = PLOT_INTERACT;
		if (inPanel == null) {
			plots = new DrawableContainer(new TableLayout(1));
			plots.add(plotNBCInteractive.getPlot());
			inPanel = new InteractivePanel(plots);
			add(inPanel);
			repaint();
			return;
		}
		plots.add(plotNBCInteractive.getPlot());
		for (Drawable drawable : plots) {
			try {
				XYPlot xyPlot = (XYPlot) drawable;
				XYPlot xyPlot2 = (XYPlot)plotNBCInteractive.getPlot();
				xyPlot.getNavigator().connect(xyPlot2.getNavigator());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		repaint();
	}
	public void addPlotNBCInteractive(PlotNBCInteractive plotNBCInteractive) {
		if (plotNBCInteractive.isPlotareaAll()) {
			setBackground(plotNBCInteractive.getBg());
		}
		plotType = PLOT_INTERACT;
		if (inPanel == null) {
			plots = new DrawableContainer(new TableLayout(1));
			plots.add(plotNBCInteractive.getPlot());
			inPanel = new InteractivePanel(plots);
			add(inPanel);
			repaint();
			return;
		}
		plots.add(plotNBCInteractive.getPlot());
		repaint();
	}
	/**
	 * 设定待画的图形
	 * @param plotNBC
	 */
	public void setPlotNBC(PlotNBC plotNBC) {
		plotType = PLOT_NORM;
		this.plotNBC = plotNBC;
	}
	
	boolean painted = false;
 
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (plotType == PLOT_INTERACT) {
			return;
		}
        this.setOpaque(true);
        g.setColor(plotNBC.getBg());
        plotNBC.drawData(this.getWidth(), this.getHeight());
        g.drawImage(plotNBC.getBufferedImage(), 0, 0, this);
//        g.finalize();
      }
    
    
    /**
     * 默认保存为jpg格式
	 * Generates a new chart <code>Image</code> based upon the currently held 
	 * settings and then attempts to save that image to disk, to the location 
	 * provided as a File parameter. The image type of the saved file will 
	 * equal the extension of the filename provided, so it is essential that a 
	 * suitable extension be included on the file name.
	 * 
	 * <p>
	 * All supported <code>ImageIO</code> file types are supported, including 
	 * PNG, JPG and GIF.
	 * <p>
	 * No chart will be generated until this or the related 
	 * <code>getChartImage()</code> method are called. All successive calls 
	 * will result in the generation of a new chart image, no caching is used.
     * @param outputFileName the file location that the generated image file should 
	 * be written to. The File must have a suitable filename, with an extension
	 * of a valid image format (as supported by <code>ImageIO</code>).
     * @param Width
     * @param Height
     * @param transpreat 是否透明
     * @throws IOException if the output file's filename has no extension or 
	 * if there the file is unable to written to. Reasons for this include a 
	 * non-existant file location (check with the File exists() method on the 
	 * parent directory), or the permissions of the write location may be 
	 * incorrect.
	 */
	public void saveToFile(String outputFileName, int Width, int Height) {
		if (plotType == PLOT_NORM) {
			plotNBC.saveToFile(outputFileName, Width, Height);
			return;
		}
		BufferedImage bufferedImage = toImageInterAct(true, Width, Height);
		String filename = FileOperate.getFileNameSep(outputFileName)[1];
		if (filename.equals("")) {
			outputFileName = FileOperate.changeFileSuffix(outputFileName, null, ".jpg");
		}
    	BufferedImage bufferedImageResult = PlotNBC.paintGraphicOut(bufferedImage, null, true, Width, Height);
    	PlotNBC.saveGraphic(bufferedImageResult, outputFileName, 1.0f);
	}
	private BufferedImage toImageInterAct(Boolean alpha, int Width, int Height) {
		int imageType = (alpha ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage bufferedImage = new BufferedImage(Width,Height, imageType);
		DrawingContext context = new DrawingContext((Graphics2D) bufferedImage.getGraphics());
		Rectangle2D rectangle2d = plots.getBounds();
		plots.setBounds(0, 0, Width, Height);
		plots.draw(context);
		plots.setBounds(rectangle2d);
		return bufferedImage;
	}
    
 
}
