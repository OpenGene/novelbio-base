package com.novelbio.base.nbcReport;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.hg.xdoc.XDoc;
import com.hg.xdoc.XDocIO;
import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.ZipOperate;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 模板类
 * @author gaozhu
 */
public class XdocTemplate {
	public static void main(String[] args) {
		XdocTemplate.setRootXdocTemp("/home/zong0jie/software/git/NBCWebApp1.0/NBCWebApp1.0/target/classes/");
		XdocTemplate xdocTemplate = new XdocTemplate("","/home/zong0jie/desktop/Novelbio Result");
//		XdocTemplate xdocTemplate = new XdocTemplate("","/home/zong0jie/Atmp/Test/Novelbio Result");
		xdocTemplate.readParamAndGenerateXdoc();
		xdocTemplate.outputReport("/home/zong0jie/desktop/result3.docx");
//		xdocTemplate.outputReport("/home/zong0jie/Atmp/Test/result.docx");
	}
	
	public static final Logger logger = Logger.getLogger(XdocTemplate.class);
	/** 模板所在根路径 */
	public static String rootXdocTemp = "/home/zong0jie/git/NBCWebApp1.0/NBCWebApp1.0/target/classes/";
	public static final String XdocRootTmplt = "Novelbio Result.xdoc";
	/** 
	 * 设置模板根路径，这个方法会在第一次生成模板的时候调用
	 * 为了方便，决定在客户端请求到来的时候调用
	 */
	public static void setRootXdocTemp(String rootXdocTemp) {
		XdocTemplate.rootXdocTemp = rootXdocTemp;
	}

	/** 对应的模板位置， */
	public String xdocPath = FileOperate.addSep(rootXdocTemp) + "xdocTemplate";
	
	/** 如果某类结果的数量太多，那么就只取前面5个的结果在pdf中做展示 */
	int numSample = 5;
	
	/** 待渲染的当前文件夹，如果当前文件夹和根文件夹是同一个，则表示在渲染第一层文件夹 */
	String documtPath;
	/** 本次自动化报告的根文件目录 */
	String reportPath;
	
	/** 对应的模板文件名 */
	public String tempName = "";
	/**  结果获取路径  */
	public Set<String> setResultFiles = new LinkedHashSet<String>();
	/** 从每个文件夹读取的参数集合
	 * key: 参数
	 * valuue: 可以是该参数的具体值，也可以是一个子模板
	 *  */
	public Map<String, Object> mapParams = new LinkedHashMap<String, Object>();
	/** 子级模板，嵌套在该父模板中 */
	public List<XdocTemplate> lsXdocChildren = new ArrayList<XdocTemplate>();
	
	/** 设定该模块前面显示的number，如1.1   1.2 */
	public String numInTitle = "";
	/**
	 * 标题名，用于做大类的标题，根据项目实际文件夹名获取，必须<b>数字开头，顿号结尾</b>
	 * 用来是否添加判断对应的计算方法
	 */
	public String titleFold = "";
	



	public XdocTemplate() { }
	
	/** 根据结果报告文件夹路径初始化对象
	 * @param numberInTitle 标题的序号，如 1.1.1. 注意一般情况下最后有个. 因为模板中是"3.2.${no}.1" 方法 这种格式
	 * @param documtPath 待读取的文件夹路径
	 */
	public XdocTemplate(String numberInTitle, String documtPath) {
		this(numberInTitle, documtPath, documtPath);
	}
	
	/**
	 * @param numberInTitle
	 * @param documtPath 当前文件夹
	 * @param reportPath 报告根文件夹的路径
	 */
	private XdocTemplate(String numberInTitle, String documtPath, String reportPath) {
		if (numberInTitle != null) {
			this.numInTitle = numberInTitle;
		}
		this.documtPath = FileOperate.removeSep(documtPath);
		this.reportPath = reportPath;
		String fileName = FileOperate.getFileName(documtPath);
		if (fileName.contains("、") || fileName.contains(",") || fileName.contains("，")) {
			//获得文件夹的title，文件夹头上必须有编号
			String[] names = fileName.split("、|,|，", 0);
			this.numInTitle = names[0];
			this.titleFold = names[names.length-1];
		} else {
			this.titleFold = FileOperate.getFileName(documtPath);
		}
		if (documtPath.equals(FileOperate.removeSep(reportPath))) {
			this.tempName = XdocRootTmplt;//用该名称去找report的根模板
		} else {
			this.tempName = titleFold + ".xdoc";
		}
	}
	
