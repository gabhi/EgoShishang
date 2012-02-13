package net.walnutvision.orm;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.List;

import net.walnutvision.amazon.ProductCrawl;
import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.orm.HBaseObject.ItemImage;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.sys.MongoImageIdAssigner;
import net.walnutvision.util.WebFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ItemOperatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInsertItem() {
		//first create a list of ItemMeta object
		String itemPage = "http://www.amazon.cn/b/ref=amb_link_29066492_8?ie=UTF8&node=658495051&pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=left-1&pf_rd_r=1XWJ0M9PVSRBMW5FAENV&pf_rd_t=101&pf_rd_p=62930612&pf_rd_i=658393051";
		try {
			WebFile wf = new WebFile(itemPage);
			String content = (String)wf.getContent();
			List<ItemMeta> metaList = ProductCrawl.parsePage(content);
			//save the meta to hbase
			for(ItemMeta meta : metaList)
			{
//				System.out.println(meta);
				ItemOperator.insertItem(meta);				
			}
			//
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args)
	{
		HBaseInstance.getInstance().createTablePool(Integer.MAX_VALUE);
		ItemImage.setIdAssigner(MongoImageIdAssigner.getInstance());
		ItemOperatorTest iot = new ItemOperatorTest();
		iot.testInsertItem();
	}

}
