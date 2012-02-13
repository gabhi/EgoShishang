package net.walnutvision.amazon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ResponseParserTest {
	protected ResponseParser rp = null;
	@Before
	public void setUp(){
		rp = new ResponseParser.AmazonResponseParser();
	}
	
	@Test
	public void testPriceParse()
	{
		String priceStr = "¥ 29.80";
		System.out.println(ResponseParser.AmazonResponseParser.getItemPrice(priceStr));
	}
	@After
	public void tearDown()
	{
		
	}

}
