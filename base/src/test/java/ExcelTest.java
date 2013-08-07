
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.novelbio.base.dataOperate.ExcelOperate;


public class ExcelTest {
	public static void main(String[] args) {
		ExcelOperate excelOperate = new ExcelOperate("maprfs:/gao.xls");
//		List<String[]> aaaList = new ArrayList<String[]>();
//		String[] aStrings = {"a","b"};
//		aaaList.add(aStrings);
//		excelOperate.WriteExcel("sb", 1, 1, aaaList);
//		excelOperate.WriteExcel("sb1", 1, 1, aaaList);
		//excelOperate.changeCellBGColor(1, 1, 1,IndexedColors.BLUE.getIndex());
	}
}
