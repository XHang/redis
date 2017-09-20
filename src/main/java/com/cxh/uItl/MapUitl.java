package com.cxh.uItl;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * map工具类
 * @author Administrator
 *
 */
public class MapUitl {
	
	
//挖坑，不填
/*	public static Map<String,String> objectToMap(Object object){
		Class<? extends Object> clazz = object.getClass();
		for(Method method:clazz.getMethods()){
		if((method.getName().startsWith("get") && isPropertiesMethod(method,clazz))){
			
			}
		}
	}*/
	
	
	
	/**
	 * 判断方法是不是属于属性的方法
	 * ps：如果是属性方法，正常应该set和get成对出现
	 * @param set/get方法对象
	 * @param clazz 方法所属类
	 * @return
	 */
	public static boolean isPropertiesMethod(Method method,Class clazz){
		String methodName = method.getName();
		boolean isPropertiesMethod=false;
		try{
				if(methodName.contains("set")){
					String getMethodName = "get"+method.getName().substring(3);
					clazz.getMethod(getMethodName, null);
					isPropertiesMethod=true;
				}else if (methodName.contains("get")){
					Class<?> returnClass = method.getReturnType();
					String setMethodName = "set"+method.getName().substring(3);
					clazz.getMethod(setMethodName, returnClass);
					isPropertiesMethod=true;
				}else{
					isPropertiesMethod=false;
				}
		}catch(NoSuchMethodException e){
			isPropertiesMethod=false;
		}
		return isPropertiesMethod;
	}
	public  boolean isSetPropertiesMethod(Method method,Class clazz){
		String methodName = method.getName();
		try{
			if(methodName.contains("set")){
				String getMethodName = "get"+method.getName().substring(3);
				clazz.getMethod(getMethodName, null);
				return true;
			}
		}catch(NoSuchMethodException e){
			return false;
		}
		return false;
	}
	
	
	/**
	 * 获取属性方法所代表的属性名
	 * @return
	 */
	private  String getPropertiesByMethod(Method method){
		String methodName = method.getName();
		 String propertiesName = methodName.substring(3); 
		 return firstCharLowerCase(propertiesName);
	}
	 /**
     * 将字符串的首字母改成小写
     * @param source
     * @return
     */
    public  String firstCharLowerCase(String source){
    	 char[] cs = source.toCharArray();
         cs[0] += 32;
         return String.valueOf(cs);
    }
}
