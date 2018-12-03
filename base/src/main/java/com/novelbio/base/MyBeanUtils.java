package com.novelbio.base;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

import net.sf.cglib.beans.BeanMap;

/**
 * 本类进行source到target的拷贝时，相同的字段名对应的类型要一致。<br>
 * 当出现相同字段对应不应的类型时，请使用
 * {@link MyBeanUtils#copyWithSameType(Object, Object, boolean)} 方法。
 * 
 * @author novelbio liqi
 * @date 2018年11月30日 下午2:30:21
 */
public class MyBeanUtils extends BeanUtils {

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
		BeanMap beanMap = BeanMap.create(source);
		for (Object field : beanMap.keySet()) {
			Object value = beanMap.get(field);
			if (value != null) {
				mapResult.put(field.toString(), value.toString());
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
		return copyAllProperties(source, target, false);
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
		// 获取soure中不为null的属性
		Map<String, Object> notNullMap = new HashMap<>();
		BeanMap sourceMap = BeanMap.create(source);
		for (Object field : sourceMap.keySet()) {
			Object value = sourceMap.get(field);
			// isCoypNull复制空属性时， 或者 value不为null
			if (isCoypNull || value != null) {
				notNullMap.put(field.toString(), value);
			}
		}
		BeanMap targetMap = BeanMap.create(target);
		targetMap.putAll(notNullMap);
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
		Map<String, Object> allPropertiesMap = new HashMap<>();
		// 添加所有属性后，移除id
		BeanMap sourceMap = BeanMap.create(source);
		allPropertiesMap.putAll(sourceMap);
		allPropertiesMap.remove("id");
		// 合并到target中
		BeanMap targetMap = BeanMap.create(target);
		targetMap.putAll(allPropertiesMap);
		return target;
	}

	/**
	 * 将Map的属性拷贝到对象的属性中<br>
	 * key要等于对象中的属性名，对应的属性需要有set方法<br>
	 * 属性中可以有map，通过map.a设置map中的属性，map暂不支持嵌套
	 * 
	 * <p>
	 * 当前仅能处理最后一级为map的情况，map和对象混用会抛出runtimeException。需要改进
	 * 
	 * @param source
	 * @param target
	 */
	public static void copyMap2Object(Map<String, Object> source, Object target) {
		for (String key : source.keySet()) {
			try {
				if (key.indexOf(".") == -1) { // 不存在map
					BeanUtils.copyProperty(target, key, source.get(key));
				} else {
					// field最少有2个数据
					String[] fields = key.split("\\."); // 拆分field和map的key
					List<BeanMap> lsObj = new ArrayList<>();
					Object temObj = target;
					int lastIndex = fields.length - 1;
					for (int i = 0; i < lastIndex; i++) { // fields数组 0 --> (length-2)
						BeanMap beanMap = BeanMap.create(temObj);
						lsObj.add(beanMap);
						temObj = beanMap.get(fields[i]);
					}

					// 赋值fields最末尾一个
					if (temObj instanceof Map) {
						((Map) temObj).put(fields[lastIndex], source.get(key));
					} else {
						BeanUtils.copyProperty(temObj, fields[lastIndex], source.get(key));
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

	/**
	 * <strong>优先使用其他方法</strong><br>
	 * 仅拷贝相同字段相同类型的方法, (容器类，暂未实现)针对容器类，会进行范型的判断。<br>
	 * 比如:<blockquote> {@code List<Object>} 可以接受 {@code List<String>} </blockquote>
	 * 
	 * @param source
	 *            拷贝来源对象
	 * @param target
	 *            拷贝目标对象
	 * @param isCoypNull
	 *            是否拷贝null
	 * @return 合并后的target
	 */
	public static <S, T> T copyWithSameType(S source, T target, boolean isCoypNull) {
		Validate.notNull(target, "Target must not be null");
		Validate.notNull(source, "Source must not be null");
		// 可以被复制的属性
		List<String> lsCopyFieldName = new ArrayList<>();
		// 来源对象获取field
		Map<String, Field> mapSourceRef = new HashMap<>();
		List<Field> lsSourceField = Lists.newArrayList(source.getClass().getDeclaredFields());
		lsSourceField.forEach(sField -> {
			mapSourceRef.put(sField.getName(), sField);
		});
		// 目标字段获取field
		Map<String, Field> mapTargetRef = new HashMap<>();
		List<Field> lsTargetField = Lists.newArrayList(target.getClass().getDeclaredFields());
		lsTargetField.forEach(tField -> {
			mapTargetRef.put(tField.getName(), tField);
		});
		for (String fieldName : mapSourceRef.keySet()) {
			if (mapTargetRef.containsKey(fieldName)) {
				// source中的field
				Field sfield = mapSourceRef.get(fieldName);
				// target中的field
				Field tfield = mapTargetRef.get(fieldName);
				if (sfield.getType().equals(tfield.getType())) {
					lsCopyFieldName.add(fieldName);
				}
			}
		}

		BeanMap sourceMap = BeanMap.create(source);
		// 可以被拷贝的属性
		Map<String, Object> mapCopyProperties = new HashMap<>();
		// 填充可以被拷贝的属性
		for (String key : lsCopyFieldName) {
			if (!sourceMap.containsKey(key)) {
				continue;
			}
			Object sourceProperty = sourceMap.get(key);
			// 不拷贝null值且source属性为null时，跳过
			if (!isCoypNull && sourceProperty == null) {
				continue;
			}
			mapCopyProperties.put(key, sourceProperty);
		}

		// copy到目标对象中
		BeanMap targetMap = BeanMap.create(target);
		targetMap.putAll(mapCopyProperties);
		return target;
	}
}
