package com.novelbio.base.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.faceless.graph2.Output;
import org.faceless.graph2.SVGOutput;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HdfsBase;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 对BufferedImage做各种处理<br>
 * 如果要读取或者写入BufferedImage，可以用ImageIO类
 * 
 * @author zong0jie
 * 
 */
public class GraphicCope {
	public static void main(String[] args) throws Exception {
//		BufferedImage bufferedImage = convertSvg2BfImg("/home/zong0jie/desktop/IdegramSsSc0707031_v3.svg", 1.0);
//		try {
//			ImageIO.write(bufferedImage, "png", new File("/home/zong0jie/desktop/2.png"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	/**
	 * 将svg转化为BufferedImage，方便后期处理
	 * 
	 * @param svgFile
	 * @param zoomeSize
	 *            相对于svg的原始图片进行缩放
	 * @return 出错返回null
	 */
	public static BufferedImage convertSvg2BfImg(String svgFile,
			double zoomeSize) {
		double[] svgResolution = getResolution(svgFile);
		float width = (float) (zoomeSize * svgResolution[0]);
		float heigth = (float) (zoomeSize * svgResolution[1]);
		try {
			return convertSvg2BfImgExp(svgFile, width, heigth);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 将svg转化为BufferedImage，方便后期处理
	 * 
	 * @param svgFile
	 * @param width
	 *            如果为0，则走svg的默认值
	 * @param height
	 *            如果小于等于0，则按照width进行按比例缩放
	 * @return 出错返回null
	 */
	public static BufferedImage convertSvg2BfImg(String svgFile, double width,
			double heigth) {
		double[] svgResolution = getResolution(svgFile);
		if (width <= 0) {
			width = svgResolution[0];
			heigth = svgResolution[1];
		} else if (width > 0 && heigth <= 0) {
			heigth = (svgResolution[1] * width / svgResolution[0]);
		}
		try {
			return convertSvg2BfImgExp(svgFile, (float) width, (float) heigth);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 保存bufferImage为图片文件
	 * 
	 * @param chart
	 *            图表
	 * @param outputFile
	 *            输出路径带有文件名如：/home/novelbio/aaa.png
	 */
	public static void saveBufferedImage(BufferedImage chart, String outputFile) {
		String ext = FileOperate.getFileNameSep(outputFile)[1];
		OutputStream out = null;
		try {
			if (HdfsBase.isHdfs(outputFile)) {
				FileHadoop fileHadoop = new FileHadoop(outputFile);
				out = fileHadoop.getOutputStreamNew(true);
			} else {
				out = new FileOutputStream(outputFile);
			}
			// Handle jpg without transparency.
			if (ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
				try {
					ImageIO.write(chart,  "jpg", out);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					Iterator<ImageWriter> writers = ImageIO
							.getImageWritersByMIMEType("image/png");
					while (writers.hasNext()) {
						ImageWriter writer = writers.next();
						ImageOutputStream ios = ImageIO
								.createImageOutputStream(out);
						writer.setOutput(ios);
						try {
							writer.write(chart);
						} finally {
							ios.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

	/**
	 * 将svg转化为BufferedImage，方便后期处理
	 * 
	 * @param svgFile
	 * @param width
	 *            如果为0，则走svg的默认值
	 * @param height
	 *            如果小于等于0，则按照width进行按比例缩放
	 * @return
	 * @throws Exception
	 */
	private static BufferedImage convertSvg2BfImgExp(String svgFile,
			float width, float heigth) throws Exception {
		BufferedImageTranscoder imageTranscoder = new BufferedImageTranscoder();

		imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, width);
		imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, heigth);

		String svgURI = new File(svgFile).toURL().toString();
		TranscoderInput input = new TranscoderInput(svgURI);
		imageTranscoder.transcode(input, null);

		return imageTranscoder.getBufferedImage();
	}

	/**
	 * 保存bcf生成的图表并移除水印
	 * 
	 * @param output
	 *            bfo提供的输出流，目前只支持{@link org.faceless.graph2.SVGOutput}
	 * @param path
	 *            全路径包括文件名如/home/novelbio/aaa.svg
	 * @return 是否成功
	 */
	public static boolean saveAsSvgAndRemoveWatermark(Output output, String path) {
		if (!(output instanceof SVGOutput)) {
			return false;
		}
		TxtReadandWrite txtReadandWrite = null;
		TxtReadandWrite txtReadandWriteNew = null;
		try {
			String tempSvgFilePath = FileOperate.addSep(FileOperate
					.getParentPathName(path)) + DateUtil.getDateAndRandom();
			((SVGOutput) output).writeSVG(new FileWriter(new File(
					tempSvgFilePath)), true);
			txtReadandWrite = new TxtReadandWrite(tempSvgFilePath, false);
			txtReadandWriteNew = new TxtReadandWrite(path, true);
			for (String string : txtReadandWrite.readlines()) {
				if (string
						.endsWith("style=\"fill-opacity:0.149\" pointer-events=\"none\">DEMO</text>"))
					continue;
				txtReadandWriteNew.writefileln(string);
			}
			FileOperate.delFile(tempSvgFilePath);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			txtReadandWriteNew.close();
			txtReadandWrite.close();
		}
		return true;
	}

	/** 获得svg文件的长宽 */
	private static double[] getResolution(String svgFile) {
//		PatternOperate patternOperate = new PatternOperate("\\d+(\\.?\\d+){0,1}", false);
		TxtReadandWrite txtRead = new TxtReadandWrite(svgFile);
		double[] resolution = new double[2];
		String allContent = "";
		for (String content : txtRead.readlines()) {
//			content = content.trim().toLowerCase();
//			if (content.startsWith("width=")) {
//				String info = content.replace("width=", "").replace("\"", "");
//				resolution[0] = Double.parseDouble(patternOperate.getPatFirst(info));
//			}
//			if (content.startsWith("height=")) {
//				String info = content.replace("height=", "").replace("\"", "");
//				resolution[1] = Double.parseDouble(patternOperate.getPatFirst(info));
//			}
//			if (resolution[0] > 0 && resolution[1] > 0) {
//				break;
//			}
			allContent += content;
		}
		Pattern pattern = Pattern.compile("(?<=width=\").+?(?=\")");
		Pattern pattern0 = Pattern.compile("(?<=height=\").+?(?=\")");
		Pattern pattern1 = Pattern.compile("(?<=<svg).+?(?=>)");
		Matcher matcher1 = pattern1.matcher(allContent);
		if (matcher1.find()){
			Matcher matcher = pattern.matcher(matcher1.group());
			if (matcher.find()){
				resolution[0] = Double.parseDouble(matcher.group());
			}
			Matcher matcher0 = pattern0.matcher(matcher1.group());
			if (matcher0.find()){
				resolution[1] = Double.parseDouble(matcher0.group());
			}
		}
		txtRead.close();
		return resolution;
	}

	/**
	 * 旋转图片为指定角度
	 * 
	 * @param bufferedimage
	 *            目标图像
	 * @param degree
	 *            旋转角度 顺时针旋转
	 * @return
	 */
	public static BufferedImage rotateImage(final BufferedImage bufferedimage,
			final int degree) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		int type = bufferedimage.getColorModel().getTransparency();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, type)).createGraphics())
				.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
		graphics2d.drawImage(bufferedimage, 0, 0, null);
		graphics2d.dispose();
		return img;
	}
	
	/**
	 * 变更图像为指定大小
	 * 
	 * @param bufferedimage
	 *            目标图像
	 * @param w
	 *            宽
	 * @param h
	 *            高
	 * @return
	 */
	public static BufferedImage resizeImage(final BufferedImage bufferedimage,
			final int w, final int h) {
		return resizeImage(true, bufferedimage, w, h);
	}

	/**
	 * 变更图像为指定大小
	 * 
	 * @param liner
	 *            是否线性缩放图片
	 * @param bufferedimage
	 *            目标图像
	 * @param w
	 *            宽
	 * @param h
	 *            高
	 * @return
	 */
	public static BufferedImage resizeImage(boolean liner,
			BufferedImage bufferedimage, final int w, final int h) {
		BufferedImage img;
		if (liner) {
			img = new ImageScale().imageZoomOut(bufferedimage, w, h);
		} else {
			int type = bufferedimage.getColorModel().getTransparency();
			Graphics2D graphics2d;
			(graphics2d = (img = new BufferedImage(w, h, type))
					.createGraphics()).setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2d.drawImage(bufferedimage, 0, 0, w, h, 0, 0,
					bufferedimage.getWidth(), bufferedimage.getHeight(), null);
			graphics2d.dispose();
		}
		return img;
	}

	/**
	 * 水平翻转图像
	 * 
	 * @param bufferedimage
	 *            目标图像
	 * @return
	 */
	public static BufferedImage flipImage(final BufferedImage bufferedimage) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, bufferedimage
				.getColorModel().getTransparency())).createGraphics())
				.drawImage(bufferedimage, 0, 0, w, h, w, 0, 0, h, null);
		graphics2d.dispose();
		return img;
	}

	/**
	 * 图片水印
	 * 
	 * @param pressImg
	 *            水印图片
	 * @param targetImg
	 *            目标图片
	 * @param x
	 *            修正值 默认在中间
	 * @param y
	 *            修正值 默认在中间
	 * @param alpha
	 *            透明度
	 */
	public final static void pressImage(String pressImg, String targetImg,
			float alpha, String out) {
		// int x, int y, float alpha) {
		try {
			File img = new File(targetImg);
			Image src = ImageIO.read(img);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			// 第一个也透明化
			// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
			// alpha));
			//
			g.drawImage(src, 0, 0, wideth, height, null);
			// 水印文件
			Image src_biao = ImageIO.read(new File(pressImg));
			int wideth_biao = src_biao.getWidth(null);
			int height_biao = src_biao.getHeight(null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					alpha));
			g.drawImage(src_biao, (wideth - wideth_biao) / 2,
					(height - height_biao) / 2, wideth_biao, height_biao, null);
			// 水印文件结束
			g.dispose();

			File fileOut = new File(out);

			ImageIO.write((BufferedImage) image, "jpg", fileOut);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 文字水印
	 * 
	 * @param pressText
	 *            水印文字
	 * @param targetImg
	 *            目标图片
	 * @param fontName
	 *            字体名称
	 * @param fontStyle
	 *            字体样式
	 * @param color
	 *            字体颜色
	 * @param fontSize
	 *            字体大小
	 * @param x
	 *            修正值
	 * @param y
	 *            修正值
	 * @param alpha
	 *            透明度
	 */
	public static void pressText(String pressText, String targetImg,
			String fontName, int fontStyle, Color color, int fontSize, int x,
			int y, float alpha) {
		try {
			File img = new File(targetImg);
			Image src = ImageIO.read(img);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);
			g.setColor(color);
			g.setFont(new Font(fontName, fontStyle, fontSize));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					alpha));
			g.drawString(pressText, (width - (getLength(pressText) * fontSize))
					/ 2 + x, (height - fontSize) / 2 + y);
			g.dispose();
			ImageIO.write((BufferedImage) image, "jpg", img);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 缩放
	 * 
	 * @param filePath
	 *            图片路径
	 * @param height
	 *            高度
	 * @param width
	 *            宽度
	 * @param bb
	 *            比例不对时是否需要补白
	 */
	public static void resize(String filePath, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // 缩放比例
			File f = new File(filePath);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
			// 计算比例
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				if (bi.getHeight() > bi.getWidth()) {
					ratio = (new Integer(height)).doubleValue()
							/ bi.getHeight();
				} else {
					ratio = (new Integer(width)).doubleValue() / bi.getWidth();
				}
				AffineTransformOp op = new AffineTransformOp(
						AffineTransform.getScaleInstance(ratio, ratio), null);
				itemp = op.filter(bi, null);
			}
			if (bb) {
				BufferedImage image = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, width, height);
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
							itemp.getWidth(null), itemp.getHeight(null),
							Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
							itemp.getWidth(null), itemp.getHeight(null),
							Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write((BufferedImage) itemp, "jpg", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static int getLength(String text) {
		int length = 0;
		for (int i = 0; i < text.length(); i++) {
			if (new String(text.charAt(i) + "").getBytes().length > 1) {
				length += 2;
			} else {
				length += 1;
			}
		}
		return length / 2;
	}
	
	/**
	 * @param horizon 是否为水平连接，否则为垂直合并
	 * @param sepPix 两张图片中间间隔多少像素
	 * @param bufferedImages
	 * @return
	 */
	public static BufferedImage combineBfImage( boolean horizon, int sepPix, BufferedImage... bufferedImages) {
		List<BufferedImage> lsBufferedImages = ArrayOperate.converArray2List(bufferedImages);
		return combineBfImage(horizon, sepPix, lsBufferedImages);
	}
	/**
	 * @param horizon 是否为水平连接，否则为垂直合并
	 * @param sepPix 两张图片中间间隔多少像素
	 * @param bufferedImages
	 * @return
	 */
	public static BufferedImage combineBfImage( boolean horizon, int sepPix, List<BufferedImage> bufferedImages) {
		if (bufferedImages.size() == 0) return null;
		
		int type = ColorModel.TRANSLUCENT;
		int width = bufferedImages.get(0).getWidth(), height = bufferedImages.get(0).getHeight();
		for (int i = 1; i < bufferedImages.size(); i++) {
			BufferedImage bufferedImage = bufferedImages.get(i);
			if (horizon) {
				width = width + sepPix + bufferedImage.getWidth();
			} else {
				height = height + sepPix + bufferedImage.getHeight();
			}
		}
		
		BufferedImage bufferedImageResult = new BufferedImage(width, height, type);
		Graphics2D graphics2d = bufferedImageResult.createGraphics();
		int xLoc = 0, yLoc = 0;
		for (BufferedImage bufferedImage : bufferedImages) {
			graphics2d.drawImage(bufferedImage, xLoc, yLoc, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
			if (horizon) {
				xLoc = xLoc + bufferedImage.getWidth() + sepPix;
			} else {
				yLoc = yLoc + bufferedImage.getHeight() + sepPix;
			}
		}

		return bufferedImageResult;
	}
	
}

class ImageScale {
	/**
	 * 图片无损缩放 try { File fi = new File("c:/image2.jpg"); // 大图文件 File fo = new
	 * File("c:/imgTest.jpg"); // 将要转换出的小图文件 BufferedImage bis =
	 * ImageIO.read(fi); BufferedImage bid = null; bid = new
	 * ImageScale().imageZoomOut(bis, 228, 96); ImageIO.write(bid, "jpeg", fo);
	 * } catch (Exception e) { e.printStackTrace(); }
	 */
	private int width;
	private int height;
	private int scaleWidth;

	double support = (double) 3.0;
	double PI = (double) 3.14159265358978;
	double[] contrib;
	double[] normContrib;
	double[] tmpContrib;
	int startContrib, stopContrib;
	int nDots;
	int nHalfDots;

	/**
	 * Start: Use Lanczos filter to replace the original algorithm for image
	 * scaling. Lanczos improves quality of the scaled image modify by :blade
	 */
	public BufferedImage imageZoomOut(BufferedImage srcBufferImage, int w, int h) {
		width = srcBufferImage.getWidth();
		height = srcBufferImage.getHeight();
		scaleWidth = w;
		if (DetermineResultSize(w, h) == 1) {
			return srcBufferImage;
		}
		CalContrib();
		BufferedImage pbOut = HorizontalFiltering(srcBufferImage, w);
		BufferedImage pbFinalOut = VerticalFiltering(pbOut, h);
		return pbFinalOut;
	}

	private int DetermineResultSize(int w, int h) {
		double scaleH, scaleV;
		scaleH = (double) w / (double) width;
		scaleV = (double) h / (double) height;

		if (scaleH >= 1.0 && scaleV >= 1.0) {
			return 1;
		}
		return 0;
	}

	private double Lanczos(int i, int inWidth, int outWidth, double Support) {
		double x;
		x = (double) i * (double) outWidth / (double) inWidth;
		return Math.sin(x * PI) / (x * PI) * Math.sin(x * PI / Support)
				/ (x * PI / Support);

	} // end of Lanczos()

	private void CalContrib() {
		nHalfDots = (int) ((double) width * support / (double) scaleWidth);
		nDots = nHalfDots * 2 + 1;
		try {
			contrib = new double[nDots];
			normContrib = new double[nDots];
			tmpContrib = new double[nDots];
		} catch (Exception e) {
			System.out.println("init   contrib,normContrib,tmpContrib" + e);
		}

		int center = nHalfDots;
		contrib[center] = 1.0;

		double weight = 0.0;
		int i = 0;
		for (i = 1; i <= center; i++) {
			contrib[center + i] = Lanczos(i, width, scaleWidth, support);
			weight += contrib[center + i];
		}

		for (i = center - 1; i >= 0; i--) {
			contrib[i] = contrib[center * 2 - i];
		}

		weight = weight * 2 + 1.0;

		for (i = 0; i <= center; i++) {
			normContrib[i] = contrib[i] / weight;
		}

		for (i = center + 1; i < nDots; i++) {
			normContrib[i] = normContrib[center * 2 - i];
		}
	}

	private void CalTempContrib(int start, int stop) {
		double weight = 0;

		int i = 0;
		for (i = start; i <= stop; i++) {
			weight += contrib[i];
		}

		for (i = start; i <= stop; i++) {
			tmpContrib[i] = contrib[i] / weight;
		}

	} // end of CalTempContrib()

	private int GetRedValue(int rgbValue) {
		int temp = rgbValue & 0x00ff0000;
		return temp >> 16;
	}

	private int GetGreenValue(int rgbValue) {
		int temp = rgbValue & 0x0000ff00;
		return temp >> 8;
	}

	private int GetBlueValue(int rgbValue) {
		return rgbValue & 0x000000ff;
	}

	private int ComRGB(int redValue, int greenValue, int blueValue) {

		return (redValue << 16) + (greenValue << 8) + blueValue;
	}

	private int HorizontalFilter(BufferedImage bufImg, int startX, int stopX,
			int start, int stop, int y, double[] pContrib) {
		double valueRed = 0.0;
		double valueGreen = 0.0;
		double valueBlue = 0.0;
		int valueRGB = 0;
		int i, j;

		for (i = startX, j = start; i <= stopX; i++, j++) {
			valueRGB = bufImg.getRGB(i, y);

			valueRed += GetRedValue(valueRGB) * pContrib[j];
			valueGreen += GetGreenValue(valueRGB) * pContrib[j];
			valueBlue += GetBlueValue(valueRGB) * pContrib[j];
		}

		valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
				Clip((int) valueBlue));
		return valueRGB;

	} // end of HorizontalFilter()

	private BufferedImage HorizontalFiltering(BufferedImage bufImage, int iOutW) {
		int dwInW = bufImage.getWidth();
		int dwInH = bufImage.getHeight();
		int value = 0;
		BufferedImage pbOut = new BufferedImage(iOutW, dwInH,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < iOutW; x++) {

			int startX;
			int start;
			int X = (int) (((double) x) * ((double) dwInW) / ((double) iOutW) + 0.5);
			int y = 0;

			startX = X - nHalfDots;
			if (startX < 0) {
				startX = 0;
				start = nHalfDots - X;
			} else {
				start = 0;
			}

			int stop;
			int stopX = X + nHalfDots;
			if (stopX > (dwInW - 1)) {
				stopX = dwInW - 1;
				stop = nHalfDots + (dwInW - 1 - X);
			} else {
				stop = nHalfDots * 2;
			}

			if (start > 0 || stop < nDots - 1) {
				CalTempContrib(start, stop);
				for (y = 0; y < dwInH; y++) {
					value = HorizontalFilter(bufImage, startX, stopX, start,
							stop, y, tmpContrib);
					pbOut.setRGB(x, y, value);
				}
			} else {
				for (y = 0; y < dwInH; y++) {
					value = HorizontalFilter(bufImage, startX, stopX, start,
							stop, y, normContrib);
					pbOut.setRGB(x, y, value);
				}
			}
		}

		return pbOut;

	} // end of HorizontalFiltering()

	private int VerticalFilter(BufferedImage pbInImage, int startY, int stopY,
			int start, int stop, int x, double[] pContrib) {
		double valueRed = 0.0;
		double valueGreen = 0.0;
		double valueBlue = 0.0;
		int valueRGB = 0;
		int i, j;

		for (i = startY, j = start; i <= stopY; i++, j++) {
			valueRGB = pbInImage.getRGB(x, i);

			valueRed += GetRedValue(valueRGB) * pContrib[j];
			valueGreen += GetGreenValue(valueRGB) * pContrib[j];
			valueBlue += GetBlueValue(valueRGB) * pContrib[j];
		}

		valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
				Clip((int) valueBlue));
		// System.out.println(valueRGB);
		return valueRGB;

	} // end of VerticalFilter()

	private BufferedImage VerticalFiltering(BufferedImage pbImage, int iOutH) {
		int iW = pbImage.getWidth();
		int iH = pbImage.getHeight();
		int value = 0;
		BufferedImage pbOut = new BufferedImage(iW, iOutH,
				BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < iOutH; y++) {

			int startY;
			int start;
			int Y = (int) (((double) y) * ((double) iH) / ((double) iOutH) + 0.5);

			startY = Y - nHalfDots;
			if (startY < 0) {
				startY = 0;
				start = nHalfDots - Y;
			} else {
				start = 0;
			}

			int stop;
			int stopY = Y + nHalfDots;
			if (stopY > (int) (iH - 1)) {
				stopY = iH - 1;
				stop = nHalfDots + (iH - 1 - Y);
			} else {
				stop = nHalfDots * 2;
			}

			if (start > 0 || stop < nDots - 1) {
				CalTempContrib(start, stop);
				for (int x = 0; x < iW; x++) {
					value = VerticalFilter(pbImage, startY, stopY, start, stop,
							x, tmpContrib);
					pbOut.setRGB(x, y, value);
				}
			} else {
				for (int x = 0; x < iW; x++) {
					value = VerticalFilter(pbImage, startY, stopY, start, stop,
							x, normContrib);
					pbOut.setRGB(x, y, value);
				}
			}

		}
		return pbOut;

	} // end of VerticalFiltering()

	int Clip(int x) {
		if (x < 0)
			return 0;
		if (x > 255)
			return 255;
		return x;
	}
}

class BufferedImageTranscoder extends ImageTranscoder {
	@Override
	public BufferedImage createImage(int w, int h) {
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		return bi;
	}

