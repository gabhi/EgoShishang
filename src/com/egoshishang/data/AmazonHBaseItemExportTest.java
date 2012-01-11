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
	String asinFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/10000_asin_jn";
	String imageIdFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_id";
	String counterFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_counter";
	@Before
	public void setUp()
	{
		amazonExport = new AmazonHBaseItemExport(asinFile,imageIdFile,10);
	}
	@Test
	public void testBatchQuery()
	{
		String imageRoot = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data";
		amazonExport.setImageLocalDir(imageRoot);
		amazonExport.setMetaLocalFile(imageRoot + "/item_meta");
		amazonExport.setDownloadCounterFile(counterFile);
		amazonExport.init();
		amazonExport.export();
	}
	
}
