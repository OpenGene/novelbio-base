package com.novelbio.base.plot;

import java.io.IOException;
import java.util.Collection;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

public class Rplot {
	/**
	 * 用R画直方图,新图把老图覆盖
	 * @param data
	 * @param min 数据的最小值 如果最小值大于等于最大值，那么就不进行过滤
	 * @param max 数据的最大值  如果最小值大于等于最大值，那么就不进行过滤
	 * @param mainTitle 主标题
	 * @param xTitle x标题
	 * @param yTitle y标题
	 * @param resultPath 结果路径
	 * @param resultPrix 结果文件的前缀，后面带有density
	 * @throws Exception
	 */
	public static void plotHist(Collection<? extends Number> data,double min,double max,String mainTitle,String xTitle,String yTitle,String resultPath,String resultPrix) throws Exception {
		//写入数据
		TxtReadandWrite txtR = new TxtReadandWrite();
		txtR.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
		if (min < max) {
			for (Number d : data) {
				if (d.doubleValue() >=min && d.doubleValue() <=max) {
					txtR.writefile(d+"\n");
				}
			}
		}
		else {
			for (Number d : data) {
				txtR.writefile(d+"\n");
			}
		}
		txtR.close();
		TxtReadandWrite txtTitle = new TxtReadandWrite();
		txtTitle.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
		txtTitle.writefile(mainTitle+"\n");
		txtTitle.writefile(xTitle+"\n");
		txtTitle.writefile(yTitle);
		txtTitle.close();
		rscript(NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT);
		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
	}
	/**
	 * 用R画直方图,新图把老图覆盖
	 * @param data
	 * @param min 数据的最小值 如果最小值大于等于最大值，那么就不进行过滤
	 * @param max 数据的最大值  如果最小值大于等于最大值，那么就不进行过滤
	 * @param mainTitle 主标题
	 * @param xTitle x标题
	 * @param yTitle y标题
	 * @param resultPath 结果路径
	 * @param resultPrix 结果文件的前缀，后面带有density
	 * @throws Exception
	 */
	public static void plotHist(double[] data,double min,double max,String mainTitle,String xTitle,String yTitle,String resultPath,String resultPrix) throws Exception {
		//写入数据
		TxtReadandWrite txtR = new TxtReadandWrite();
		txtR.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
		if (min < max) {
			for (double d : data) {
				if (d>=min && d <=max) {
					txtR.writefile(d+"\n");
				}
			}
		}
		else {
			for (double d : data) {
				txtR.writefile(d+"\n");
			}
		}
		txtR.close();
		TxtReadandWrite txtTitle = new TxtReadandWrite();
		txtTitle.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
		txtTitle.writefile(mainTitle+"\n");
		txtTitle.writefile(xTitle+"\n");
		txtTitle.writefile(yTitle);
		txtTitle.close();
		rscript(NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT);
		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
	}
	
//	/**
//	 * 用R画直方图,新图把老图覆盖
//	 * @param data
//	 * @param min 数据的最小值 如果最小值大于等于最大值，那么就不进行过滤
//	 * @param max 数据的最大值  如果最小值大于等于最大值，那么就不进行过滤
//	 * @param mainTitle 主标题
//	 * @param xTitle x标题
//	 * @param yTitle y标题
//	 * @param resultPath 结果路径
//	 * @param resultPrix 结果文件的前缀，后面带有density
//	 * @throws Exception
//	 */
//	public static void plotHist(Collection<? extends Number> data,double min,double max,String mainTitle,String xTitle,String yTitle,String resultPath,String resultPrix) throws Exception {
//		//写入数据
//		TxtReadandWrite txtR = new TxtReadandWrite();
//		txtR.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
//		if (min < max) {
//			for (Number d : data) {
//				if (d.doubleValue() >=min && d.doubleValue() <=max) {
//					txtR.writefile(d+"\n");
//				}
//			}
//		}
//		else {
//			for (Number d : data) {
//				txtR.writefile(d+"\n");
//			}
//		}
//		txtR.close();
//		TxtReadandWrite txtTitle = new TxtReadandWrite();
//		txtTitle.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
//		txtTitle.writefile(mainTitle+"\n");
//		txtTitle.writefile(xTitle+"\n");
//		txtTitle.writefile(yTitle);
//		txtTitle.close();
//		rscript(NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT);
//		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
//	}
	
	
	/**
	 * 执行R程序，直到R程序结束再返回
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private static int rscript(String scriptPath) throws IOException, InterruptedException  
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command=NovelBioConst.R_SCRIPT + scriptPath;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}
}
