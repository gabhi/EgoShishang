package net.walnutvision.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;

public class MyBytes {

	private static Map<String, Object> dummyObjectMap = new HashMap<String,Object>();
	public static byte[] LONG_BUFFER = null;
	public static byte[] INT_BUFFER = null;
	
	static
	{
		long testLong = 0;
		LONG_BUFFER = MyBytes.toBytes(testLong);
		int testInt = 0;
		INT_BUFFER = MyBytes.toBytes(testInt);
		
	}
	static
	{
		dummyObjectMap.put(int.class.getName(), (int)0);
		dummyObjectMap.put(short.class.getName(), (short)0);
		dummyObjectMap.put(long.class.getName(), (long)0);
		dummyObjectMap.put(float.class.getName(), (float)0.0);
		dummyObjectMap.put(double.class.getName(), (double)0.0);
		dummyObjectMap.put(String.class.getName(),"");
		dummyObjectMap.put(Object.class.getName(), new Object());
		
		dummyObjectMap.put(Integer.class.getName(), Integer.valueOf(0));
		dummyObjectMap.put(Float.class.getName(), Float.valueOf(3.14f));
		dummyObjectMap.put(Double.class.getName(), Double.valueOf(2.78f));
		dummyObjectMap.put(Short.class.getName(), Short.valueOf((short) 51));
		dummyObjectMap.put(byte[].class.getName(), new byte[1]);
	}
	
	public static Object getDummyObject(Class cls)
	{
		String clsName = cls.getName();
		Object dummyObject = null;
		boolean isArray = cls.isArray();

		if( dummyObjectMap.containsKey(clsName))
		{
			dummyObject =  dummyObjectMap.get(clsName);
		}
		else
		{
			Constructor ct;
			
			try {
				if(isArray)
				{
					dummyObject = Array.newInstance(cls.getComponentType(), 1);
				}
				else
				{
					ct = cls.getConstructor();
					dummyObject = ct.newInstance();
				}

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dummyObjectMap.put(clsName, dummyObject);
		}
		return dummyObject;
		
	}
	public static byte[] toBytes(Object obj) {
		byte[] objByte = null;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(obj);
			objectStream.flush();
			objByte = byteStream.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objByte;
	}

	public static byte[] toBytes(boolean boolVal)
	{
		return Bytes.toBytes(boolVal);
	}
	
	public static byte[] toBytes(int intVal)
	{
		return Bytes.toBytes(intVal);
	}
	public static byte[] toBytes(long longVal)
	{
		return Bytes.toBytes(longVal);
	}
	public static byte[] toBytes(float floatVal)
	{
		return Bytes.toBytes(floatVal);
	}
	public static byte[] toBytes(double doubleVal)
	{
		return Bytes.toBytes(doubleVal);
	}

	public static byte[] toBytes(byte[] byteArray)
	{
		return byteArray;
	}

	public static byte[] toBytes(short sVal)
	{
		return Bytes.toBytes(sVal);
	}
	
	public static byte[] toBytes(String str)
	{
		return Bytes.toBytes(str);
	}
	
	public static byte[] toBytes(long[] arr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(long l : arr)
		{
			byte[] tmpBytes = MyBytes.toBytes(l);
			try {
				baos.write(tmpBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}
	
	public static byte[] toBytes(int[] arr)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int l : arr)
		{
			byte[] tmpBytes = MyBytes.toBytes(l);
			try {
				baos.write(tmpBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return baos.toByteArray();
	}
	
	public static <T> byte[] toBytes(List<T> objList)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int objCnt = objList.size();
			baos.write(MyBytes.toBytes(objCnt));
			//first write how many 
			for(int i = 0; i < objList.size(); i++)
			{
				byte[] objBytes = MyBytes.toBytes(objList.get(i));
				int objSize = objBytes.length;
				baos.write(MyBytes.toBytes(objSize));
				baos.write(objBytes);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
//
//	public static <T> List<T> toObject(byte[] byteArray, List<T> dummy)
//	{
//		List<T> objList = new LinkedList<T>();
//		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
//		try {
//			bais.read(MyBytes.INT_BUFFER);
//			int objCnt = MyBytes.toObject(MyBytes.INT_BUFFER, (int)0);
//			for(int i = 0; i < objCnt; i++)
//			{
//				bais.read(INT_BUFFER);
//				int objSize = MyBytes.toObject(MyBytes.INT_BUFFER, (int)0);
//				byte[] tmpBuffer = new byte[objSize];
//				bais.read(tmpBuffer);
//				Class<T> cls;
//				T object = (T)MyBytes.toObject(tmpBuffer,MyBytes.getDummyObject(cls));
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return objList;
//	}
//	
	public static int toObject(byte[] byteArray,int dummy)
	{
		return Bytes.toInt(byteArray);
	}
	
	public static long toObject(byte[] byteArray,long dummy)
	{
		return Bytes.toLong(byteArray);
	}

	public static float toObject(byte[] byteArray, float dummy)
	{
		return Bytes.toFloat(byteArray);
	}
	
	public static double toObject(byte[] byteArray, double dummy)
	{
		return Bytes.toDouble(byteArray);
	}

	public static boolean toObject(byte[] byteArray, boolean dummy)
	{
		return Bytes.toBoolean(byteArray);
	}
	
	public static short toObject(byte[] byteArray, short dummy)
	{
		return Bytes.toShort(byteArray);
	}
	
	public static String toObject(byte[] byteArray, String dummy)
	{
		return Bytes.toString(byteArray);
	}
	
	public static long[] toObject(byte[] byteArray,long[] dummy)
	{
		int longCnt = byteArray.length / LONG_BUFFER.length;
		long[] longArr = new long[longCnt];
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		for(int i = 0; i < longCnt; i++)
		{
			bais.read(LONG_BUFFER, 0, LONG_BUFFER.length);
			longArr[i] = (Long) MyBytes.toObject(LONG_BUFFER, MyBytes.getDummyObject(long.class));
		}
		return longArr;
	}
	
	public static int[] toObject(byte[] byteArray,int[] dummy)
	{
		int longCnt = byteArray.length / INT_BUFFER.length;
		int[] longArr = new int[longCnt];
		ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
		for(int i = 0; i < longCnt; i++)
		{
			bais.read(INT_BUFFER, 0, INT_BUFFER.length);
			longArr[i] = (Integer) MyBytes.toObject(INT_BUFFER, MyBytes.getDummyObject(int.class));
		}
		return longArr;
	}
		
	public static Object toObject(byte[] byteArray, Object dummy) {
		if(	dummy instanceof byte[])
		{
			return byteArray;
		}
		if(dummy instanceof java.lang.String)
		{
			return toObject(byteArray,(String)dummy);
		}
		if(dummy instanceof java.lang.Integer)
		{
		    return toObject(byteArray, (int)0);	
		}
		if(dummy instanceof java.lang.Long)
		{
			return toObject(byteArray,(long)0);
		}
		
		if(dummy instanceof long[])
		{
			return toObject(byteArray,(long[])dummy);
		}

		if(dummy instanceof int[])
		{
			return toObject(byteArray,(int[])dummy);
		}

		Serializable obj = null;
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(
					byteArray);
			ObjectInputStream objectStream = new ObjectInputStream(byteStream);
			obj = (Serializable)objectStream.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	
	public static void streamCopy(InputStream is, OutputStream os, int bufSize)
	{
		byte[] buffer = new byte[bufSize];
		int readLen = 0;
		try {
			while( (readLen = is.read(buffer)) > 0)
			{
				os.write(buffer, 0, readLen);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
