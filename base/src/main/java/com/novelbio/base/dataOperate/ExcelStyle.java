package com.novelbio.base.dataOperate;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelStyle {
	
	public static final short BLACK = IndexedColors.BLACK.getIndex();
	public static final short WHITE = IndexedColors.WHITE.getIndex();
	public static final short AQUA = IndexedColors.AQUA.getIndex();
	/** 颜色，40%的灰色 */
	public static final short GREY40 = IndexedColors.GREY_40_PERCENT.getIndex();
	/** 颜色，20%的灰色 */
	public static final short GREY25 = IndexedColors.GREY_25_PERCENT.getIndex();
	
	/** cell类型的枚举，如 titleLine标题行的cell*/
	enum EnumXlsCell {
		titleLine, evenLine, oddLine, endLine, firstCol, evenCol, oddCol, blank
	}
	/** 颜色类型的枚举，如BottomBorder下边框的颜色 */
	enum EnumXlsCellBorder {
		BG, BottomBorder, TopBorder, LeftBorder, RightBorder, Font
	}
	
	private Workbook wb;
	
	/** 表格的标题行的样式 */
	private CellStyle titleLineStyle;
	/** 表格的奇数行样式 */
	private CellStyle evenLineStyle;
	/** 表格的偶数行样式 */
	private CellStyle oddLineStyle;
	/** 表格的结束行样式 */
	private CellStyle endLineStyle;
	/** 表格的首列样式 */
	private CellStyle firstColStyle;
	/** 表格的奇数列样式 */
	private CellStyle evenColStyle;
	/** 表格的偶数列样式 */
	private CellStyle oddColStyle;
	/** 空的样式 */
	private CellStyle blankStyle;
	
	/** 表格开始的行数 */
	private int startNum;
	/** 表格结束的行数 */
	private int endNum;
	
	/** 不同的cell对应的背景颜色
	 * key: cell的类型（标题行，奇数行等）
	 * value：不同的边框对应的颜色，也包括背景颜色，也是个map，key和value如下
	 * 		key：边框的类型（上边框，下边框等也包括背景颜色）
	 * 		value：颜色
	 */
	Map<EnumXlsCell, Map<EnumXlsCellBorder, Short>> mapCell_2_Border2Color = new HashMap<>();
	/** 不同的cell对应的边框类型
	 * key: cell的类型（标题行，奇数行等）
	 * value：不同的边框对应的线的粗细，也是个map，key和value如下
	 * 		key：边框的类型（上边框，下边框等）
	 * 		value：边框为粗线细线等
	 */
	Map<EnumXlsCell, Map<EnumXlsCellBorder, Short>> mapCell_2_Border2Style = new HashMap<>();
	/**不同的cell对应的字体 
	 * key：cell的类型（标题行，奇数行等）
	 * value：字体
	  */
	Map<EnumXlsCell, ExcelFont> mapCell_2_Font = new HashMap<>();
	
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
			// 默认字体为Times New Roman
			ExcelFont normalFont = new ExcelFont();
			mapCell_2_Font.put(xlsCell, normalFont);
		}
	}
	
	/** 设置最后一行的行数 */
	public void setStartAndEndNum(int startNum, int endNum) {
		this.startNum = startNum;
		this.endNum = endNum;
	}
	
	public void setWorkbook(Workbook workbook) {
		titleLineStyle = workbook.createCellStyle();
		evenLineStyle = workbook.createCellStyle();
		oddLineStyle = workbook.createCellStyle();
		endLineStyle = workbook.createCellStyle();
		firstColStyle = workbook.createCellStyle();
		evenColStyle = workbook.createCellStyle();
		oddColStyle = workbook.createCellStyle();
		blankStyle = workbook.createCellStyle(); 
		wb = workbook;
		setTitleStyle();
	}
	
	/** 设置边框， excelCell：cell的类型（标题行，奇数行等），border：边框（上边框，下边框等），border：边框的类型*/
	public void setBorder(EnumXlsCell excelCell, EnumXlsCellBorder border, short style) {
		mapCell_2_Border2Style.get(excelCell).put(border, style);
	}
	
	/** 设置颜色，excelCell：cell的类型（标题行，奇数行等），border：边框（上边框，下边框等也包括背景颜色），color：颜色*/
	public void setColor(EnumXlsCell excelCell, EnumXlsCellBorder border, short color) {
		mapCell_2_Border2Color.get(excelCell).put(border, color);
	}
	
	/** 设置字体，excelCell：cell的类型（标题行，奇数行等），border：边框（上边框，下边框等也包括背景颜色），font：字体色*/
	public void setFont(EnumXlsCell excelCell, ExcelFont font) {
		mapCell_2_Font.put(excelCell, font);
	}
	
	private void setTitleStyle() {
		setCellStyle(titleLineStyle, EnumXlsCell.titleLine);
		setCellStyle(evenLineStyle, EnumXlsCell.evenLine);
		setCellStyle(oddLineStyle, EnumXlsCell.oddLine);
		setCellStyle(endLineStyle, EnumXlsCell.endLine);
		setCellStyle(firstColStyle, EnumXlsCell.firstCol);
		setCellStyle(evenColStyle, EnumXlsCell.evenCol);
		setCellStyle(oddColStyle, EnumXlsCell.oddCol);
	}
	
	/** 设置表格的样式，style为需要设置的样式，foreGroundColor为单元格的前景色，borderTop为上边框样式， borderBottom为下边框样式 */
	private void setCellStyle(CellStyle cellStyle, EnumXlsCell excelCell) {
		ExcelFont fontExcel = mapCell_2_Font.get(excelCell);
		Font font = wb.createFont();
		fontExcel.fillFont(font);
		cellStyle.setFont(font);
		
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
			case BG :
				// 如果使用setFillBackgroundColor就会整个表格都会变成黑色
				if (color >= 0) cellStyle.setFillForegroundColor(color);
				break;
			default:
				break;
			}
		}
		// 指定填充模式，不加这行代码，单元格颜色的填充会失效
		cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		DataFormat format = wb.createDataFormat();
		cellStyle.setDataFormat(format.getFormat("@"));
	}

	/** 渲染单元格，cell为一个单元格对象，rowNum为第几行（用来判断是奇数行偶数行首行或尾行），colNum为第几列 */
	public void renderCell(Cell cell, int rowNum, int colNum) {
		CellStyle colStyle = getColStyle(colNum);
		if (rowNum + 1 > endNum) {
			cell.setCellStyle(blankStyle);
		} else {
			if (rowNum + 1  == startNum) {
				cell.setCellStyle(titleLineStyle);
			} else if (rowNum + 1 == endNum) {
				cell.setCellStyle(unionCellStyle(endLineStyle, colStyle));
			} else if ((rowNum - startNum + 1)%2 == 0) {
				cell.setCellStyle(unionCellStyle(oddLineStyle, colStyle));
			} else {
				cell.setCellStyle(unionCellStyle(evenLineStyle, colStyle));
			}
		}
	}
	
	/** 根据列号获得所对应的列样式 */
	private CellStyle getColStyle(int colNum) {
		if (colNum == 0) {
			return firstColStyle;
		} else if (colNum%2 == 0) {
			return oddColStyle;
		} else {
			return evenColStyle;
		}
	}
	
	/** 把cellStyle2里非默认的参数设置进cellStyle1 */
	private CellStyle unionCellStyle(CellStyle cellStyle1, CellStyle cellStyle2) {
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.cloneStyleFrom(cellStyle1);
		// 获得cellStyle2样式的参数
		short foreGroundColor = cellStyle2.getFillForegroundColor();
		short topBorder = cellStyle2.getBorderTop();
		short bottomBorder = cellStyle2.getBorderBottom();
		short leftBorder = cellStyle2.getBorderLeft();
		short rightBorder = cellStyle2.getBorderRight();
		short topBorderColor = cellStyle2.getTopBorderColor();
		short bottomBorderColor = cellStyle2.getBottomBorderColor();
		short leftBorderColor = cellStyle2.getLeftBorderColor();
		short rightBorderColor = cellStyle2.getRightBorderColor();
		// 判断cellStyle2样式的参数是不是默认的，如果不是默认的就改变cellStyle1对应的参数
		if (foreGroundColor != 64) cellStyle.setFillForegroundColor(foreGroundColor);
		if (topBorder != 0) cellStyle.setBorderTop(topBorder);
		if (bottomBorder != 0) cellStyle.setBorderBottom(bottomBorder);
		if (leftBorder != 0) cellStyle.setBorderLeft(leftBorder);
		if (rightBorder != 0) cellStyle.setBorderRight(rightBorder);
		if (topBorderColor != 8) cellStyle.setTopBorderColor(topBorderColor);
		if (bottomBorderColor != 8) cellStyle.setBottomBorderColor(bottomBorderColor);
		if (leftBorderColor != 8) cellStyle.setLeftBorderColor(leftBorderColor);
		if (rightBorderColor != 8) cellStyle.setRightBorderColor(rightBorderColor);
		return cellStyle;
	}

	/** 获取三线表的样式, startNum：开始的行号，endNum：结束的行号 */
	public static ExcelStyle getThreeLineTable(int startNum, int endNum) {
		ExcelStyle excelStyle = new ExcelStyle();
		excelStyle.setStartAndEndNum(startNum, endNum);
		// 设置边框的样式
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.TopBorder, CellStyle.BORDER_THICK);
		excelStyle.setBorder(EnumXlsCell.titleLine, EnumXlsCellBorder.BottomBorder, CellStyle.BORDER_THICK);
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
		// 设置颜色，背景颜色和边框颜色
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.BG, WHITE);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.TopBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.BottomBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.titleLine, EnumXlsCellBorder.RightBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.BG, GREY25);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.BottomBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.evenLine, EnumXlsCellBorder.RightBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.BG, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.BottomBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.oddLine, EnumXlsCellBorder.RightBorder, WHITE);
		// 判断最后一行是奇数行还是偶数行
		// 总行数
		int allRow = endNum - startNum + 1;
		if (allRow%2 == 0) {
			excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BG, GREY25);
		} else {
			excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BG, WHITE);
		}
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.TopBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.BottomBorder, BLACK);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.LeftBorder, WHITE);
		excelStyle.setColor(EnumXlsCell.endLine, EnumXlsCellBorder.RightBorder, WHITE);
		
		// 标题行字体
		ExcelFont fontTitle = new ExcelFont();
		fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
		excelStyle.setFont(EnumXlsCell.titleLine, fontTitle);
		
		return excelStyle;
	}

}

class ExcelFont {
	/** 如 宋体，Times New Roman */
	String fontName = "Times New Roman";
	/** 颜色 */
	short color = Font.COLOR_NORMAL;
	/** 粗体等 */
	short boldweight = Font.BOLDWEIGHT_NORMAL;
 
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
 
	public void setColor(short color) {
		this.color = color;
	}
 
	public void setBoldweight(short boldweight) {
		this.boldweight = boldweight;
	}
	
	protected void fillFont(Font font) {
		font.setFontName(fontName);
		font.setColor(color);
		font.setBoldweight(boldweight);
	}
	
}
