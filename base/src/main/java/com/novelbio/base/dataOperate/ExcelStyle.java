package com.novelbio.base.dataOperate;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import com.novelbio.base.SepSign;

public class ExcelStyle {
	
	public static final short BLACK = IndexedColors.BLACK.getIndex();
	public static final short WHITE = IndexedColors.WHITE.getIndex();
	public static final short AQUA = IndexedColors.AQUA.getIndex();
	/** 颜色，40%的灰色 */
	public static final short GREY40 = IndexedColors.GREY_40_PERCENT.getIndex();
	/** 颜色，20%的灰色 */
	public static final short GREY25 = IndexedColors.GREY_25_PERCENT.getIndex();
	/** 自动默认的颜色 */
	public static final short AUTOMATIC =  IndexedColors.AUTOMATIC.getIndex();
	
	/** cell类型的枚举，如 titleLine标题行的cell*/
	enum EnumXlsCell {
		titleLine, evenLine, oddLine, endLine, firstCol, evenCol, oddCol, blank
	}
	/** 颜色类型的枚举，如BottomBorder下边框的颜色 */
	enum EnumXlsCellBorder {
		BG, BottomBorder, TopBorder, LeftBorder, RightBorder, Font
	}
	
	/** 冻结第几列，譬如excel的首列固定，就设置为1
	 * 0表示不冻结
	 */
	int freezePaneCol = 0;
	/** 冻结第几列，譬如excel的首行固定，就设置为1
	 *  0表示不冻结
	 */
	int freezenPaneRow = 0;
	
	private Workbook wb;
	
	/** 表格的标题行的样式 */
	private ExcelCellStyle titleLineStyle;
	/** 表格的奇数行样式 */
	private ExcelCellStyle evenLineStyle;
	/** 表格的偶数行样式 */
	private ExcelCellStyle oddLineStyle;
	/** 表格的结束行样式 */
	private ExcelCellStyle endLineStyle;
	/** 表格的首列样式 */
	private ExcelCellStyle firstColStyle;
	/** 表格的奇数列样式 */
	private ExcelCellStyle evenColStyle;
	/** 表格的偶数列样式 */
	private ExcelCellStyle oddColStyle;
	/** 空的样式 */
	private ExcelCellStyle blankStyle;
	
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
	
	Map<EnumXlsCell, Boolean> mapCell_2_IsText = new HashMap<>();
	
