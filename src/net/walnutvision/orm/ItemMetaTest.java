package net.walnutvision.orm;

import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.util.MyBytes;

import org.junit.Test;



public class ItemMetaTest {

	
	@Test
	public void testUpdateMeta()
	{
		ItemMeta meta1 = new ItemMeta();
		ItemMeta meta2 = new ItemMeta();
		meta1.addColumn(ItemMeta.MERCHANT, MyBytes.toBytes(ItemMeta.AMAZON));
		meta1.addColumn(ItemMeta.CURRENCY_CODE, MyBytes.toBytes(ItemMeta.CNY));
		meta1.addColumn(ItemMeta.ASIN, MyBytes.toBytes("1234"));
		meta1.addColumn(ItemMeta.LIST_PRICE, MyBytes.toBytes(10.0f));
		meta1.addColumn(ItemMeta.LOWEST_NEW_PRICE, MyBytes.toBytes(6.0f));
		meta1.addColumn(ItemMeta.TITLE, MyBytes.toBytes("book1"));

		meta2.addColumn(ItemMeta.MERCHANT, MyBytes.toBytes(ItemMeta.AMAZON));
		meta2.addColumn(ItemMeta.CURRENCY_CODE, MyBytes.toBytes(ItemMeta.CNY));
		meta2.addColumn(ItemMeta.ASIN, MyBytes.toBytes("1234"));
		meta2.addColumn(ItemMeta.LIST_PRICE, MyBytes.toBytes(10.0f));
		meta2.addColumn(ItemMeta.LOWEST_NEW_PRICE, MyBytes.toBytes(6.0f));
		meta2.addColumn(ItemMeta.TITLE, MyBytes.toBytes("book1"));
		
		String [] photoList1 = {"p1","p2","p3"};
		String [] photoList2 = {"p1","q2","q3"};
		
		for(int i = 0 ;i < photoList1.length; i++)
		{
			meta1.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(photoList1[i]));
			meta2.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(photoList2[i]));
		}
		meta1.updateItem(meta2);
		System.out.println(meta1);
	}
}
