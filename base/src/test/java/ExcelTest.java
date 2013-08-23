
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelOperate.*;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;


public class ExcelTest {
	public static void main(String[] args) {
		ExcelOperate excelOperate = new ExcelOperate("/home/novelbio/桌面/abc.xlsx");
		excelOperate.changeToNBCExcel(1);
		//excelOperate.changeRowBGColor(1, ArrayOperate.converList2Array(lsRowBGColorNBCs),IndexedColors.WHITE.getIndex());
//		List<String[]> aaaList = new ArrayList<String[]>();
//		String[] aStrings = {"a","b"};
//		aaaList.add(aStrings);
//		excelOperate.WriteExcel("sb", 1, 1, aaaList);
//		excelOperate.Close();
//		excelOperate.changeCellBGColor(1,new CellBGColorNBC[]{new CellBGColorNBC(1, 1, IndexedColors.GREEN.getIndex())});
		excelOperate.Close();
//		FileOperate.copyFile("/home/novelbio/桌面/GOTest/tmp2013-08-140205-12940/aaaaa.docx", "maprfs:/nbcCloud/aaaa.docx", true);
	}
}
