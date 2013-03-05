package com.novelbio.base.plot;

import java.awt.image.BufferedImage;

public class PlotBufferedImage extends PlotNBC{
    public void setBufferedImage(BufferedImage bufferedImage) {
    	this.bufferedImage = bufferedImage;
	}
	@Override
	protected void draw(int width, int heigh) {
		bufferedImage = paintGraphicOut(bufferedImage, fg, alpha, width, heigh);
	}
	
	

}