	@Override
	public void writeImage(BufferedImage img, TranscoderOutput output) {
		this.img = img;
	}

	public BufferedImage getBufferedImage() {
		return img;
	}

	private BufferedImage img = null;

}



class CombinePic {
	public static void xPic(){//横向处理图片
		try {
			/* 1 读取第一张图片*/ 
			File fileOne = new File("E:\\1.png");
			BufferedImage imageFirst = ImageIO.read(fileOne);
			int width = imageFirst.getWidth();// 图片宽度
			int height = imageFirst.getHeight();// 图片高度
			int[] imageArrayFirst = new int[width * height];// 从图片中读取RGB
			imageArrayFirst = imageFirst.getRGB(0, 0, width, height, imageArrayFirst, 0, width);

			/* 1 对第二张图片做相同的处理 */
			File fileTwo = new File("E:\\2.png");
			BufferedImage imageSecond = ImageIO.read(fileTwo);
			int[] imageArraySecond = new int[width * height];
			imageArraySecond = imageSecond.getRGB(0, 0, width, height, imageArraySecond, 0, width);
			
			// 生成新图片 
			BufferedImage imageResult = new BufferedImage(width * 2 , height,BufferedImage.TYPE_INT_RGB);
			imageResult.setRGB(0, 0, width, height, imageArrayFirst, 0, width);// 设置左半部分的RGB
			imageResult.setRGB(width, 0, width, height, imageArraySecond, 0, width);// 设置右半部分的RGB
			File outFile = new File("D:\\out.jpg");
			ImageIO.write(imageResult, "jpg", outFile);// 写图片
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void yPic(){//纵向处理图片
		try {
			/* 1 读取第一张图片*/ 
			File fileOne = new File("D:\\1.GIF");
			BufferedImage imageFirst = ImageIO.read(fileOne);
			int width = imageFirst.getWidth();// 图片宽度
			int height = imageFirst.getHeight();// 图片高度
			int[] imageArrayFirst = new int[width * height];// 从图片中读取RGB
			imageArrayFirst = imageFirst.getRGB(0, 0, width, height, imageArrayFirst, 0, width);

			/* 1 对第二张图片做相同的处理 */
			File fileTwo = new File("D:\\1.GIF");
			BufferedImage imageSecond = ImageIO.read(fileTwo);
			int[] imageArraySecond = new int[width * height];
			imageArraySecond = imageSecond.getRGB(0, 0, width, height, imageArraySecond, 0, width);
			
			// 生成新图片 
			BufferedImage imageResult = new BufferedImage(width, height * 2,BufferedImage.TYPE_INT_RGB);
			imageResult.setRGB(0, 0, width, height, imageArrayFirst, 0, width);// 设置左半部分的RGB
			imageResult.setRGB(0, height, width, height, imageArraySecond, 0, width);// 设置右半部分的RGB
			File outFile = new File("D:\\out.jpg");
			ImageIO.write(imageResult, "jpg", outFile);// 写图片
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}