package net.walnutvision.orm;

import net.walnutvision.util.MyBytes;

import org.junit.Before;
import org.junit.Test;


public class RowSerializableTest {
	public static class RowSerializableObject extends RowSerializable
	{

		public static final String PHOTO_URL = "pu";
		@Override
		public String getTableName() {
			return "test";
		}
	}
	RowSerializableObject pso = null;
	@Before
	public void setUp()
	{
		pso = new RowSerializableObject();
	}
	
	@Test
	public void testAdd()
	{
		for(int i = 0; i < 10 ;i ++)
		{
			pso.addColumn(RowSerializableObject.PHOTO_URL, MyBytes.toBytes("url_" + i));
		}
		pso.removeColumn(RowSerializable.synthesizeFullColumnName(RowSerializableObject.PHOTO_URL, 0));
		pso.removeColumn(RowSerializable.synthesizeFullColumnName(RowSerializableObject.PHOTO_URL, 3));
		pso.addColumn(RowSerializableObject.PHOTO_URL, MyBytes.toBytes("url extra"));
		pso.addColumn(RowSerializableObject.PHOTO_URL, MyBytes.toBytes("url extra 1"));
		pso.removeColumnList(RowSerializableObject.PHOTO_URL);
		pso.addColumn(RowSerializableObject.PHOTO_URL, MyBytes.toBytes("url extra 2"));
		System.out.println(pso.toString());	
	}
	
}
