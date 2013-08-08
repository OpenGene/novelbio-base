package com.novelbio.base.nbcReport;

import java.io.Serializable;
import java.util.List;

/**
 * 表格参数类
 * @author novelbio
 *
 */
public class XdocTable implements Cloneable,Serializable {
	private static final long serialVersionUID = 1L;
	
	private String weight = "0.0";
	private String cols = "";
	private String rows = "";
	private String backfillColor = "#00CCCC";
	private String singlefillColor = "#CCFFFF";
	private String doublefillColor = "#FFFFFF";
	private String upCompare = "";
	private String downCompare = "";
	private String title = "";
	private String note = "";
	private List<List<String>> lsExcelTable = null;

	/**
	 * 
	 * @param weight
	 *            边框宽度
	 * @param cols
	 *            列宽集合　如100,200,50,80
	 * @param rows
	 *            行高 如10,20,30
	 * @param backfillColor
	 *            表格第一行标题填充色
	 * @param singlefillColor
	 *            奇数行填充色
	 * @param doublefillColor
	 *            偶数行填充色
	 * @param upCompare
	 *            表格上方说明
	 * @param downCompare
	 *            表格下方说明
	 * @param title
	 *            表格总标题
	 * @param note
	 *            下方的 注:
	 */
	public XdocTable(String weight, String cols, String rows, String backfillColor, String singlefillColor, String doublefillColor, String upCompare, String downCompare, String title, String note) {
		if (weight != null) {
			this.weight = weight;
		}
		if (cols != null) {
			this.cols = cols;
		}
		if (rows != null) {
			this.rows = rows;
		}
		if (backfillColor != null) {
			this.backfillColor = backfillColor;
		}
		if (singlefillColor != null) {
			this.singlefillColor = singlefillColor;
		}
		if (doublefillColor != null) {
			this.doublefillColor = doublefillColor;
		}
		if (upCompare != null) {
			this.upCompare = upCompare;
		}
		if (downCompare != null) {
			this.downCompare = downCompare;
		}
		if (title != null) {
			this.title = title;
		}
		if (note != null) {
			this.note = note;
		}
	}
	
	public XdocTable(String cols, String rows) {
		if (cols != null) {
			this.cols = cols;
		}
		
		if (rows != null) {
			this.rows = rows;
		}
	}
	/** 边框宽度 */
	public String getWeight() {
		return weight;
	}
	/** 列宽集合　如100,200,50,80 */
	public String getCols() {
		return cols;
	}
	/**  行高 如10,20,30 */
	public String getRows() {
		return rows;
	}
	/**  表格第一行标题填充色 */
	public String getBackfillColor() {
		return backfillColor;
	}
	/** 奇数行填充色 */
	public String getSinglefillColor() {
		return singlefillColor;
	}
	/** 偶数行填充色 */
	public String getDoublefillColor() {
		return doublefillColor;
	}
	/** 表格上方说明 */
	public String getUpCompare() {
		return upCompare;
	}
	/** 表格下方说明 */
	public String getDownCompare() {
		return downCompare;
	}
	/** 表格总标题 */
	public String getTitle() {
		return title;
	}
	/** 下方的 注: */
	public String getNote() {
		return note;
	}
	/** 最大行数 */
	public int getMaxRow() {
		return rows.split(",").length;
	}
	/** 最大列数 */
	public int getMaxCol() {
		return cols.split(",").length;
	}
	/** 表格数据集合 */
	public List<List<String>> getLsExcelTable() {
		return lsExcelTable;
	}
	/** 边框宽度 */
	public void setWeight(String weight) {
		this.weight = weight;
	}
	/** 列宽集合　如100,200,50,80 */
	public void setCols(String cols) {
		this.cols = cols;
	}
	/**  行高 如10,20,30 */
	public void setRows(String rows) {
		this.rows = rows;
	}
	/**  表格第一行标题填充色 */
	public void setBackfillColor(String backfillColor) {
		this.backfillColor = backfillColor;
	}
	/** 奇数行填充色 */
	public void setSinglefillColor(String singlefillColor) {
		this.singlefillColor = singlefillColor;
	}
	/** 偶数行填充色 */
	public void setDoublefillColor(String doublefillColor) {
		this.doublefillColor = doublefillColor;
	}
	/** 表格上方说明 */
	public void setUpCompare(String upCompare) {
		this.upCompare = upCompare;
	}
	/** 表格下方说明 */
	public void setDownCompare(String downCompare) {
		this.downCompare = downCompare;
	}
	/** 表格总标题 */
	public void setTitle(String title) {
		this.title = title;
	}
	/** 下方的 注: */
	public void setNote(String note) {
		this.note = note;
	}
	/** 表格数据集合 */
	public void setLsExcelTable(List<List<String>> lsExcelTable) {
		this.lsExcelTable = lsExcelTable;
	}

	/**
	 * 取得克隆的对象
	 */
	public XdocTable getClone() {
		try {
			return (XdocTable) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}