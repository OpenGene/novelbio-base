/**
 *
 * @author novelbio fans.fan
 * @date 2018年5月30日
 */
package com.novelbio.base.fileOperate;

import java.lang.reflect.Modifier;
import java.util.Set;

import com.novelbio.base.reflect.ClassFinder;
import com.novelbio.base.util.ServiceEnvUtil;

/**
 *
 * @author novelbio fans.fan
 */
public class CloudFileOperateFactory {
	private static CloudFileOperateFactory cloudFileOperateFactory = new CloudFileOperateFactory();

	ICloudFileOperate cloudFileOperate;

	/**
	 * 私有方法并初始化相关基本类.
	 * 
	 * @date 2015年12月23日
	 */
	private CloudFileOperateFactory() {
		if (!ServiceEnvUtil.isCloudEnv()) {
			return;
		}
		//TODO 这里的包路径因为接口和实现类的不相同.先这么写.后边改成动态的
		Set<Class<?>> setAllClazz = ClassFinder.getClasses("com.novelbio.erp.biz.project.domain");
		for (Class<?> clazz : setAllClazz) {
			if (!ICloudFileOperate.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers())
					|| clazz.isInterface()) {
				// 不是ICloudFileOperate的子类或是抽象类,就不要
				continue;
			}
			try {
				cloudFileOperate = (ICloudFileOperate) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static CloudFileOperateFactory getInstance() {
		return cloudFileOperateFactory;
	}

	public ICloudFileOperate getCloudFileOperate() {
		return cloudFileOperate;
	}

}
