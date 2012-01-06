package com.egoshishang.util;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.egoshishang.data.ItemMeta;

public class DataUtilityTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testWriteObject()
	{
		ItemMeta itemMeta = new ItemMeta();
		itemMeta.asin = "ADFDF";
		itemMeta.extra = "nothing else";
		itemMeta.photoUrl = "http://google.jpg";
		itemMeta.title = "testing object";
		itemMeta.url = "http://detail.com";
		byte[] objByte = DataUtility.objectToByteArray(itemMeta);
		ItemMeta itemMeta2 = (ItemMeta)DataUtility.byteArrayToObject(objByte);
		System.out.println(itemMeta2.toString());
	}

}
