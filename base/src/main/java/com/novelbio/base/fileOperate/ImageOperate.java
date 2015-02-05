package com.novelbio.base.fileOperate;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

public class ImageOperate {
	
	/** 将tiff图片转换成png图片 */
	public final static void convertTiff2Png(String tiffImagePath, String pngImagePath) throws IOException {
		BufferedImage bufferedImage = getTiffBufferedImage(tiffImagePath);
        File out = FileOperate.getFile(pngImagePath);
		ImageIO.write(bufferedImage, "png", out);
	}
	
	/** 获取tiff格式图片的BufferedImage，需传入图片的全路经 */
	public final static BufferedImage getTiffBufferedImage(String tiffImagePath) throws IOException {
			File file =  FileOperate.getFile(tiffImagePath);
			SeekableStream seekableStream = new FileSeekableStream(file);
			ParameterBlock parameterBlock = new ParameterBlock();
			parameterBlock.add(seekableStream);
			RenderedOp renderedOp = JAI.create("tiff", parameterBlock);
			String[] lsPropertyName = renderedOp.getPropertyNames();
			Hashtable<String, Object> properties = new Hashtable<String, Object>();
			for(int i = 0; i < lsPropertyName.length; i++) {
				properties.put(lsPropertyName[i], renderedOp.getProperty(lsPropertyName[i]));
			}
			BufferedImage cache_buffer = new BufferedImage(renderedOp.getColorModel(), (WritableRaster)renderedOp.getData(), false, properties);
			return cache_buffer;
	}

}
