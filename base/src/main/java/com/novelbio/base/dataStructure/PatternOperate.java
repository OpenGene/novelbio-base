package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输入正则表达式和所查找的文本，找到该正则表达式出现的位置
 */
public class PatternOperate {
	String regex = "";
	boolean CASE_SENSITIVE = false;
	Pattern patInput;
	Matcher matInput;
	String inputStr;
	List<PatternUnit> lsPatternUnits;
	 /**
	  * 设定正则表达式
	  * @param regex
	  * @param CASE_SENSITIVE 大小写是否敏感
	  */
   public PatternOperate(String regex,boolean CASE_SENSITIVE) {
		this.regex = regex;
		this.CASE_SENSITIVE = CASE_SENSITIVE;

   	 if(!CASE_SENSITIVE) {
   	     patInput=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
   	 } else {
   		 patInput=Pattern.compile(regex);
		}
	}
    
    /** 设定需要查找的序列，设定后可以用{@link #getLsPatternUnits()} 来获得抓取得到的信息 */
    public void setInputStr(String inputStr) {
		this.inputStr = inputStr;
		getPatternInfo();
	}
    /** 获得{@link #setInputStr(String)} 输入的list的抓取信息 */
    public List<PatternUnit> getLsPatternUnits() {
		return lsPatternUnits;
	}
    
    private void getPatternInfo() {
    	//hashtable用来装载正则表达式的不同具体字符串，用以判断某个特定字符串出现的次数
    	Map<String, Integer> mapPat2Num=new HashMap<>();
    	lsPatternUnits = new ArrayList<>();
    	matInput=patInput.matcher(inputStr);
    	Integer index;//某个字符的出现次数
    	while(matInput.find()) {
    		PatternUnit patternUnit = new PatternUnit();
    		patternUnit.setPatternStr(matInput.group());
    		if((index = mapPat2Num.get(patternUnit.getPatternStr())) ==null) {
    			mapPat2Num.put(patternUnit.getPatternStr(), 1);//第一次发现该字符串，则设定为1
    		}
    		else {
    			mapPat2Num.put(patternUnit.getPatternStr(), index+1);//以前发现过，则+1
    		}
    		int locationstart = 0;//设置该表达式到起点距离为0
    		int locationend = 0;//该表达式到终点距离为0
    		int num = mapPat2Num.get(patternUnit.getPatternStr());//总共发现了num次
    		for(int i=0; i<num;i++) {
    			locationstart=inputStr.indexOf(patternUnit.getPatternStr(), locationstart)+1;
    		}
    		locationend=inputStr.length() - locationstart - patternUnit.getPatternStr().length() + 2;
    		patternUnit.setStartLoc(locationstart);
    		patternUnit.setEndLoc(locationend);
    		lsPatternUnits.add(patternUnit);
    	}
    }

    
    public static class PatternUnit {
    	/** 匹配到的某个特定的字符串 */
    	String patternStr;
    	/** 该字符串的位置，为该字符串第一个字符到这个字符串起点的位置：acksd中a为1,k为3 */
    	int startLoc;
    	/** 该字符串的位置，为该字符串最后一个字符到这个字符串终点的位置：acksd中a为5,k为3 */
    	int endLoc;
    	
    	private void setPatternStr(String patternStr) {
			this.patternStr = patternStr;
		}
    	/** 该字符串的位置，为该字符串最后一个字符到这个字符串终点的位置：acksd中a为5,k为3 */
    	private void setEndLoc(int endLoc) {
			this.endLoc = endLoc;
		}
    	/** 该字符串的位置，为该字符串第一个字符到这个字符串起点的位置：acksd中a为1,k为3 */
    	private void setStartLoc(int startLoc) {
			this.startLoc = startLoc;
		}

    	public String getPatternStr() {
			return patternStr;
		}
    	/** 该字符串的位置，为该字符串最后一个字符到这个字符串终点的位置：acksd中a为5,k为3 */
    	public int getEndLoc() {
			return endLoc;
		}
    	/** 该字符串的位置，为该字符串第一个字符到这个字符串起点的位置：acksd中a为1,k为3 */
    	public int getStartLoc() {
			return startLoc;
		}
    }
    
