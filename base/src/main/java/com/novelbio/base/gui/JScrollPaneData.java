package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * JScrollPane的扩展，方便添加和删除行
 * @author zong0jie
 *
 */
public class JScrollPaneData extends JScrollPane{
	private static final long serialVersionUID = -4238706503361283499L;
	
	DefaultTableModel defaultTableModel = null;
	JTable jTabFInput = new JTable();
	String[] title;
	
	public JScrollPaneData() {
		setViewportView(jTabFInput);
	}
	
	public JTable getjTabFInput() {
		return jTabFInput;
	}
	
	/**
	 * 往jScrollPane中添加表格，第一行为title
	 */
	public void setItemLs( List<String[]> lsInfo) {
		String[][] tableValue = null;
		title = lsInfo.get(0);
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInput.setModel(defaultTableModel);
		for (int i = 1; i < lsInfo.size(); i++) {
			defaultTableModel.addRow(lsInfo.get(i));
		}
	}
	
	/**
	 * 往jScrollPane中添加表格(lsls结构)，第一行为title
	 */
	public void setItemLsLs( List<List<String>> lsInfo) {
		ArrayList<String[]> lsStrs = new ArrayList<String[]>();
		for (List<String> list : lsInfo) {		
			String[] strs = new String[list.size()];
			for (int j = 0; j < list.size(); j++) {
				strs[j] = list.get(j);
			}
			lsStrs.add(strs);
		}
		setItemLs(lsStrs);
	}
	//不能用
//	public void setColumn(int... width) {
//		TableColumnModel tableColumnModel = new DefaultTableColumnModel();
//		for (int i = 0; i < width.length; i++) {
//			TableColumn tableColumn = new TableColumn(i, width[i]);
//			tableColumnModel.addColumn(tableColumn);
//		}
//		jTabFInputGo.setColumnModel(tableColumnModel);
//	}
	/**
	 * 往jScrollPane中添加表格，第一列为表头
	 */
	public void setTitle( String[] title) {
		String[][] tableValue = null;
		this.title = title;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInput.setModel(defaultTableModel);
	}
	
	/**
	 * 设定本表的选项
	 */
	public void setItem(int column, JComboBox jComboBox) {
		TableColumn tableCol =jTabFInput.getColumnModel().getColumn(column);
		tableCol.setCellEditor(new DefaultCellEditor(jComboBox));
	}
	
	/**
	 * 往jScrollPane中添加表格，如果没有title，则第一行为title
	 */
	public void addItemLs( List<String[]> lsInfo) {
		if (defaultTableModel == null) {
			setItemLs(lsInfo);
			return;
		}
		for (String[] strings : lsInfo) {
			defaultTableModel.addRow(strings);
		}
	}
	/**
	 * 往jScrollPane中添加表格，如果没有title，则第一行为title
	 */
	public void addItemLsSingle( List<String> lsInfo) {
		int colNum = 1;
		if (defaultTableModel != null) {
			colNum = defaultTableModel.getColumnCount();
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsInfo) {
			String[] tmpResult = new String[colNum];
			tmpResult[0] = string;
			lsResult.add(tmpResult);
		}
		addItemLs(lsResult);
	}
	/**
	 * 往jScrollPane中添加表格
	 */
	public void addItem(String[] info) {
		if (defaultTableModel == null) {
			String[][] tableValue = null;
			defaultTableModel = new DefaultTableModel(tableValue, info);
			jTabFInput.setModel(defaultTableModel);
			return;
		}
		
		defaultTableModel.addRow(info);
	}

	/**
	 * 没有就返回空的list
	 * @return
	 */
	public ArrayList<String[]> getLsDataInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
			String[] tmpResult = new String[defaultTableModel.getColumnCount()];
			for (int j = 0; j < defaultTableModel.getColumnCount(); j++) {
				try {
					tmpResult[j] = defaultTableModel.getValueAt(i, j).toString();
				} catch (Exception e) {
					tmpResult[j] = "";
				}
			
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	/**
	 * 删除实际行
	 * @param rowNum
	 */
	public void removeRow(int... rowNum) {
		MathComput.sort(rowNum, false);
		for (int i : rowNum) {
			if (i < 0 || i > defaultTableModel.getRowCount()) {
				continue;
			}
			defaultTableModel.removeRow(i - 1);
//			defaultTableModel.setRowCount(i);// 删除行比较简单，只要用DefaultTableModel的removeRow()方法即可。删除
//			// 行完毕后必须重新设置列数，也就是使用DefaultTableModel的setRowCount()方法来设置。
		}
	}
	/**
	 * 获得绝对行数
	 * @return
	 */
	public int[] getSelectRows() {
		int selectRows=jTabFInput.getSelectedRows().length;// 取得用户所选行的行数
		//单行
		if(selectRows==1) {
			int selectedRowIndex = jTabFInput.getSelectedRow(); // 取得用户所选单行
			return new int[]{selectedRowIndex + 1};
		}
		int[] selRowIndexs = null;
		if(selectRows>1) {
			selRowIndexs =jTabFInput.getSelectedRows();// 用户所选行的序列
			for (int i = 0; i < selRowIndexs.length; i++) {
				selRowIndexs[i] = selRowIndexs[i] + 1;
			}
			return selRowIndexs;
		}
		return null;
	}
	/**
	 * 删除用户选定的行
	 */
	public void deleteSelRows() {
		removeRow(getSelectRows());
	}
	/** 不稳定 */
	public void clean() {
		String[][] tableValue = null;
		defaultTableModel = new DefaultTableModel(tableValue, title);
		jTabFInput.setModel(defaultTableModel);
	}
	/**
	 * 从文件名获得文件的prefix并返回
	 * @param lsFileName
	 * @return
	 */
	public static ArrayList<String[]> getLsFileName2Prefix(ArrayList<String> lsFileName) {
		ArrayList<String[]> lsFileName2Prefix = new ArrayList<String[]>();
		for (String fileName : lsFileName) {
			String prefix = FileOperate.getFileNameSep(fileName)[0].split("_")[0];
			lsFileName2Prefix.add(new String[]{fileName, prefix});
		}
		return lsFileName2Prefix;
	}
	
	/**
	 * 将输入的每个文件名后面都加上一个suffix，然后返回
	 * @param lsFileName
	 * @return
	 */
	public static ArrayList<String[]> getLsFileName2Out(ArrayList<String> lsFileName, String append, String houzhuiming) {
		ArrayList<String[]> lsFileName2Prefix = new ArrayList<String[]>();
		for (String fileName : lsFileName) {
			String outFileName = FileOperate.changeFileSuffix(fileName, append, houzhuiming);
			lsFileName2Prefix.add(new String[]{fileName, outFileName});
		}
		return lsFileName2Prefix;
	}
}

