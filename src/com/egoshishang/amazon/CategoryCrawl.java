package com.egoshishang.amazon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.egoshishang.mongodb.CategoryPageTracker;
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
	
	protected String firstPage()
	{
		return baseUrl + "1";		
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
	
	protected static CategoryPageTracker getPageTracker()
	{
		return CategoryPageTracker.getInstance();
	}
	
	public List<ItemMeta> nextPage()
	{
		WebFile wf;
		List<ItemMeta> metaList = null;
		try {
			String curPageUrl = getPageTracker().getPageUrl(this.categoryId);
			this.curPage = getPageTracker().getPageIndex(this.categoryId);
			this.numPage = getPageTracker().getMaxPage(this.categoryId);
//			System.out.println("current page and max page:" + curPage + " " + numPage);
			if(this.curPage > this.numPage)
				return new LinkedList<ItemMeta>();
			if(curPageUrl == null)
			{
				curPageUrl = firstPage();
			}
			
			wf = new WebFile(curPageUrl);
			String content = (String) wf.getContent();

			//update num of pages for this category
			if(content != null)
			{
				if(curPageUrl.equals(firstPage()))
				{
					numPage = ProductCrawl.getItemPageCnt(content);
					getPageTracker().setMaxPage(this.categoryId, numPage);
					numPageInited = true;										
				}
				nextPageUrl = this.nextPageLink(content);
				//update next page url
				getPageTracker().setNextPageUrl(this.categoryId, nextPageUrl);
//				metaList = ProductCrawl.extractPageItemId(content);
//				System.out.println(content);
				metaList = ProductCrawl.parsePage(content);
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
