package net.walnutvision.crawl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AmazonPageHandlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testCrawlPage() {
		String url = "http://www.amazon.cn/s/ref=sr_ex_n_1?rh=n%3A658390051&bbn=658390051&ie=UTF8&qid=1328440131";
		AmazonPageHandler aph = new AmazonPageHandler();
		aph.crawlPage(url);
	}

}