	public String getTitle() {
		return titleFold;
	}
	
	/**
	 * 如果某类结果的数量太多，那么就只取前面 numSample 个的结果在pdf中做展示
	 * 默认为5
	 */
	public void setNumSample(int numSample) {
		this.numSample = numSample;
	}
	
	/**
	 * 递归读取所有文件夹下的参数和文件，并产生xdoc
	 * 文件夹名字类似 "1,Analysis"
	 * 这时候就会将1这个字段截取下来放在最后的reoprt中做为title
	 */
	public void readParamAndGenerateXdoc() {
		mapParams.put("no", numInTitle);
		readFoldParams2mapParams(documtPath);
		List<String> lsFileNames = FileOperate.getFoldFileNameLs(documtPath, "*", "*");
		Collections.sort(lsFileNames, new XdocComparator());
		/** 判断是否是文件夹,把文件夹再当成一个xdocTemplate来处理，并加到本类的子模板里 */
		for (int i = 0; i < lsFileNames.size(); i++) {
			String filename = lsFileNames.get(i);
			if (FileOperate.isFileDirectory(filename)) {
				//TODO 这里就是简单的把阿拉伯数字放进去作为title，也可以用一个变量来记录是第几层，每一层用特定的字符来展示
				XdocTemplate xdocTemplate = new XdocTemplate(i + "", filename, reportPath);
				xdocTemplate.readParamAndGenerateXdoc();
				lsXdocChildren.add(xdocTemplate);
			}
		}
	}

	/**
	 * 读取params.txt文件中的所有参数（允许不存在）并<br>
	 * 放到mapParams中<br><br>
	 * 
	 *  params.txt里的数据格式如下：<br>
	 *  Key@@Value读取为正常参数 <br>
	 *  Key@@EXCEL::value1#/#1;value1#/#2;value2#/#1....读取为excel集合<br>
	 *  Key@@PICTURE::value1;value2;value3....读取为picture集合<br><br>
	 *  
	 *  其中key为传入xdoc的模板里面的参数
	 * @param fileName
	 */
	protected void readFoldParams2mapParams(String documtPath) {
		documtPath = FileOperate.addSep(documtPath);
		String paramFile = documtPath+"params.txt";
		if (!FileOperate.isFileExist(paramFile)) {
			return;
		}
		TxtReadandWrite txtRead = new TxtReadandWrite(paramFile);
		Set<String> setContent = new HashSet<String>();//去重复使用，如果出现相同的两行只取一行
		for (String content : txtRead.readlines()) {
			content = content.trim();
			if (content.equals("") || content.startsWith("#")) continue;
			if (setContent.contains(content)) continue;
			setContent.add(content);			
			
			String[] params = content.split(SepSign.SEP_INFO);
			if (params.length == 1) {
				logger.error("params.txt文件书写不规范");
			}
			String type = params[1].split(":")[0];
			if (EnumXdocType.get(type) == null) {
				mapParams.put(params[0], params[1]);
			} else {
				EnumXdocType xdocType = EnumXdocType.get(type);
				Set<String> setFileName2Param = getLsFileNameAndParam(xdocType,documtPath, params[1]);
				if (setFileName2Param.size() == 0) continue;
				
				setResultFiles.addAll(getLsFileNameInDoc(setFileName2Param));
				if (mapParams.containsKey(params[0])) {
					((List<String>)mapParams.get(params[0])).addAll(getLsFileTmplt(xdocType, setFileName2Param));
				} else {
					mapParams.put(params[0], getLsFileTmplt(xdocType, setFileName2Param));
				}
			}
		}
		mapParams.put("lsResultFiles", new ArrayList<String>(setResultFiles));
		txtRead.close();
		return;
	}
	
