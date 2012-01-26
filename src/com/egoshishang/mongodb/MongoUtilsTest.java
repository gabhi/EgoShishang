package com.egoshishang.mongodb;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.egoshishang.orm.HBaseObject.ItemImage;
import com.egoshishang.sys.MongoImageIdAssigner;
import com.egoshishang.util.CommonUtils;
import com.egoshishang.util.MyBytes;

public class MongoUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testSaveImage()
	{
		ItemImage itemImage = new ItemImage();
		ItemImage.setIdAssigner( MongoImageIdAssigner.getInstance());
		String photoUrl = "http://ec4.images-amazon.com/images/I/51qjkPqI7LL._SS400_.jpg";
		itemImage.setImageData(CommonUtils.getInternetImage(photoUrl));
		itemImage.generateKey();
		itemImage.generateId();
		itemImage.addColumn(ItemImage.ITEM_ID, MyBytes.toBytes("B006FPZIZ6"));
		//
		MongoUtils.saveItemImage(itemImage);
	}

}
