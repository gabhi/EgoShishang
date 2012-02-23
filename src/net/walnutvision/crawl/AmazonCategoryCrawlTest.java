package net.walnutvision.crawl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

//	@Test
	public void testCrawl() {
		acc.Crawl();
	}
	@Test
	public void testCategoryPattern()
	{
		String patStr = "http://www\\.amazon\\.cn/s\\?ie=UTF8&rh=n%3A\\d+&page=1";
		String url = "http://www.amazon.cn/s?ie=UTF8&rh=n%3A658390051&page=1";
		Pattern pat = Pattern.compile(patStr);
		Matcher mat = pat.matcher(url);
		if(mat.find())
		{
			System.out.println(mat.group());
		}
	}
//	@Test
	public void testDownloadPage()
	{
		String url = "http://www.amazon.cn/s/ref=sr_ex_n_1?rh=n%3A658390051%2Cn%3A!658391051%2Cn%3A658400051%2Cn%3A658619051&bbn=658619051&ie=UTF8&qid=1329681736";
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
