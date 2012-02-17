package net.walnutvision.crawl.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.walnutvision.crawl.AmazonItem;

/**
 * 
 * @author qizhao
 *define how a class mapped to an integer
 *this technique is used to cast a RowSeriazable object to its original type
 */
public class ClassIntegerMapper {
	public static final int INVALID_CLASS_ID = -1;
	protected Class[] clsArray= {
		net.walnutvision.crawl.AmazonItem.class	
	};
	protected Map<String,Integer> clsNameIntMap = new HashMap<String,Integer>();
	protected ClassIntegerMapper(){
		///init the map
		Integer clsIndex = 0;
		for(Class cls : clsArray)
		{
			String clsName = cls.getName();
			clsNameIntMap.put(clsName, clsIndex);
			clsIndex++;
		}
	}
	protected static ClassIntegerMapper INSTANCE = new ClassIntegerMapper();
	public static ClassIntegerMapper ref()
	{
		return INSTANCE;
	}
	public Object integerToObject(int objId) throws IllegalArgumentException, SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		//check the object id in the right range
		if(objId < 0 || objId >= clsArray.length)
			throw new IllegalArgumentException("object id is out of range");
		///get the Class and construct an object out of it
		Class cls = clsArray[objId];
		Constructor ct = cls.getConstructor();
		Object inst =  ct.newInstance();
		return inst;
	}
	public int objectToInteger(Object obj)
	{
		Class cls = obj.getClass();
		return classToInteger(cls);
	}
	
	public int classToInteger(Class cls)
	{
		if(clsNameIntMap.containsKey(cls.getName()))
		{
			return clsNameIntMap.get(cls.getName());
		}
		else
		{
			return INVALID_CLASS_ID;
		}
	}
}
