package com.egoshishang.amazon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.util.WebFile;

public class CategoryCrawl {
	protected String categoryId = null;
	protected int numPage = 1;
	protected int curPage = 1;
	protected boolean numPageInited = false;
	protected String baseUrl = null;
	protected String nextPageUrl = null;
	public CategoryCrawl(String categoryId)
	{
		this.categoryId = categoryId;
		this.baseUrl = "http://www.amazon.cn/s?ie=UTF8&rh=n%3A" + categoryId + "&page=";
		nextPageUrl = baseUrl + "1";
	}
	
	protected String nextPageLink(String content)
	{
		String nextLinkUrl = null;
		String nextLinkPat = "<span\\s+class=\"pagnNext\">.*?<a.*?href=\"(.*?)\"";
		Pattern pat = Pattern.compile(nextLinkPat);
		Matcher mat = pat.matcher(content);
		if(mat.find())
		{
			nextLinkUrl = mat.group(1);
		}
		return nextLinkUrl;
	}
	
	public List<ItemMeta> nextPage()
	{
		WebFile wf;
		List<ItemMeta> metaList = null;
		try {
			wf = new WebFile(nextPageUrl);
			String content = (String) wf.getContent();
			if(!numPageInited)
			{
				if(content != null)
				{
					numPage = ProductCrawl.getItemPageCnt(content);
					numPageInited = true;					
				}
			}
			if(content != null)
			{
				nextPageUrl = this.nextPageLink(content);
				metaList = ProductCrawl.extractPageItemId(content);
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
		curPage++;
//		System.out.println("page:" + curPage + "\t" + "total:" + numPage);
		return metaList;
	}
	
	public boolean hasMorePage()
	{
		return curPage <= numPage;
	}
	public static void main(String[] args)
	{
		String categoryId = "658393051";
		CategoryCrawl cc = new CategoryCrawl(categoryId);
		while(cc.hasMorePage())
		{
			List<ItemMeta> metaList = cc.nextPage();		
			for(ItemMeta meta : metaList)
			{
				System.out.println(meta);
			}
		}
	}
}
