package net.walnutvision.crawl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import net.walnutvision.util.WebFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AmazonCategoryCrawlTest {

	AmazonCategoryCrawl acc = new AmazonCategoryCrawl();
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCrawl() {
//		acc.Crawl();
	}
	@Test
	public void testDownloadPage()
	{
		String url = "http://www.amazon.cn/%E4%B8%80%E4%B8%AA%E5%B8%9D%E5%9B%BD%E7%9A%84%E7%94%9F%E4%B8%8E%E6%AD%BB-%E6%B0%B4%E6%B5%92-%E9%87%8C%E4%B8%8D%E4%B8%BA%E4%BA%BA%E7%9F%A5%E7%9A%84%E5%8E%86%E5%8F%B2%E5%AF%86%E7%A0%81-%E5%A4%9C%E7%8B%BC%E5%95%B8%E8%A5%BF%E9%A3%8E/dp/B006GX8KY8/ref=sr_1_1?s=books&ie=UTF8&qid=1329196803&sr=1-1";
		try {
			WebFile wf = new WebFile(url);
			String content = (String)wf.getContent();
			System.out.println(content);
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
