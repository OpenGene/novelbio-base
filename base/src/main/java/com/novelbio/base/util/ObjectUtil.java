package com.novelbio.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectUtil {

	/**
	 * 对象深度克隆<br/>
	 * <b>注意:对瞬态的属性,会丢失值</b>
	 * 
	 * @param t
	 * @return
	 */
	public static <T> T deepClone(T t) {
		T tc = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();  
			ObjectOutputStream out = new ObjectOutputStream(bo);  
			out.writeObject(t);
			out.close();
			
			//从流里读出来  
			ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());  
			ObjectInputStream oi = new ObjectInputStream(bi);  
			tc = (T) oi.readObject();
			oi.close();
			
			return tc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tc;
	}

}