	/** 同一行的单元格可以使用一种类型的style，因此可以将同一种类型的单元格放到这个map里面来
	 * key: style组合的名称
	 * value: style的具体形式
	 */
	Map<String, ExcelCellStyle> mapStyleInfo2Style = new HashMap<>();
	
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
			mapCell_2_IsText.put(xlsCell, false);
			// 默认字体为Times New Roman
			ExcelFont normalFont = new ExcelFont();
			mapCell_2_Font.put(xlsCell, normalFont);
		}
	}
	
	public void setFreezenPane(int freezenPaneRow, int freezePaneCol) {
		this.freezenPaneRow = freezenPaneRow;
		this.freezePaneCol = freezePaneCol;
	}
	
	public int getFreezenPaneRow() {
		return freezenPaneRow;
	}
	public int getFreezePaneCol() {
		return freezePaneCol;
	}
	
	/** 输入的表格有多少行 */
	protected void setAllLineNum(int allLineNum) {
		if (endNum < 0) {
			endNum = allLineNum;
		}
	}
	
	/** 设置第一行和最后一行的行数，从1开始
	 * @param startNum
	 * @param endNum 小于0表示全部行
	 */
	public void setStartAndEndNum(int startNum, int endNum) {
		this.startNum = startNum;
		this.endNum = endNum;
	}
	
	public void setWorkbook(Workbook workbook) {
		titleLineStyle = new ExcelCellStyle(workbook, "title");
		evenLineStyle = new ExcelCellStyle(workbook, "evenLine");
		oddLineStyle = new ExcelCellStyle(workbook, "oddLine");
		endLineStyle = new ExcelCellStyle(workbook, "endLine");
		firstColStyle = new ExcelCellStyle(workbook, "firstCol");
		evenColStyle = new ExcelCellStyle(workbook, "evenCol");
		oddColStyle = new ExcelCellStyle(workbook, "oddCol");
		blankStyle = new ExcelCellStyle(workbook, "blank");
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
	
	/** 设置是否为text，如may5等必须是text才行，excelCell：cell的类型（标题行，奇数行等）
	 * ，border：边框（上边框，下边框等也包括背景颜色），boolean：是否为text */
	public void setIsText(EnumXlsCell excelCell, Boolean isText) {
		mapCell_2_IsText.put(excelCell, isText);
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
	private void setCellStyle(ExcelCellStyle cellStyle, EnumXlsCell excelCell) {
		//设定字体
		ExcelFont fontExcel = mapCell_2_Font.get(excelCell);
		cellStyle.setExcelFont(fontExcel);
		
		//设定是否为text
		boolean isText = mapCell_2_IsText.get(excelCell); 
		if (isText) {
			DataFormat format = wb.createDataFormat();
			cellStyle.setDataFormat(format.getFormat("@"));
		}
		
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
		cellStyle.getCellStyle().setFillPattern(CellStyle.SOLID_FOREGROUND);

	}

	/** 渲染单元格，cell为一个单元格对象，rowNum为第几行（用来判断是奇数行偶数行首行或尾行），colNum为第几列 */
	public void renderCell(Cell cell, int rowNum, int colNum) {
		ExcelCellStyle resultStyle = getColStyle(colNum);
		if (endNum >= 0 && rowNum + 1 > endNum) {
			cell.setCellStyle(blankStyle.getCellStyle());
		} else {
			if ((rowNum - startNum + 1)%2 == 0) {
				resultStyle = unionCellStyle(resultStyle, oddLineStyle);
			} else {
				resultStyle = unionCellStyle(resultStyle, evenLineStyle);
			}
			if (rowNum + 1  == startNum) {
				resultStyle = unionCellStyle(resultStyle, titleLineStyle);
			}
			if (rowNum + 1 == endNum) {
				resultStyle = unionCellStyle(resultStyle, endLineStyle);
			}
			cell.setCellStyle(resultStyle.getCellStyle());
		}
	}
	
	/** 渲染单元格，cell为一个单元格对象，rowNum为第几行（用来判断是奇数行偶数行首行或尾行），colNum为第几列 */
	public void renderRow(Row row, int rowNum) {
		ExcelCellStyle resultStyle = getColStyle(2);
		if (endNum >= 0 && rowNum + 1 > endNum) {
			row.setRowStyle(blankStyle.getCellStyle());
		} else {
			if ((rowNum - startNum + 1)%2 == 0) {
				resultStyle = unionCellStyle(resultStyle, oddLineStyle);
			} else {
				resultStyle = unionCellStyle(resultStyle, evenLineStyle);
			}
			if (rowNum + 1  == startNum) {
				resultStyle = unionCellStyle(resultStyle, titleLineStyle);
			}
			if (rowNum + 1 == endNum) {
				resultStyle = unionCellStyle(resultStyle, endLineStyle);
			}
			row.setRowStyle(resultStyle.getCellStyle());
		}
	}
	
	/** 根据列号获得所对应的列样式 */
	private ExcelCellStyle getColStyle(int colNum) {
		if (colNum == 0) {
			return firstColStyle;
		} else if (colNum%2 == 0) {
			return oddColStyle;
		} else {
			return evenColStyle;
		}
	}
	
	/** 把cellStyle2里非默认的参数设置进cellStyle1 */
	public ExcelCellStyle unionCellStyle(ExcelCellStyle styleRaw, ExcelCellStyle styleNew) {
		String key = styleRaw.getKey() + SepSign.SEP_ID + styleNew.getKey();
		if (mapStyleInfo2Style.containsKey(key)) {
			return mapStyleInfo2Style.get(key);
		}
		
		ExcelCellStyle cellStyle = new ExcelCellStyle(wb, key);
		cellStyle.cloneExcelCellStyle(styleRaw);
		// 判断cellStyle2样式的参数是不是默认的，如果不是默认的就改变cellStyle1对应的参数
		if (styleNew.getFillForegroundColor() != -1) cellStyle.setFillForegroundColor(styleNew.getFillForegroundColor());
		if ( styleNew.getBorderTop() != -1) cellStyle.setBorderTop( styleNew.getBorderTop());
		if (styleNew.getBorderBottom() != -1) cellStyle.setBorderBottom(styleNew.getBorderBottom());
		if (styleNew.getBorderLeft() != -1) cellStyle.setBorderLeft(styleNew.getBorderLeft());
		if (styleNew.getBorderRight() != -1) cellStyle.setBorderRight(styleNew.getBorderRight());
		if (styleNew.getTopBorderColor() != -1) cellStyle.setTopBorderColor(styleNew.getTopBorderColor());
		if (styleNew.getBottomBorderColor() != -1) cellStyle.setBottomBorderColor(styleNew.getBottomBorderColor());
		if (styleNew.getLeftBorderColor() != -1) cellStyle.setLeftBorderColor(styleNew.getLeftBorderColor());
		if (styleNew.getRightBorderColor() != -1) cellStyle.setRightBorderColor(styleNew.getRightBorderColor());
		if (styleNew.isChangeFont()) cellStyle.setExcelFont(styleNew.getExcelFont());
		if (styleNew.getDataFormat() != -1) cellStyle.setDataFormat(styleNew.getDataFormat());
		
		mapStyleInfo2Style.put(key, cellStyle);
		return cellStyle;
	}

	/**
	 * 获取三线表的样式
	 * @param startNum 开始的行号，从1开始
	 * @param endNum 结束的行号，从1开始，如果endNum小于0，表示直到结束行
	 * @return
	 */
	public static ExcelStyle getThreeLineTable(int startNum, int endNum) {
		ExcelStyle excelStyle = new ExcelStyle();
		excelStyle.setFreezenPane(1, 1);
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
	
	protected ExcelCellStyle getTitleLineStyle() {
		return titleLineStyle;
	}
	protected void setTitleLineStyle(ExcelCellStyle titleLineStyle) {
		this.titleLineStyle = titleLineStyle;
	}
	protected ExcelCellStyle getEvenLineStyle() {
		return evenLineStyle;
	}
	protected void setEvenLineStyle(ExcelCellStyle evenLineStyle) {
		this.evenLineStyle = evenLineStyle;
	}
	protected ExcelCellStyle getOddLineStyle() {
		return oddLineStyle;
	}
	protected void setOddLineStyle(ExcelCellStyle oddLineStyle) {
		this.oddLineStyle = oddLineStyle;
	}
	protected ExcelCellStyle getEndLineStyle() {
		return endLineStyle;
	}
	protected void setEndLineStyle(ExcelCellStyle endLineStyle) {
		this.endLineStyle = endLineStyle;
	}
	protected ExcelCellStyle getFirstColStyle() {
		return firstColStyle;
	}
	protected void setFirstColStyle(ExcelCellStyle firstColStyle) {
		this.firstColStyle = firstColStyle;
	}
	protected ExcelCellStyle getEvenColStyle() {
		return evenColStyle;
	}
	protected void setEvenColStyle(ExcelCellStyle evenColStyle) {
		this.evenColStyle = evenColStyle;
	}
	protected ExcelCellStyle getOddColStyle() {
		return oddColStyle;
	}
	protected void setOddColStyle(ExcelCellStyle oddColStyle) {
		this.oddColStyle = oddColStyle;
	}

}

class ExcelCellStyle {
	CellStyle cellStyle;
	ExcelFont excelFont;
	Font font;
	short colorBG = -1;
	short colorTopBorder = -1;
	short colorBottomBorder = -1;
	short colorRightBorder = -1;
	short colorLeftBorder = -1;
	short topBorder = -1;
	short bottomBorder = -1;
	short rightBorder = -1;
	short leftBorder = -1;
	short dataFormat = -1;
	
	String key;
	
	public ExcelCellStyle(Workbook wb, String key) {
		this.cellStyle = wb.createCellStyle();
		this.font = wb.createFont();
		this.key = key;
	}
	
	public void setExcelFont(ExcelFont excelFont) {
		if (excelFont == null) return;
		
		this.excelFont = excelFont;
		excelFont.fillFont(font);
		cellStyle.setFont(font);
	}
	
	public boolean isChangeFont() {
		return excelFont == null? false: true;
	}
	
	
	public ExcelFont getExcelFont() {
		return excelFont;
	}
	
	public CellStyle getCellStyle() {
		return cellStyle;
	}
	
	public void cloneExcelCellStyle(ExcelCellStyle excelCellStyle) {
//		MyBeanUtils.copyNotNullProperties(excelCellStyle, this);
		cellStyle.cloneStyleFrom(excelCellStyle.getCellStyle());
		this.bottomBorder = excelCellStyle.bottomBorder;
		this.colorBG = excelCellStyle.colorBG;
		this.colorBottomBorder = excelCellStyle.colorBottomBorder;
		this.colorLeftBorder = excelCellStyle.colorLeftBorder;
		this.colorRightBorder = excelCellStyle.colorRightBorder;
		this.colorTopBorder = excelCellStyle.colorTopBorder;
		this.excelFont = excelCellStyle.excelFont.clone();
		excelFont.fillFont(this.font);
		this.leftBorder = excelCellStyle.leftBorder;
		this.rightBorder = excelCellStyle.rightBorder;
		this.topBorder = excelCellStyle.topBorder;		
	}
	
	public void setDataFormat(short dataFormat) {
		this.dataFormat = dataFormat;
		cellStyle.setDataFormat(dataFormat);
	}
	public short getDataFormat() {
		return dataFormat;
	}
	
	public void setLeftBorderColor(short color) {
		this.colorLeftBorder = color;
		cellStyle.setLeftBorderColor(color);
	}
	public short getLeftBorderColor() {
		return colorLeftBorder;
	}

	public void setRightBorderColor(short color) {
		this.colorRightBorder = color;
		cellStyle.setRightBorderColor(color);
	}
	public short getRightBorderColor() {
		return colorRightBorder;
	}

	public void setTopBorderColor(short color) {
		this.colorTopBorder = color;
		cellStyle.setTopBorderColor(color);
	}
	public short getTopBorderColor() {
		return colorTopBorder;
	}

	public void setBottomBorderColor(short color) {
		this.colorBottomBorder = color;
		cellStyle.setBottomBorderColor(color);
	}
	public short getBottomBorderColor() {
		return colorBottomBorder;
	}

	public void setFillForegroundColor(short bg) {
		this.colorBG = bg;
		cellStyle.setFillForegroundColor(bg);
	}
	public short getFillForegroundColor() {
		return colorBG;
	}

	public void setBorderLeft(short border) {
		this.leftBorder = border;
		cellStyle.setBorderLeft(border);
	}
	public short getBorderLeft() {
		return leftBorder;
	}

	public void setBorderRight(short border) {
		this.rightBorder = border;
		cellStyle.setBorderRight(border);
	}
	public short getBorderRight() {
		return rightBorder;
	}

	public void setBorderTop(short border) {
		this.topBorder = border;
		cellStyle.setBorderTop(border);
	}
	public short getBorderTop() {
		return topBorder;
	}

	public void setBorderBottom(short border) {
		this.bottomBorder = border;
		cellStyle.setBorderBottom(border);
	}
	public short getBorderBottom() {
		return bottomBorder;
	}
	
	public String getKey() {
		return key;
	}

}

class ExcelFont implements Cloneable {
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
	
	public ExcelFont clone() {
		try {
			return (ExcelFont) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
