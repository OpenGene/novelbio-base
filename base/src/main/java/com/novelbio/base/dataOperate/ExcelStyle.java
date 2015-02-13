package com.novelbio.base.dataOperate;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelStyle {
	// TODO 有待完善，列的样式
	
	public static final short BLACK = IndexedColors.BLACK.getIndex();
	public static final short WHITE = IndexedColors.WHITE.getIndex();
	public static final short AQUA = IndexedColors.AQUA.getIndex();
	/** 颜色，40%的灰色 */
	public static final short GREY40 = IndexedColors.GREY_40_PERCENT.getIndex();
	/** 颜色，20%的灰色 */
	public static final short GREY25 = IndexedColors.GREY_25_PERCENT.getIndex();
	
	/** cell类型的枚举，如 titleLine标题行的cell*/
	enum EnumXlsCell {
		titleLine, evenLine, oddLine, endLine
	}
	/** 颜色类型的枚举，如BottomBorder下边框的颜色 */
	enum EnumXlsCellBorder {
		BG, BottomBorder, TopBorder, LeftBorder, RightBorder, Font
	}
	
	/** 表格的标题的样式 */
	private CellStyle titleStyle;
	/** 表格的奇数行样式 */
	private CellStyle evenStyle;
	/** 表格的偶数行样式 */
	private CellStyle oddStyle;
	/** 表格的结束行样式 */
	private CellStyle endStyle;
	/** 表格结束的行数 */
	private int endNum;
	
	/** 不同的cell对应的背景颜色
	 * key: 
	 */
	Map<EnumXlsCell, Map<EnumXlsCellBorder, Short>> mapCell_2_Border2Color = new HashMap<>();
	Map<EnumXlsCell, Map<EnumXlsCellBorder, Short>> mapCell_2_Border2Style = new HashMap<>();
	Map<EnumXlsCell, Font> mapCell_2_Font = new HashMap<>();
	
	public ExcelStyle() {
		for (EnumXlsCell xlsCell : EnumXlsCell.values()) {
			Map<EnumXlsCellBorder, Short> mapBorder2Color = new HashMap<>();
			Map<EnumXlsCellBorder, Short> mapBorder2Style = new HashMap<>();
			for (EnumXlsCellBorder xlsCellBorder : EnumXlsCellBorder.values()) {
				mapBorder2Color.put(xlsCellBorder, (short) -1);
				mapBorder2Style.put(xlsCellBorder, (short) -1);
			}
			mapCell_2_Border2Color.put(xlsCell, mapBorder2Color);
			mapCell_2_Border2Style.put(xlsCell, mapBorder2Style);
		}
	}
	
	/** 设置最后一行的行数 */
	public void setEndNum(int endNum) {
		this.endNum = endNum;
	}
	
	public void setWorkbook(Workbook workbook) {
		titleStyle = workbook.createCellStyle();
		evenStyle = workbook.createCellStyle();
		oddStyle = workbook.createCellStyle();
		endStyle = workbook.createCellStyle();
	}
	
	/** 设置边框，border：边框的类型， excelCell：cell的类型（标题行，奇数行等），borderType：边框的类型（上边框，下边框等）*/
	public void setBorder(EnumXlsCell excelCell, EnumXlsCellBorder border, short style) {
		mapCell_2_Border2Style.get(excelCell).put(border, style);
	}
	
	/** 设置颜色，color：颜色， excelCell：cell的类型（标题行，奇数行等），colorType：颜色的类型（上边框，下边框等）*/
	public void setColor(EnumXlsCell excelCell, EnumXlsCellBorder border, short color) {
		mapCell_2_Border2Color.get(excelCell).put(border, color);
	}
	
	private void setTitleStyle() {
		setCellStyle(titleStyle, EnumXlsCell.titleLine);
		setCellStyle(evenStyle, EnumXlsCell.evenLine);
		setCellStyle(oddStyle, EnumXlsCell.oddLine);
		setCellStyle(endStyle, EnumXlsCell.endLine);
	}
	
	/** 设置表格的样式，style为需要设置的样式，foreGroundColor为单元格的前景色，borderTop为上边框样式， borderBottom为下边框样式 */
	private void setCellStyle(CellStyle cellStyle, EnumXlsCell excelCell) {
//		Font font = mapCell_2_Font.get(excelCell);
//		cellStyle.setFont(font);
		
		Map<EnumXlsCellBorder, Short> mapBorder2Color = mapCell_2_Border2Color.get(excelCell);
		Map<EnumXlsCellBorder, Short> mapBorder2Style = mapCell_2_Border2Style.get(excelCell);
		
		for (EnumXlsCellBorder border : mapBorder2Style.keySet()) {
			short color = mapBorder2Color.get(border);
			short style = mapBorder2Style.get(border);
			switch (border) {
			case LeftBorder :
				if (style >= 0) cellStyle.setBorderLeft(style);
				if (color >= 0) cellStyle.setLeftBorderColor(color);
				break;
			case RightBorder :
				if (style >= 0) cellStyle.setBorderRight(style);
				if (color >= 0) cellStyle.setRightBorderColor(color);
				break;
			case TopBorder :
				if (style >= 0) cellStyle.setBorderTop(style);
				if (color >= 0) cellStyle.setTopBorderColor(color);
				break;
			case BottomBorder :
				if (style >= 0) cellStyle.setBorderBottom(style);
				if (color >= 0) cellStyle.setBottomBorderColor(color);
				break;
			case BG ://TODO 测试 背景 能产生什么结果
				if (color >= 0) cellStyle.setFillForegroundColor(color);
				break;
			default:
				break;
			}
		}
	}
	
	/** 渲染单元格，cell为一个单元格对象，rowNum为第几行（用来判断是奇数行偶数行首行或尾行） */
	public void renderCell(Cell cell, int rowNum) {
		if (rowNum == 0) {
			cell.setCellStyle(titleStyle);
		} else if (rowNum + 1 == endNum) {
			cell.setCellStyle(endStyle);
		} else if (rowNum%2 == 0) {
			cell.setCellStyle(oddStyle);
		} else {
			cell.setCellStyle(evenStyle);
		}
	}
	
	public static ExcelStyle getThreeLineTable(int endNum, Workbook workbook) {
		ExcelStyle excelStyle = new ExcelStyle();
		excelStyle.setEndNum(endNum);
		excelStyle.setWorkbook(workbook);
		
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.TopBorder, CellStyle.BORDER_THICK);
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.BottomBorder, CellStyle.BORDER_THIN);
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.LeftBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.RightBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.evenLine, EnumXlsCellBorder.TopBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.evenLine, EnumXlsCellBorder.BottomBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.evenLine, EnumXlsCellBorder.LeftBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.evenLine, EnumXlsCellBorder.RightBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.oddLine, EnumXlsCellBorder.TopBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.oddLine, EnumXlsCellBorder.BottomBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.oddLine, EnumXlsCellBorder.LeftBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.oddLine, EnumXlsCellBorder.RightBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.endLine, EnumXlsCellBorder.TopBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.endLine, EnumXlsCellBorder.BottomBorder, CellStyle.BORDER_THICK);
		excelStyle.setBorder(EnumXlsCell.endLine, EnumXlsCellBorder.LeftBorder, CellStyle.BORDER_NONE);
		excelStyle.setBorder(EnumXlsCell.endLine, EnumXlsCellBorder.RightBorder, CellStyle.BORDER_NONE);
		
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.BG, AQUA);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.TopBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.BottomBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.RightBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.BG, GREY40);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.BottomBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.RightBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.BG, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.BottomBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.RightBorder, WHITE);
		if (endNum%2 == 0) {
			excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BG, WHITE);
		} else {
			excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BG, GREY40);
		}
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BottomBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.RightBorder, WHITE);

		excelStyle.setTitleStyle();
		return excelStyle;
	}

}
