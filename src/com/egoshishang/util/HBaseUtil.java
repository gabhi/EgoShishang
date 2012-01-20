package com.egoshishang.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.data.RowWritable;

public class HBaseUtil {
	
	protected static void listFieldToByte(List<Object> objList, String colQualifier, Put put)
	{
		for(int i = 0; i < objList.size(); i++)
		{
			String tmpQualifier = colQualifier + i;
			byte[] qualifierByte = MyBytes.toBytes(tmpQualifier);
			byte[] colByte = MyBytes.toBytes(objList.get(i));
			put.add(RowWritable.CF_BYTES,qualifierByte,colByte);
		}
	}
	
	protected static void fieldToByte(Object obj, String colQualiier, Put put)
	{
		byte[] objBytes = MyBytes.toBytes(obj);
		put.add(RowWritable.CF_BYTES, MyBytes.toBytes(colQualiier), objBytes);
	}
	
	protected static void byteToField(RowWritable rowObject, String fieldName, Class fieldClass, byte[] colBytes)
	{
		try {
			Field fld = rowObject.getClass().getDeclaredField(fieldName);
			fld.setAccessible(true);
			Object fieldObj = MyBytes.toObject(colBytes, MyBytes.getDummyObject(fieldClass));
			fld.set(rowObject, fieldObj);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	protected static void listToField(RowWritable rowObject, String fieldName, Class elementType, List<byte[]> colBytes)
	{
		//get the list element type, so that we know how to cast it back
		try {
			Field field = rowObject.getClass().getDeclaredField(fieldName);
			List<Object> fieldList = new LinkedList<Object>();
			for(byte[] col : colBytes)
			{
				fieldList.add(MyBytes.toObject(col, MyBytes.getDummyObject(elementType)));
			}
			field.set(rowObject, fieldList);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void writeToBase(RowWritable obj, List<String> memberList)
	{
		//use reflection and mapping to copy members to put
		Put put = new Put(obj.getRowKey());
		Class thisCls = obj.getClass();
		Map<String,String> mcMap1 = obj.getFieldColumnMap();
		Map<String,String> mcMap = mcMap1;
		if(memberList != null)
		{
			mcMap  = new HashMap<String,String>();
			for(String member : memberList)
				mcMap.put(member, mcMap1.get(member));
		}

		for(Entry<String,String> ent :mcMap.entrySet())
		{
			String memberClsName = ent.getKey();
			String colClsName = ent.getValue();
			//get the member bytes
			try {
				Field fld = thisCls.getDeclaredField(memberClsName);
				fld.setAccessible(true);
				Type fldType = fld.getGenericType();
				Object fieldObject = fld.get(obj);
				if(fldType instanceof ParameterizedType)
				{
					ParameterizedType pt = (ParameterizedType)fldType;
					Type[] argTypes = pt.getActualTypeArguments();
					HBaseUtil.listFieldToByte((List<Object>)fieldObject, colClsName, put);
				}
				else
				{
					HBaseUtil.fieldToByte(fieldObject, colClsName, put);
				}
				HTable table = (HTable)HBaseInstance.getInstance().getTableFromPool(obj.getTableName());
				table.put(put);
				HBaseInstance.getInstance().getTablePool().putTable(table);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	public static List<String> getAllMembers(RowWritable obj)
	{
		Class cls = obj.getClass();
		List<String> memberList = new LinkedList<String>();
		Set<String> keySet = obj.getFieldColumnMap().keySet();
		for(String key : keySet)
		{
			memberList.add(key);
		}
		return memberList;
	}
	
	public static boolean verifyMember(RowWritable obj, List<String> memberList)
	{
		Class cls = obj.getClass();
		int found = 0;
		for(String member: memberList)
		{
			Field fld;
			try {
				fld = cls.getDeclaredField(member);
				fld.setAccessible(true);
				found++;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(found == memberList.size())
			return true;
		else
			return false;
	}
	
	public static  Result query(RowWritable obj, List<String> members)
	{
		if(members == null)
		{
			members = getAllMembers(obj);
		}
		Get get = new Get(obj.getRowKey());
		Map<String,String> mcMap = obj.getFieldColumnMap();
		for(String member : members)
		{
			String colStr = mcMap.get(member);
			byte[] colByte = MyBytes.toBytes(colStr);
			get.addColumn(RowWritable.CF_BYTES, colByte);
		}
		
		HTable table = (HTable) HBaseInstance.getInstance().getTableFromPool(obj.getTableName());
		try {
			Result result = table.get(get);
			HBaseInstance.getInstance().getTablePool().putTable(table);
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected static String isListMember(String fieldName)
	{
		Pattern pat = Pattern.compile("(\\w+)(\\d+$)");
		Matcher mat = pat.matcher(fieldName);
		String res = null;
		while(mat.find())
		{
			res = mat.group(1);
			break;
		}
		return res;
	}
	
	public static void resultToMember(Result result,RowWritable obj)
	{
		if(result == null)
			return;
		Class thisCls = obj.getClass();
		Map<String,String> cmMap = obj.getColumnFieldMap();
		Map<String, List<byte[]>> listMemberMap = new HashMap<String,List<byte[]>>();
		for(KeyValue kv: result.list())
		{
			byte[] colByte = kv.getQualifier();
			String colName = (String) MyBytes.toObject(colByte, MyBytes.getDummyObject(String.class));
			//now get the field name
			String fldName = cmMap.get(colName);
			String isListField = isListMember(fldName);
			if(isListField != null)
			{
				if(listMemberMap.containsKey(isListField))
				{
					listMemberMap.get(isListField).add(colByte);
				}
				else
				{
					List<byte[]> tmpList = new LinkedList<byte[]>();
					tmpList.add(colByte);
					listMemberMap.put(isListField, tmpList);
				}
			}
			else
			{
				//add them directly
				try {
					Field memberField = obj.getClass().getDeclaredField(fldName);
					Class fieldClass = (Class)memberField.getGenericType();
					HBaseUtil.byteToField(obj, fldName, fieldClass, colByte);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//now handle the list field type
		for(Entry<String, List<byte[]>> ent : listMemberMap.entrySet())
		{
			String colName = ent.getKey();
			Field memberField;
			try {
				memberField = obj.getClass().getDeclaredField(colName);
				memberField.setAccessible(true);
				Type fieldType = memberField.getGenericType();
				ParameterizedType pt = (ParameterizedType)fieldType;
				Type[] argTypes = pt.getActualTypeArguments();
				Type rawType = pt.getRawType();
				Class elementType = (Class)argTypes[0];
				if(rawType instanceof java.util.List)
				{
					HBaseUtil.listToField(obj, colName, elementType, ent.getValue());					
				}
				else
				{
					
				}
				
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	public static void readFromBase(RowWritable obj, List<String> memberList){
		Result result = query(obj, memberList);
		resultToMember(result,obj);
	}
}
