package net.walnutvision.data;

import org.junit.Before;
import org.junit.Test;

public class AmazonHBaseItemExportTest {

	protected AmazonHBaseItemExport amazonExport = null;
	String asinFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/10000_asin_jn";
	String imageIdFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_id";
	String counterFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_counter";
	@Before
	public void setUp()
	{
		amazonExport = new AmazonHBaseItemExport(asinFile,10);
	}
	@Test
	public void testBatchQuery()
	{
		String imageRoot = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data";
		amazonExport.export();
	}
}