	/**
	 * 返回前几个含有param的文件全名<br>
	 * param是指value1#/#1中，#/#之后的参数，一般在excel中用于指示读取第几个Sheet<br>
	 * @param documtPath
	 * @param param1
	 * @return
	 */
	private Set<String> getLsFileNameAndParam(EnumXdocType xdocType,String documtPath, String param1) {
		Set<String> setFileNameAndParamResult = new LinkedHashSet<String>();
		if(param1.split("::").length != 2){
			return setFileNameAndParamResult;
		}
		String allFileName = param1.split("::")[1];
		
		Set<String> setFileName = new HashSet<String>();
		
		for (String fileNameDetail : allFileName.split(";")) {
			if (xdocType == EnumXdocType.Picture) {
				String[] fileNames = fileNameDetail.split(SepSign.SEP_ID);
				for (int i = 0; i < fileNames.length; i++) {
					if (FileOperate.isFileExistAndBigThanSize(FileOperate.addSep(documtPath)+fileNames[i], 0)) {
						setFileNameAndParamResult.add(FileOperate.addSep(documtPath)+fileNameDetail);
					}
				}
			}
			if (xdocType == EnumXdocType.Excel) {
				if (setFileName.size() > numSample || fileNameDetail.equals("")) {
					break;
				}
				String fileName = FileOperate.addSep(documtPath) + fileNameDetail.split(SepSign.SEP_INFO_SAMEDB)[0];
				if (FileOperate.isFileExistAndBigThanSize(fileName, 0)) {
					setFileName.add(fileName);
					setFileNameAndParamResult.add(FileOperate.addSep(documtPath) + fileNameDetail);
				}
			}
		}
		return setFileNameAndParamResult;
	}
	
	/** 获取填写入模板的文件名，是绝对路径 */
	private Set<String> getLsFileNameInDoc(Set<String> setFileName2Param) {
		Set<String> setFileName = new LinkedHashSet<String>();
		for (String string : setFileName2Param) {
			if (string.contains(SepSign.SEP_ID)) {
				continue;
			}
			String fileName = string.split(SepSign.SEP_INFO_SAMEDB)[0];
			String relativeFileName = fileName.replaceFirst(FileOperate.addSep(reportPath), "");
			relativeFileName = FileOperate.addSep(FileOperate.getFileName(reportPath)) + relativeFileName;
			
			//把这个文件的相对路径添加到结果路径里，这个结果路径将来是在报告中展示的
			setFileName.add(relativeFileName);
		}
		return setFileName;
	}
	
	/**
	 * 根据结果路径中的文件，获得对应的子模版 渲染好的 xdoc字符串<br>
	 * @param xdocType
	 * @param setFileName2Param  set中是这种类型 value1#/#1;value1#/#2;value2#/#1....，其中value1等为全文件名<br>
	 * @return
	 */
	private List<String> getLsFileTmplt(EnumXdocType xdocType, Set<String> setFileName2Param) {
		List<String> lsResult = new ArrayList<String>();
		for (String filename2Param : setFileName2Param) {
			XdocTemplate xdocTemplate = createTemplate(xdocType, filename2Param);
			xdocTemplate.readParamAndGenerateXdoc();
			lsResult.add(xdocTemplate.toString());
		}
		return lsResult;
	}