    //=============== 简单处理 ============================//
    /**
     * <b>简单处理，相对快速</b>
     * 获得序列中指定的所有正则表达式的值
     * @param inputstr
     * @param regex
     * @param CASE
     * @return 没有抓到的话，返回size=0的list
     */
    public ArrayList<String> getPat(String inputstr) {
    	return getPat(inputstr, 0);
    }
    /**
     * <b>简单处理，相对快速</b>
     * 获得序列中指定的所有正则表达式的值
     * @param inputstr
     * @param regex
     * @param CASE
     * @return 没有抓到的话，返回size=0的list
     */
    public ArrayList<String> getPat(String inputstr, int groupID) {
    	ArrayList<String> lsresult = new ArrayList<String>();
    	 matInput=patInput.matcher(inputstr);
    	 while (matInput.find()) {
    		 String info = matInput.group(groupID);
    		 if (info != null) {
    			 lsresult.add(info);
			}
		}
    	return lsresult;
    }
    /**
     * <b>简单处理，相对快速</b>
     * 获得序列中指定的所有正则表达式的值
     * @param inputstr
     * @param regex
     * @param CASE
     * @return 没有抓到的话，返回size=0的list
     */
    public ArrayList<String> getPat(String inputstr, int... groupID) {
    	ArrayList<String> lsresult = new ArrayList<String>();
    	 matInput=patInput.matcher(inputstr);
    	 while (matInput.find()) {
    		 for (int i : groupID) {
    			 String info = matInput.group(i);
        		 if (info != null) {
        			 lsresult.add(info);
    			}
			}
		}
    	return lsresult;
    }
    /**
     * <b>简单处理，相对快速</b>
     * 获得序列中指定的第一个正则表达式的值
     * @param inputstr
     * @param regex
     * @param CASE
     * @return 没有抓到的话，返回null
     */
    public String getPatFirst(String inputstr) {
    	return getPatFirst(inputstr, 0);
    }
    /**
     * <b>简单处理，相对快速</b>
     * 获得序列中指定的第一个正则表达式的值
     * @param inputstr
     * @param regex
     * @param CASE
     * @return 没有抓到的话，返回null
     */
    public String getPatFirst(String inputstr, int groupID)  {
    	 matInput=patInput.matcher(inputstr);
    	 while (matInput.find()) {
    		 return matInput.group(groupID);
		}
    	return null;
    }
    
    /**
	 * <b>高级处理，耗时长</b>
	 * 输入stringinput，正则表达式,以及是否无视大小写（True），
	 * @param inputstr 输入所要查找的string
	 * @param regex 输入要匹配的正则表达式
	 * @param CASE 是否无视大小写。False:无视大小写。True:检查大小写
	 * @return 返回List<String[3]>
	 * list(i):input中找到的第i个匹配字符--具体内容为里面装的string[2]数组。<br/>
	 * String[0]:正则表达式的某个特定的字符串<br/>
	 * String[1]:该字符串的位置，为该字符串第一个字符到这个字符串起点的位置：acksd中a为1,k为3<br/>
	 * String[2]:该字符串的位置，为该字符串最后一个字符到这个字符串终点的位置：acksd中a为5,k为3
	 * 如果没找到，不返回null，而是返回一个size为0的list
	 */
	public static ArrayList<String[]> getPatLoc(String inputstr, String regex, boolean CASE) {
    	//hashtable用来装载正则表达式的不同具体字符串，用以判断某个特定字符串出现的次数
    	 Hashtable<String, Integer> pathash=new Hashtable<String, Integer>();
    	 ArrayList<String[]> listResult=new ArrayList<String[]>();
    
    	 Pattern patInput; Matcher matInput;
    	 if(!CASE) {
    	     patInput=Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
    	 }
    	 else {
    		 patInput=Pattern.compile(regex);
    	 }
    	 matInput=patInput.matcher(inputstr);
    	 Integer index;//某个字符的出现次数
    	 while(matInput.find()) {   
    		 String[] patinfo=new String[3];//装载这次找到的字符串的具体信息
    		 patinfo[0]=matInput.group();//这次找到的字符串
    		 if((index=pathash.get(patinfo[0]))==null) {
    			 pathash.put(patinfo[0], 1);//第一次发现该字符串，则设定为1
    		 }
    		 else {
    			 pathash.put(patinfo[0], index+1);//以前发现过，则+1
    		 }
    		 int locationstart=0;//设置该表达式到起点距离为0
    		 int locationend=0;//该表达式到终点距离为0
    		 int num=pathash.get(patinfo[0]);//总共发现了num次
    		 for(int i=0; i<num;i++) {
    			 locationstart=inputstr.indexOf(patinfo[0], locationstart)+1;
    		 }
    		 locationend=inputstr.length()-locationstart-patinfo[0].length()+2;
    		 
    		 patinfo[1]=locationstart+"";
    		 patinfo[2]=locationend+"";
    		 listResult.add(patinfo);
       }
       return listResult;
    }
}
