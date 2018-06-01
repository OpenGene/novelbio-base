/**
 *
 * @author novelbio fans.fan
 * @date 2018年5月29日
 */
package com.novelbio.base.fileOperate;

import java.nio.file.Path;
import java.util.List;

/**
 * 云平台的公共文件和task结果文件都有存在数据库.通过实现该接口,减少Fileoperate操作对象存储的频率
 * 
 * @author novelbio fans.fan
 */
public interface ICloudFileOperate {

	public boolean isDbSavedPath(Path path);
	
//	public boolean isFileExist(Path path);
//	
//	public boolean isFileDirectory(Path path);
//	
//	public long getFileSizeLong(Path path);
//	
//	public long getTimeLastModify(Path path);

	public List<Path> getLsFoldPath(Path file, String filename, String suffix);

	public List<Path> getLsFoldPathRecur(Path file, String filename, String suffix, boolean isNeedFolder);

}