	/** 输出渲染好的xdoc包括所有子集的toString结果
	 * @param xdocMethods  方法学说明  
	 */
	public String toString(XdocMethods xdocMethods) {
		List<String> lsRenderedXdocs = new ArrayList<String>();
		/** 把子xdoc的toString方法封装成集合传递给本xdoc */
		for ( XdocTemplate xdoc : lsXdocChildren) {
			lsRenderedXdocs.add(xdoc.toString(xdocMethods));
			xdocMethods.addMethod(xdoc.getTitle());
		}
		try{
			mapParams.put("lsMethod", xdocMethods.getSetMethod());
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		
		mapParams.put("lsXdocChildren", lsRenderedXdocs);
		try {
			if (!FileOperate.isFileExist(xdocPath+"/"+tempName)) {
				String xdocStrings = "";
				for(String xdocString : lsRenderedXdocs){
					xdocStrings += xdocString;
				}
				return xdocStrings;
			}
			return renderXdoc(xdocPath,tempName,mapParams);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("渲染模板"+tempName+"出错");
			return "";
		}
	}
	
	/**
	 * 给定保存路径outPath、文件名fileName后缀名suffix，输出带有页面页脚的最终结果报告
	 */
	public boolean outputReport(String outPathFile){
	
		mapParams.put("lsXdocChildren", toString(new XdocMethods()));
		try {
			//设置目录
			String tmpResult = renderXdoc(xdocPath,"background_temp.xdoc",mapParams);
			mapParams.put("lsCatalogs", createCatalog(tmpResult));
//			TxtReadandWrite txtWrite = new TxtReadandWrite(outPathFile + ".xdoc", true);
//			txtWrite.writefileln(result);
//			txtWrite.close();
			List<String> lsImageSrcs = findAllImageSrc(tmpResult);
			// 用字符串构建XDoc
			XDoc xdoc = new XDoc(tmpResult);
			//加上一个封面模板，因为封面模板是没有页眉页脚的
			// 生成的文件保存目录
			XDocIO.write(xdoc, new File(outPathFile));
			motifyReport(lsImageSrcs, outPathFile);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存目录无效或不能转换成此类文件！");
			return false;
		}
		return true;
	}
	
	/**
	 * 根据渲染的String结果查找标题来创建目录 
	 */
	private List<String> createCatalog(String result){
		List<String> lsCatalogs = new ArrayList<String>();
		Document doc = null;
	    try {
            // 读取并解析XML文档
           // 下面的是通过解析xml字符串的
	    	doc = DocumentHelper.parseText(result); // 将字符串转为XML
	    	Element rootElt = doc.getRootElement(); // 获取根节点
	    	@SuppressWarnings("unchecked")
	    	//找出name属性为title的所有para标签下的text标签，在设计模板的时候这类para标签下只允许设置一个text标签
			List<Element> elements = (List<Element>)rootElt.selectNodes("//para[@name='title']/text");
	    	for(Element element : elements){
	    		//根据<text> 标签中的name属性的值来确定标题级别，这个name的属性值必须为数字
	    		int titleLv = Integer.parseInt(element.attributeValue("name"));
	    		String title = element.getTextTrim();
	    		//在不同级别标签前加不同数量的缩进符
	    		for (int i = 0; i < titleLv ; i++) {
					title = "\t" + title;
				}
	    		//最后放到目录集合里
	    		lsCatalogs.add(title);
	    	}
	    } catch (DocumentException e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	logger.error("模板书写不规范");
            e.printStackTrace();

        }
		return lsCatalogs;
	}

	/**
	 * 根据一个xml字符串读取里面的<img src="aaa">标签的src属性
	 * 返回一个图片全路径序列集合
	 */
	public List<String> findAllImageSrc(String result) {
		List<String> lsImageSrcs = new ArrayList<String>(); 
		Document doc = null;
	    try {
		 
            // 读取并解析XML文档
           // 下面的是通过解析xml字符串的
	    	doc = DocumentHelper.parseText(result); // 将字符串转为XML
	    	Element rootElt = doc.getRootElement(); // 获取根节点
	    	@SuppressWarnings("unchecked")
	    	//找出所有img标签
			List<Element> elements = (ArrayList<Element>)rootElt.selectNodes("//img");
	    	//循环标签，取得它的src属性值放到集合中
            for (Element element : elements ){
            	lsImageSrcs.add(element.attribute("src").getValue());
            	if(!element.attribute("src").getValue().startsWith("data:im")){
            		element.attribute("src").setValue( FileOperate.addSep(rootXdocTemp) + "pictureTemplate" + FileOperate.getSepPath() + "picTemp.PNG");
            		System.out.println("图片" + element.attribute("src").getValue());
            	}
            }
            //背景层里的一条线，虽然不是img标签，但是最终会保存为一张图片，所以要把它算到集合里
            lsImageSrcs.add(1, "data:im");
        } catch (DocumentException e) {
        	e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
			return lsImageSrcs;
     }
	
	/**
	 *  将最后生成的word中的小图换成原始大图
	 *  同时将封面的页眉页脚去掉
	 *  同时还要把所有的表格边框线改成白色
	 * @param lsImageSrcs 图片位置序列
	 * @param reportPath 生成的word报告文件，包含全路径
	 */
	public void motifyReport(List<String> lsImageSrcs , String reportPath ) {
		//word转成的压缩包名
		String zipFileName = FileOperate.changeFileSuffixReal(reportPath, null, "zip");
		//解压后的文件夹全名
		String folderName = reportPath.substring(0, reportPath.lastIndexOf(FileOperate.getSepPath())+1)+ FileOperate.getFileNameSep(reportPath)[0];
		//图片所在路径
		String imagePath = folderName+FileOperate.getSepPath()+"word"+FileOperate.getSepPath()+"media"+FileOperate.getSepPath();
		//页眉页脚需要修改的document.xml文件全路径
		String documtXMLPath = folderName+FileOperate.getSepPath()+"word" + FileOperate.getSepPath() + "document.xml";
//		final PatternOperate pat = new PatternOperate("rId(\\d+).png", false);
		try {
			ZipOperate.unZipFiles(zipFileName, folderName);
			String result = null;
			for(int i = 0; i<lsImageSrcs.size(); i++){
				result = lsImageSrcs.get(i);
		  	if(result.startsWith("data:im")){
					continue;
				}
//		  		List<String> lsImageFiles = FileOperate.getFoldFileNameLs(imagePath, "rId*", "png");
//		  		Collections.sort(lsImageFiles, new Comparator<String>() {
//					@Override
//					public int compare(String o1, String o2) {
//						Integer id1 = Integer.parseInt(pat.getPatFirst(o1, 1));
//						Integer id2 = Integer.parseInt(pat.getPatFirst(o2, 1));
//						return id1.compareTo(id2);
//					}
//				});
				FileOperate.delFile(imagePath+"rId"+(i+11)+".png");
				FileOperate.copyFile(result, imagePath + FileOperate.getFileName(result),true);
				FileOperate.changeFileName(imagePath + FileOperate.getFileName(result), "rId"+(i+11)+".png", true);
			}
			FileOperate.delFile(zipFileName);
			//去掉首页的页眉页脚,和修改表格边框
			modifyWord(documtXMLPath);
			ZipOperate.zip(folderName, zipFileName);
			FileOperate.changeFileName(zipFileName,FileOperate.getFileName(reportPath),true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileOperate.delFolder( folderName);
}
	
	/** 去页眉页脚改表格边框线，同时设置行间距1.5倍 */
	@SuppressWarnings("unchecked")
	private void modifyWord(String documtXMLPath) {
		//去掉首页的页眉页脚
		SAXReader saxReader = new SAXReader();  
        Document document = null;
		try {
			document = saxReader.read(new File(documtXMLPath));
		} catch (DocumentException e) {
			logger.error("修改报告失败");
			e.printStackTrace();
		}
        Element rootElement = document.getRootElement();
        
        //在<w:sectPr>标签中加入<w:titlePg />这样一个标签，在word中意思为首页页眉页脚不同，要另外设定
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:sectPr")){
        	element.addElement("w:titlePg");
        }
        //以下的两个循环用来移除<w:headerReference w:type="first"  /> 首页 页眉内容标签 首页 页脚标签同理
        //可用可不用，用了防止有意外，我们生成的没有以下这两种标签
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:headerReference")){
       		if(element.attributeValue("type").equals("first")){
       			element.getParent().remove(element);
       		}
        }
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:footerReference")){
        	if(element.attributeValue("type").equals("first")){
        		element.getParent().remove(element);
    		}
        }
        
        //在<w:tblPr >标签下添加<w:tblBorders >标签来设置table的边框属性
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:tblPr")){
        	Element tblBorders = element.addElement("w:tblBorders");
            //创建一个需要插入的xml标签w:tblBorders
            Element top = tblBorders.addElement("w:top"); 
            top.addAttribute("w:val", "single"); top.addAttribute("w:sz", "4"); top.addAttribute("w:space", "0"); top.addAttribute("w:color", "FFFFFF"); top.addAttribute("w:themeColor", "background1");
            Element left = tblBorders.addElement("w:left"); 
            left.addAttribute("w:val", "single"); left.addAttribute("w:sz", "4"); left.addAttribute("w:space", "0"); left.addAttribute("w:color", "FFFFFF"); left.addAttribute("w:themeColor", "background1");
            Element bottom = tblBorders.addElement("w:bottom"); 
            bottom.addAttribute("w:val", "single"); bottom.addAttribute("w:sz", "4"); bottom.addAttribute("w:space", "0"); bottom.addAttribute("w:color", "FFFFFF"); bottom.addAttribute("w:themeColor", "background1");
            Element right = tblBorders.addElement("w:right"); 
            right.addAttribute("w:val", "single"); right.addAttribute("w:sz", "4"); right.addAttribute("w:space", "0"); right.addAttribute("w:color", "FFFFFF"); right.addAttribute("w:themeColor", "background1");
            Element insideH = tblBorders.addElement("w:insideH"); 
            insideH.addAttribute("w:val", "single"); insideH.addAttribute("w:sz", "4"); insideH.addAttribute("w:space", "0"); insideH.addAttribute("w:color", "FFFFFF"); insideH.addAttribute("w:themeColor", "background1");
            Element insideV = tblBorders.addElement("w:insideV"); 
            insideV.addAttribute("w:val", "single"); insideV.addAttribute("w:sz", "4"); insideV.addAttribute("w:space", "0"); insideV.addAttribute("w:color", "FFFFFF"); insideV.addAttribute("w:themeColor", "background1");
        }
        //移除所有表格的每个cell的边框属性，让表格继承table的边框属性，上一个循环就是设置table的边框属性
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:tcBorders")){
        	element.getParent().remove(element);
        }
        
