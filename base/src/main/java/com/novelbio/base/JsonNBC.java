package com.novelbio.base;

import java.util.Collection;
import java.util.List;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

public class JsonNBC {
	/**
	 * 把一个对象转成json字符串
	 * @param object
	 * @return
	 */
	public static String fromObject(Object object){
		return JSONObject.fromObject(object).toString();
	}
	
	/**
	 * 改变Json中的null为""
	 * @param jsonObject
	 * @return
	 */
	public static String changeNull(String jsonString) {
		return jsonString.replaceAll("null", "\"\"");
	}
	
	/**
	 * 过滤无关的属性,返回json字符串
	 * @param filterField
	 * @param list
	 * @return
	 */
    public static String modelBeanToJSON(final List<String> filterField, Collection<?> list){
        JSONArray jsonObjects = new JSONArray();  
        for (Object obj : list) {            
            JsonConfig jsonConfig = new JsonConfig();
            if(filterField == null){
            	JSONObject jsonObject = JSONObject.fromObject(obj);
            	jsonObjects.add( jsonObject); 
            	continue;
            }
            jsonConfig.setJsonPropertyFilter(new FilterJsonName(filterField));
            
            JSONObject jsonObject = JSONObject.fromObject(obj, jsonConfig);
            jsonObjects.add( jsonObject); 
		}
        return jsonObjects.toString();
    }
    
}

/**
 * 过滤器，仅过滤属性的名字
 * @author novelbio
 */
class FilterJsonName implements PropertyFilter  {
	List<String> lsFilterField;

	public FilterJsonName(List<String> lsFilterField) {
		this.lsFilterField = lsFilterField;
	}
	
	@Override
	public boolean apply(Object arg0, String name, Object value) {
        boolean isFiltered=false;
        for(String string : lsFilterField){
            if(string.equals(name)){
                isFiltered = true;
            }
        }
        if (isFiltered) {
            return true; 
        } 
        return false; 
	}
}

