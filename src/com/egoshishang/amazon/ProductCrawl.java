package com.egoshishang.amazon;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductCrawl {
	public static class ResultParse
	{
		protected String pageUrl = null;
		protected String pageContent = null;
		public String getPageUrl() {
			return pageUrl;
		}
		
		public void setPageUrl(String pageUrl) {
			this.pageUrl = pageUrl;
		}
		
		public ResultParse(String pageUrl)
		{
			this.pageUrl = pageUrl;
		}
		
		public void grabPage()
		{
			//first grab the page
			URL resultUrl;
			try {
				resultUrl = new URL(pageUrl);
				BufferedReader in = new BufferedReader(new InputStreamReader(resultUrl.openStream()));
				String inputLine;
				StringBuffer sb = new StringBuffer();
				while( (inputLine = in.readLine()) != null)
				{
					sb.append(inputLine+"\n");
				}
				pageContent = sb.toString();
//				System.out.println(pageContent);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public LinkedList<HashMap<String, String>> getTitle()
		{
			LinkedList<HashMap<String, String>> bookTitles = new LinkedList<HashMap<String,String>>();
			//regular expressions to extract the titles
			if(pageContent != null)
			{
				Pattern pat = Pattern.compile("<div class=\"productTitle\"><a href=\"([^\"]+?)\".*?>(.*?)</a>");
				Matcher match = pat.matcher(pageContent);
				int i = 0;
				while(match.find())
				{
					String productUrl = match.group(1);
					String productTitle = match.group(2);
					System.out.println(i + "\t" + productUrl+"\t"+productTitle);
					i++;
				}
			}
			return bookTitles;
		}
	}
	public static void main(String[] args)
	{
		String url = "http://www.amazon.cn/s/ref=sr_ex_n_1?rh=n%3A658390051&bbn=658390051&ie=UTF8&qid=1325578989#/ref=sr_pg_1?rh=n%3A658390051&bbn=658390051&ie=UTF8&qid=1325579043";
//		String url = "http://www.google.com";
		ResultParse rp = new ResultParse(url);
		rp.grabPage();
		rp.getTitle();
	}

}
