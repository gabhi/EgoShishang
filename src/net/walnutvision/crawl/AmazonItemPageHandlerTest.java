package net.walnutvision.crawl;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import net.walnutvision.nutch.Initializer;
import net.walnutvision.nutch.PageHandler;
import net.walnutvision.orm.RowSerializable;
import net.walnutvision.util.WebFile;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.protocol.Content;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AmazonItemPageHandlerTest {

	protected AmazonItemPageHandler inst = new AmazonItemPageHandler();
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

//	@Test
	public void testDownload()
	{
		String url = "http://www.amazon.cn/b/ref=amb_link_29108512_2?ie=UTF8&node=659340051&pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=left-1&pf_rd_r=1R04SFG88CJE982ZFSYC&pf_rd_t=101&pf_rd_p=62649252&pf_rd_i=658498051";
		try {
			WebFile wf = new WebFile(url);
			System.out.println((String)wf.getContent());
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
	@Test
	public void testProcess() {
//		String url = "http://www.amazon.cn/%E6%A2%A6%E5%9B%9E%E5%A4%A7%E6%B8%85-%E9%87%91%E5%AD%90/dp/B001146VLQ";
		String url = "http://www.amazon.cn/Apple-%E8%8B%B9%E6%9E%9C-iPhone-4S-3G%E6%99%BA%E8%83%BD%E6%89%8B%E6%9C%BA/dp/B0063CCZAM/ref=sr_1_1?m=A1AJ19PSB66TGU&s=wireless&ie=UTF8&qid=1329721312&sr=1-1";
		Initializer ci = new CrawlInitializer();
		ci.setConf(new Configuration());
		ci.initialize();
		try {
			WebFile wf = new WebFile(url);
			Content content = new Content();
			String contentString = (String)wf.getContent();
			content.setContent(contentString.getBytes());
			content.setContentType("text/html");
			content.setMetadata(null);
			RowSerializable item = (RowSerializable)inst.process(url,null,content);
			System.out.println(item);
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
		AmazonItemPageHandlerTest testCase = new AmazonItemPageHandlerTest();
		testCase.testProcess();
	}

}
