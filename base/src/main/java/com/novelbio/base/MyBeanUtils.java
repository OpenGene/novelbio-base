package com.novelbio.base;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

public class MyBeanUtils extends org.springframework.beans.BeanUtils {
	/**
	 * 把新对象中不为null的属性拷贝到旧对象中
	 * 
	 * @param source
	 *            来源
	 * @param target
	 *            目标
	 * @throws BeansException
	 */
	public static <T> Map<String, String> copyNotNullProperties(T source) throws BeansException {
		Map<String, String> mapResult = new HashMap<>();
		Assert.notNull(source, "Source must not be null");
		Class<?> actualEditable = source.getClass();
		PropertyDescriptor[] sourcePds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor sourcePd : sourcePds) {
			try {
				Method readMethod = sourcePd.getReadMethod();
				if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
					readMethod.setAccessible(true);//不是public的方法，就需要设置为可获取
				}
				if (sourcePd.getName().equals("class")) {
					continue;
				}
				Object value = readMethod.invoke(source);
				// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
				if (value != null) {
					mapResult.put(sourcePd.getName(), value.toString());
				}
			} catch (Throwable ex) {
				throw new FatalBeanException("Could not copy properties from source to target", ex);
			}
		}
		return mapResult;
	}
	
	//TODO 写单元测试
	/**
	 * 把新对象中不为null的属性拷贝到旧对象中
	 * 只有当某个属性既有read method又有 write method的时候本方法才会起作用
	 * @param source
	 *            来源
	 * @param target
	 *            目标
	 * @throws BeansException
	 */
	public static <T,K> K copyNotNullProperties(T source, K target) throws BeansException {
		Assert.notNull(target, "Target must not be null");
		Assert.notNull(source, "Source must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);//不是public的方法，就需要设置为可获取
						}
						Object value = readMethod.invoke(source);
						// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
						if (value != null) {
							Method writeMethod = targetPd.getWriteMethod();
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);
						}
					} catch (Throwable ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		return target;
	}

	/**
	 * 把新对象中不为null的属性拷贝到旧对象中
	 * 
	 * @param source
	 *            来源
	 * @param target
	 *            目标
	 * @throws BeansException
	 */
	public static <T,K> K copyAllProperties(T source, K target) throws BeansException {
		Assert.notNull(target, "Target must not be null");
		Assert.notNull(source, "Source must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source);
						// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, value);
					} catch (Throwable ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		return target;
	}

}
