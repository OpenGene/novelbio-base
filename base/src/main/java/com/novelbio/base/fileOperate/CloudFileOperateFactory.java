/**
 *
 * @author novelbio fans.fan
 * @date 2018年5月30日
 */
package com.novelbio.base.fileOperate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.util.ServiceEnvUtil;

/**
 *
 * @author novelbio fans.fan
 */
public class CloudFileOperateFactory {
	private static final Logger logger = LoggerFactory.getLogger(CloudFileOperateFactory.class);
	private static CloudFileOperateFactory cloudFileOperateFactory = new CloudFileOperateFactory();

	ICloudFileOperate cloudFileOperate;

	/**
	 * 私有方法并初始化相关基本类.
	 * 
	 * @date 2015年12月23日
	 */
	private CloudFileOperateFactory() {
		if (!ServiceEnvUtil.isCloudEnv()) {
			logger.info("is not cloud env. return ");
			return;
		}
		try {
			cloudFileOperate = (ICloudFileOperate) Class.forName("com.novelbio.erp.biz.project.domain.CloudFileOperate").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
	}

	public static CloudFileOperateFactory getInstance() {
		return cloudFileOperateFactory;
	}

	public ICloudFileOperate getCloudFileOperate() {
		return cloudFileOperate;
	}

}
