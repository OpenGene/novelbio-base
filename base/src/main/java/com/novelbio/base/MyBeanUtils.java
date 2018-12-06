package com.novelbio.base;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.BeansException;

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
	public static <T> Map<String, String> copyNotNullProperties(T source) throws ExceptionNbcBean {
		Map<String, String> mapResult = new HashMap<>();
		Validate.notNull(source, "Source must not be null");
		Class<?> actualEditable = source.getClass();
		PropertyDescriptor[] sourcePds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor sourcePd : sourcePds) {
			try {
				Method readMethod = sourcePd.getReadMethod();
				if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
					readMethod.setAccessible(true);// 不是public的方法，就需要设置为可获取
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
				throw new ExceptionNbcBean("Could not copy properties from source to target", ex);
			}
		}
		return mapResult;
	}

	// TODO 写单元测试
	/**
	 * 把新对象中不为null的属性拷贝到旧对象中 只有当某个属性既有read method又有 write method的时候本方法才会起作用
	 * 
	 * @param source
	 *            来源
	 * @param target
	 *            目标
	 * @throws BeansException
	 */
	public static <T, K> K copyNotNullProperties(T source, K target) throws ExceptionNbcBean {
		Validate.notNull(target, "Target must not be null");
		Validate.notNull(source, "Source must not be null");
		Class<?> actualEditable = target.getClass();
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);// 不是public的方法，就需要设置为可获取
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
						throw new ExceptionNbcBean("Could not copy properties from source to target", ex);
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
	public static <T, K> K copyAllProperties(T source, K target) throws ExceptionNbcBean {
		return copyAllProperties(source, target, false);
	}

	/**
	 * 把新对象中不为null的属性拷贝到旧对象中
	 * 
	 * @param source
	 * @param target
	 * @param isCoypNull
	 *            是否拷贝为null的信息
	 * @return
	 * @throws ExceptionNbcBean
	 */
	public static <T, K> K copyAllProperties(T source, K target, boolean isCoypNull) throws ExceptionNbcBean {
		Validate.notNull(target, "Target must not be null");
		Validate.notNull(source, "Source must not be null");
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
						if (!isCoypNull && value == null) {
							continue;
						}
						// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, value);
					} catch (Throwable ex) {
						throw new ExceptionNbcBean("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		return target;
	}

	/**
	 * 把source对象的属性拷贝到target对象中，除了"id"属性
	 * 
	 * @param source
	 * @param target
	 * @param isCoypNull
	 *            是否拷贝为null的信息
	 * @return
	 * @throws ExceptionNbcBean
	 */
	public static <T, K> K copyAllPropertiesWithoutId(T source, K target, boolean isCoypNull) throws ExceptionNbcBean {
		Validate.notNull(target, "Target must not be null");
		Validate.notNull(source, "Source must not be null");
		Class<?> actualEditable = target.getClass();
		Class<?> sourceClass = source.getClass();
		PropertyDescriptor sourcePd = null;
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			if (!targetPd.getName().equals("id") && targetPd.getWriteMethod() != null) {
				sourcePd = getPropertyDescriptor(sourceClass, targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source);
						if (!isCoypNull && value == null) {
							continue;
						}
						// 这里判断以下value是否为空 当然这里也能进行一些特殊要求的处理 例如绑定时格式转换等等
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, value);
					} catch (Throwable ex) {
						throw new ExceptionNbcBean("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		return target;
	}

	/**
	 * 将Map的属性拷贝到对象的属性中<br>
	 * key要等于对象中的属性名，对应的属性需要有set方法<br>
	 * 属性中可以有map，通过map.a设置map中的属性，map暂不支持嵌套
	 * 
	 * <p>
	 * 当前仅能处理最后一级为map的情况，map和对象混用会抛出runtimeException。需要改进
	 * @param source
	 * @param target
	 */
	public static void copyMap2Object(Map<String, Object> source, Object target) {
		for (String key : source.keySet()) {
			try {
				if (key.indexOf(".") == -1) { // 不存在map
					org.apache.commons.beanutils.BeanUtils.copyProperty(target, key, source.get(key));
				} else {
					// field最少有2个数据
					String[] fields = key.split("\\."); // 拆分field和map的key
					List<BeanMap> lsObj = new ArrayList<>();
					Object temObj = target;
					int lastIndex = fields.length - 1;
					for (int i = 0; i < lastIndex; i++) { // fields数组 0 --> (length-2)
						BeanMap beanMap = new BeanMap(temObj);
						lsObj.add(beanMap);
						temObj = beanMap.get(fields[i]);
					}

					// 赋值fields最末尾一个
					if (temObj instanceof Map) {
						((Map) temObj).put(fields[lastIndex], source.get(key));
					} else {
						org.apache.commons.beanutils.BeanUtils.copyProperty(temObj, fields[lastIndex], source.get(key));
					}

					// 反向循环，逐层往外赋值
					int lsMaxIndex = lsObj.size() - 1;
					for (int j = lsMaxIndex; j > -1; j--) { // lsObj (length-1) --> 0
						BeanMap fartherMap = lsObj.get(j);
						fartherMap.put(fields[j], temObj);
						temObj = fartherMap.getBean();
					}
					target = temObj;
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"copyMap2Object error! map-key=" + key + "---objectClass=" + target.getClass(), e);
			}
		}
	}
}
