package com.novelbio.base.plot.heatmap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.GraphicCope;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * <p>This class is a very simple example of how to use the HeatMap class.</p>
 *
 * <hr />
 * <p><strong>Copyright:</strong> Copyright (c) 2007, 2008</p>
 *
 * <p>HeatMap is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.</p>
 *
 * <p>HeatMap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU General Public License
 * along with HeatMap; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA</p>
 *
 * @author Matthew Beckler (matthew@mbeckler.org)
 * @author Josh Hayes-Sheen (grey@grevian.org), Converted to use BufferedImage.
 * @author J. Keller (jpaulkeller@gmail.com), Added transparency (alpha) support, data ordering bug fix.
 * @version 1.6
 */

class HeatMapFrame extends JFrame
{
    PlotHeatMap panel;

    public HeatMapFrame() throws Exception
    {
        super("Heat Map Frame");
        
        boolean useGraphicsYAxis = true;
        
        // you can use a pre-defined gradient:
       
        
        // or you can also make a custom gradient:
        Color colorred = new Color(255, 0, 0, 255);
        Color colorwhite = new Color(0, 0, 0, 255);
        Color colorgreen = new Color(0, 255, 0, 255);
        Color[] gradientColors2 = new Color[]{colorwhite, colorgreen};
        Color[] gradientColors = new Color[]{colorwhite, colorred};
        
        
        Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
        
        Color[] customGradient2 = Gradient.createMultiGradient(gradientColors2, 250);
        
        int m = 0;
        double[][] data = new double[10][10];
        for (int j = 0; j < data[0].length; j++) {
			for (int i = 0; i < data.length; i++) {
				data[i][j] = m;
				m++;
			}
		}
        
        
        m = 0;
        double[][] data2 = new double[10][10];
        for (int i = 0; i < data2.length; i++) {
			for (int j = 0; j < data2[0].length; j++) {
				data2[i][j] = m;
				m++;
			}
		}
        
        panel = new PlotHeatMap(data,data2, true, customGradient,customGradient2);
        panel.setRange(2, 80, 2, 80);
//        data = panel.generatePyramidData(100);
//        panel.setRange(10, 30);
        // set miscelaneous settings
//        panel.setDrawLegend(true);
//
//        panel.setTitle("Height (m)");
//        panel.setDrawTitle(true);
// 
//        panel.setXAxisTitle("X-Distance (m)");
//        panel.setDrawXAxisTitle(true);
//
//        panel.setYAxisTitle("Y-Distance (m)");
//        panel.setDrawYAxisTitle(true);
//
//        panel.setCoordinateBounds(0, 6.28, 0, 6.28);
//        panel.setDrawXTicks(true);
//        panel.setDrawYTicks(true);
//        panel.setColorForeground(Color.white);
//        panel.setColorBackground(Color.white);
//        panel.setAlpha(true);
//        panel.saveToFile("/home/zong0jie/桌面/testimagePan2.png",500,500,true);
//        this.getContentPane().add(panel);
        this.createImage(1000, 1000);
//        saveToFile("/home/zong0jie/桌面/testimage.png",1000,1000);
    }
    
    // this function will be run from the EDT
    private static void createAndShowGUI() throws Exception
    {
        HeatMapFrame hmf = new HeatMapFrame();
        hmf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hmf.setSize(500,500);
        hmf.setVisible(true);
//        hmf.saveImage(hmf.panel,"/home/zong0jie/桌面/testimageAll.png");
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    createAndShowGUI();
                }
                catch (Exception e)
                {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 将窗体组件1：1截图并保存为jpg
     * @param com
     * @param path
     */
     public void saveImage(Component com,String path){  
    	        int comH=com.getHeight();  
    	        int comW=com.getWidth();  
    	        BufferedImage bufferImage=new BufferedImage(comW,comH,BufferedImage.TYPE_INT_RGB);  
    	        
    	        Graphics2D g2=bufferImage.createGraphics();
    	        g2.setBackground(Color.white);
    	        
    	        com.paint(g2);
    	        g2.dispose();  
    	        bufferImage = GraphicCope.resizeImage(bufferImage, 100, 100);
    	        try {
					saveGraphicJpeg(bufferImage,path, 1.0f);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }  
    
    
    
    
    
    
    
    
    
    
    
    /**
	 * Generates a new chart <code>Image</code> based upon the currently held 
	 * settings and then attempts to save that image to disk, to the location 
	 * provided as a File parameter. The image type of the saved file will 
	 * equal the extension of the filename provided, so it is essential that a 
	 * suitable extension be included on the file name.
	 * 
	 * <p>
	 * All supported <code>ImageIO</code> file types are supported, including 
	 * PNG, JPG and GIF.
	 * 
	 * <p>
	 * No chart will be generated until this or the related 
	 * <code>getChartImage()</code> method are called. All successive calls 
	 * will result in the generation of a new chart image, no caching is used.
	 * 
	 * @param outputFile the file location that the generated image file should 
	 * be written to. The File must have a suitable filename, with an extension
	 * of a valid image format (as supported by <code>ImageIO</code>).
	 * @throws IOException if the output file's filename has no extension or 
	 * if there the file is unable to written to. Reasons for this include a 
	 * non-existant file location (check with the File exists() method on the 
	 * parent directory), or the permissions of the write location may be 
	 * incorrect.
	 */
	public void saveToFile(String outputFileName, int Width, int Height) throws IOException {
		File outputFile = new File(outputFileName);
		
		String filename = FileOperate.getFileName(outputFileName);
		
		int extPoint = filename.lastIndexOf('.');

		if (extPoint < 0) {
			throw new IOException("Illegal filename, no extension used.");
		}
		Image chartImage = this.createImage(Width, Height);
//		System.out.println(this.getWidth());
//		int width = chartImage.getWidth(null);  
//		int height = chartImage.getHeight(null); 
		BufferedImage image = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_RGB);
		 Graphics2D g2=image.createGraphics();  
		 paint(g2);
		 g2.dispose();
		// Determine the extension of the filename.
		String ext = filename.substring(extPoint + 1);
		// Handle jpg without transparency.
		if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
			// Save our graphic.
			saveGraphicJpeg(image, outputFileName, 1.0f);
		} else {
			ImageIO.write(image, ext, outputFile);
		}
	}
	
	
	
	private void saveGraphicJpeg(BufferedImage chart, String pathname, float quality) throws IOException {
		File outputFile = new File(pathname);
		// Setup correct compression for jpeg.
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer = (ImageWriter) iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality);
		
		// Output the image.
		FileImageOutputStream output = new FileImageOutputStream(outputFile);
		writer.setOutput(output);
		IIOImage image = new IIOImage(chart, null, null);
		writer.write(null, image, iwp);
		writer.dispose();
	}
}
