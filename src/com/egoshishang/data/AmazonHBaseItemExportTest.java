package com.egoshishang.data;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.egoshishang.util.DataUtility;


public class AmazonHBaseItemExportTest {

	protected AmazonHBaseItemExport amazonExport = null;
	String asinFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/asin_file";
	String imageIdFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_id";
	
	@Before
	public void setUp()
	{
		amazonExport = new AmazonHBaseItemExport(asinFile,imageIdFile,10);
	}
	@Test
	public void testBatchQuery()
	{
		String imageRoot = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data";
		List<ItemImage> itemList = amazonExport.generateItems();
		for(ItemImage item : itemList)
		{
			ItemMeta meta = (ItemMeta)DataUtility.byteArrayToObject(item.itemMeta);
			System.out.println(meta);
			//get the image binary data
			try {
				FileOutputStream ofs = new FileOutputStream(imageRoot + "/" + meta.asin + ".jpg");
				ByteArrayInputStream bis = new ByteArrayInputStream(item.imageByte);
				//copy to the file
				IOUtils.copyBytes(bis, ofs, 10240);
				ofs.close();
				bis.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
}
