package net.walnutvision.util;


import java.util.LinkedList;
import java.util.List;

import net.walnutvision.orm.HBaseObject.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class MyBytesTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void testString()
	{
		String str = "helefk_1|dkjf_2";
		byte[] strBytes = MyBytes.toBytes(str);
		String str1 = (String)MyBytes.toObject(strBytes, MyBytes.getDummyObject(String.class));
		System.out.println(str1);
	}
//	@Test
	public void testType()
	{
		int a = 10;
		byte[] aArray = MyBytes.toBytes(10);
		int b = MyBytes.toObject(aArray, 0);
		System.out.println(b);
		int[] aa = {10,20,30};
		byte[] aaArray = MyBytes.toBytes(aa);
		int[] aaRecover = (int[])MyBytes.toObject(aaArray, new Object());
		for(int i : aaRecover)
		{
			System.out.print(i + "\t");
		}
		System.out.println();
	}
//	@Test
	public void testDummy()
	{
		Object[] objArray = {(int)10,(float)3.14, "", new ItemMeta()};
		for(Object obj: objArray)
		{
			System.out.println(MyBytes.getDummyObject(obj.getClass()));			
		}
	
	}
//	@Test
	public void testByteArray()
	{
		byte[] byteArray = MyBytes.toBytes("some testing stuff");
		byte[] recovered = (byte[])MyBytes.toObject(byteArray,MyBytes.getDummyObject(byte[].class));
		String recoverStr = (String)MyBytes.toObject(recovered, MyBytes.getDummyObject(String.class) );
		System.out.println(recoverStr);
	}
//	@Test
	public void testLongArray()
	{
		int[] longArr = {1,2,3,4,5};
		byte[] byteArr = MyBytes.toBytes(longArr);
		int[] rLongArr = (int[])MyBytes.toObject(byteArr,MyBytes.getDummyObject(longArr.getClass()));
		for(long l : rLongArr)
		{
			System.out.println(l);
		}
	}
	
	@Test
	public void testStringArray()
	{
		String[] strArr = {"hello,world","what else?"};
		byte[] byteArr = MyBytes.toBytes(strArr);
		String[] rLongArr = (String[])MyBytes.toObject(byteArr,MyBytes.getDummyObject(strArr.getClass()));
		for(String l : rLongArr)
		{
			System.out.println(l);
		}
	}
	
//	@Test
	public void testListObject()
	{
		List<String> strList = new LinkedList<String>();
		strList.add("a");
		Class cls = List.class;
		System.out.println(cls.getName());
		if(cls.isInstance(new LinkedList<Integer>()))
		{
			System.out.println("list object");
		}
	}
	@Test
	public void testRowSerializable()
	{
		ItemMeta meta = new ItemMeta();
		meta.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes("1.jpg"));
		meta.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes("2.jpg"));
		System.out.println(meta.toString() + "\n");
		List<byte[]> photoUrlList = meta.getColumnList(ItemMeta.PHOTO_URL);
		for(byte[] urlBytes : photoUrlList)
		{
			String url = (String)MyBytes.toObject(urlBytes, MyBytes.getDummyObject(String.class));
			System.out.println(url);
		}
	}
}