        //设值1.5倍行间距<w:pPr><w:spacing w:line="360" w:lineRule="auto"/></w:pPr> 不要添加在表格和目录上
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:pPr")){
        	if(!(element.getParent().getParent().getName().equals("tc"))){
        		Element spacing = element.addElement("w:spacing");
            	spacing.addAttribute("w:line", "360");
            	spacing.addAttribute("w:lineRule", "auto");
        	}
        }
        //移除目录的行间距
        for(Element element : (ArrayList<Element>)rootElement.selectNodes("//w:rPr/w:rFonts[@w:ascii='黑体']")){
        	if(element.getParent().element("sz").attributeValue("val").equals("22.5")){
        		Element p = element.getParent().getParent().getParent();
        		p.element("pPr").clearContent();
        	}
        }
        
       FileOperate.delFile(documtXMLPath);
        try {  
            /** 将document中的内容写入文件中 */  
            XMLWriter writer = new XMLWriter(new FileWriter(new File( documtXMLPath)));  
            writer.write(document);  
            writer.close();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
	}
	
	/**
	 *  根据读取到的参数集合用freemarker渲染xdoc模板 
	 * @throws Exception 
	 */
	protected String renderXdoc(String path,String name,Map<String, Object> map) throws Exception {
		// TODO 异常待处理
		// 加载模板
		Configuration cf = new Configuration();
		cf.setClassicCompatible(true);
		// 模板存放路径
		cf.setDirectoryForTemplateLoading(new File(path));
		cf.setEncoding(Locale.getDefault(), "UTF-8");
		// 模板名称
		Template template = cf.getTemplate(name);
		StringWriter sw = new StringWriter();
		// 处理并把结果输出到字符串中
		template.process(map, sw);
		// 返回渲染好的xdoc字符串
		return sw.toString();
	}
	
	/**
	 * 目前只支持excel和picutre
	 * @param strXdocType XdocType里面的tostring
	 * @param fileName
	 * @return
	 */
	protected static XdocTemplate createTemplate(EnumXdocType xdocType, String pathFileName) {
		if (xdocType == EnumXdocType.Excel) {
			return new XdocTmpltExcel(pathFileName);
		} else if (xdocType == EnumXdocType.Picture) {
			return new XdocTmpltPic(pathFileName);
		}
		logger.debug("未知类型");
		return null;
	}
	
}
