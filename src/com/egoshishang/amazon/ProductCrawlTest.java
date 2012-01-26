package com.egoshishang.amazon;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.util.WebFile;

public class ProductCrawlTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParsePage() {
		String pageUrl = "http://www.amazon.cn/gp/search?ie=UTF8&rh=n%3A658496051&page=9&rd=1";
		try {
			WebFile wf = new WebFile(pageUrl);
			String content = (String)wf.getContent();
			System.out.println(content);
		
			List<ItemMeta> metaList = ProductCrawl.parsePage(content);
			for(ItemMeta meta : metaList)
			{
				System.out.println(meta);
			}
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

}
