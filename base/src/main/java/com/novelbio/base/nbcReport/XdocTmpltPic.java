package com.novelbio.base.nbcReport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


/**
 *  图片对应的实体类
 * @author gaozhu
 *
 */
public class XdocTmpltPic extends XdocTemplate{
	
	/** 图片对应的说明文件名，全名 */
	private String descFile = "";
	/** 图片的标题 */
	private String title = "";
	/** 图片的注： */
	private String note = ""; 
	/** 同类图片的对比说明在这类图片的上方 */
	private String upCompare = "";
	/** 同类图片的对比说明在这类图片的下方 */
	private String downCompare = "";
	/** 实验组名 */
	private String expTeamName = "";
	
	List<String> lsPictureNames;
	
	/** 根据picture路径完成本类的构造 */
	public XdocTmpltPic(String fileNames) {
		lsPictureNames = new ArrayList<String>();
		String[] names = fileNames.split(SepSign.SEP_ID);
		String path = FileOperate.getParentPathName(names[0]);
		for (int i = 0; i < names.length; i++) {
			if (i == 0) {
				lsPictureNames.add(names[i]);
				continue;
			}
			lsPictureNames.add(FileOperate.addSep(path)+names[i]);
		}
		resolvePictureName(names[0]);
	}
	
	/**
	 * 解析文件名
	 */
	private void resolvePictureName(String pictureName) {
		//去掉后缀名
		String pictureNameNoSuffix = FileOperate.getFileNameSep(pictureName)[0];
		this.descFile = FileOperate.changeFileSuffix(pictureName, "_pic", "txt");
		super.tempName = "Picture.xdoc";
		String[] names = FileOperate.getFileNameSep(pictureNameNoSuffix)[0].split("_");
		this.expTeamName = names[0];
		
		this.title = "上图为"+FileOperate.getFileNameSep(pictureName)[0]+"所展示的图片";
	}
	
	/**
	 * 读取excel的说明文件中的参数（允许不存在）
	 */
	@Override
	public void readParamAndGenerateXdoc(){
		if (FileOperate.isFileExist(descFile)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(descFile, false);
			for (String content : txtRead.readlines()) {
				if (content.trim().equals("")) {
					continue;
				}
				String[] params = content.split("@@");
				if (params.length == 1) {
					logger.error(descFile+".txt文件书写不规范");
				}
			
				if(params[0].equals("title")){
					this.title = params[1];
				}else if(params[0].equals("note")){
					this.note = params[1];
				}else if(params[0].equals("upCompare")){
					this.upCompare = params[1];
				}else if(params[0].equals("downCompare")){
					this.downCompare = params[1];
				}
			}
			txtRead.close();
		}
		
		mapParams.put("lsSrcs", lsPictureNames);
		mapParams.put("title",title);
		mapParams.put("note",note);
		mapParams.put("upCompare",upCompare);
		mapParams.put("downCompare",downCompare);
	}
	
	/** 输出渲染好的xdoc的toString结果 */
	@Override
	public String toString(){
		/** 把子xdoc的toString方法封装成集合传递给本xdoc */
		try {
			if (!FileOperate.isFileExist(xdocPath+"/"+tempName)) {
				return "";
			}
			return renderXdoc(xdocPath,tempName,mapParams);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("渲染模板"+tempName+"出错");
			return "";
		}
	}

	/**
	 * 这种类型 PICTURE::value1#/#1;value2#/#1;value3#/#1;value4#/#1....读取为picture集合 #/#后面代表是一组的<br>
	 * @param path 文件所在父级路径
	 * @param param PICTURE::value1#/#1;value2#/#1;value3#/#1;value4#/#1....读取为picture集合<br>
	 * @param num 读取前几个文件 
	 * @return
	 */
	public static Set<String> getLsFile(String path, String param, int num) {
		Set<String> setResultFileName = new LinkedHashSet<String>();
		path = FileOperate.addSep(path);
		String[] ss = param.split(";");
		int numThis = 0;
		for (String string : ss) {
			numThis++;
			setResultFileName.add(path + string);
			if (numThis > num) {
				break;
			}
		}
		return setResultFileName;
	}
}
