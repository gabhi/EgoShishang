package com.egoshishang.amazon;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.egoshishang.orm.HBaseObject.ItemImage;
import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.util.CommonUtils;
import com.egoshishang.util.MyBytes;
import com.egoshishang.util.WebFile;

public class ProductCrawl {

	static String prodImageCellPat = "<td id=\"prodImageCell\".*?>.*?<a href.*?><img.*?src=\"(.*?)\"";
	static String largeImagePat = "<span id=\"prodImageCaption\".*?<a href=\"(.*?)\"";
	
	public static String checkLargeImageUrl(String content)
	{
		Pattern pat = Pattern.compile(largeImagePat);
		Matcher mat = pat.matcher(content);
		while(mat.find())
		{
			return mat.group(1);
		}
		return null;
	}
	public static String extractLargeImageUrl(String largeImageUrl)
	{
		WebFile wf = null;
		String extractedUrl = null;
		try {
			wf = new WebFile(largeImageUrl);
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
		finally
		{
			if( wf != null &wf.getContent() !=null)
			{
				if(wf.getContent() instanceof String)
				{
					String content = (String)wf.getContent();
					String patStr = "<div id=\"imageViewerDiv\".*?<img src=\"(.*?)\"";
					Pattern pat = Pattern.compile(patStr);
					Matcher mat = pat.matcher(content);
					while(mat.find())
					{
						extractedUrl = mat.group(1);
						break;
					}
				}
			}
		}
		return extractedUrl;
	}
	
	public static String extractProductImageUrl(String content)
	{
		Pattern pat = Pattern.compile(prodImageCellPat);
		Matcher mat = pat.matcher(content);
		String matchedUrl = null;
		while(mat.find())
		{
			matchedUrl = mat.group(1);
			break;
		}
		return matchedUrl;
	}
	
	public static byte[] downloadImage(String imageUrl)
	{
		WebFile wf = null;
		byte[] imageBytes = null;
		try {
			 wf = new WebFile(imageUrl);
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
		finally
		{
			if( wf != null && wf.getContent() != null)
			{
					//save the image file
					imageBytes = (byte[])wf.getContent();
			}
		}
		return imageBytes;
	}
	public static void parsePrice(ItemMeta item, String content)
	{
		
		String listPricePat = "<span\\s+id=\"listPriceValue\"\\s+class=\"listprice\">.*?([\\d\\.]+)</span>";
		String actualPricePat = "<span\\s+id=\"actualPriceValue\">.*?<b class=\"priceLarge\">.*?([\\d\\.]+)</b>";
		Pattern pat = Pattern.compile(listPricePat);
		Matcher mat = pat.matcher(content);
		if(mat.find())
		{
			String listPriceStr = mat.group(1);
			float listPrice = Float.valueOf(listPriceStr);
			item.addColumn(ItemMeta.LIST_PRICE, MyBytes.toBytes(listPrice));
		}
		pat = Pattern.compile(actualPricePat);
		mat = pat.matcher(content);
		if(mat.find())
		{
			String actualPriceStr = mat.group(1);
			float actualPrice = Float.valueOf(actualPriceStr);
			item.addColumn(ItemMeta.LOWEST_NEW_PRICE, MyBytes.toBytes(actualPrice));
		}
		//
	}
	
	public static List<ItemImage> downloadItemImage(ItemImage itemImage, ItemMeta meta)
	{
		List<ItemImage> imageList = new LinkedList<ItemImage>();
		meta.generateKey();
		List<byte[]> photoUrlList = meta.getColumnList(ItemMeta.PHOTO_URL);
		for(byte[] urlBytes : photoUrlList)
		{
			String photoUrl = (String)MyBytes.toObject(urlBytes, MyBytes.getDummyObject(String.class));
			byte[] imageData = CommonUtils.getInternetImage(photoUrl);
			if(imageData != null)
			{
				ItemImage tmpImage = new ItemImage();
				tmpImage.addColumn(ItemImage.IMAGE_DATA, imageData);
				tmpImage.generateKey();
				String colName = tmpImage.addColumn(ItemImage.ITEM_ID, meta.getRowKey());
				//also update meta
				
				meta.addColumn(ItemMeta.PHOTO_KEY, tmpImage.getRowKey());
				meta.addColumn(ItemMeta.FOREIGN_PHOTO_COL, MyBytes.toBytes(colName));
			}
		}
		return imageList;
	}
	
	public  static void crawlItem(ItemMeta item)
	{
		String asinUrl = (String)MyBytes.toObject(item.getColumnFirst(ItemMeta.URL),MyBytes.getDummyObject(String.class));
		try {
			WebFile wf = new WebFile(asinUrl);
			if(wf.getMIMEType().equals("text/html"))
			{
				//parse the file to get the photo and price information
				if(wf.getContent() instanceof String)
				{
					String content = (String)wf.getContent();
					//get the large photo url
					String largeImageUrl = checkLargeImageUrl(content);
					String imageUrl = null;
					if( largeImageUrl != null)
					{
						 imageUrl = extractLargeImageUrl(largeImageUrl);
					}
					else
					{
						imageUrl = extractProductImageUrl(content);
					}
					if(imageUrl != null)
					{
						//insert the link
						item.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(imageUrl));
						//download the image
					}
					//now parse the price information
					parsePrice(item,content);
				}
				
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
	
	public static String getFileName(String fullPath)
	{
		Pattern pat = Pattern.compile("/([^/]*)$");
		Matcher mat = pat.matcher(fullPath);
		String fileName = null;
		if(mat.find())
		{
			fileName = mat.group(1);
		}
		return fileName;
	}
	
	public static boolean saveImage(byte[] imageBytes, String imagePath)
	{
		boolean result = false;
		try {
			FileOutputStream fos = new FileOutputStream(imagePath);
			ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
			MyBytes.streamCopy(bais, fos, 10240);
			fos.close();
			result = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;	
	}

	public static List<ItemMeta> extractPageItemId(String content)
	{
		List<ItemMeta> metaList = new LinkedList<ItemMeta>();
		if(content != null)
		{
			String productPat = "<div class=\"productTitle\"><a href=\"([^\"]+?)\">(.*?)</a>";
			String asinPat = "/dp/([\\d\\w]+)";
			Pattern pat = Pattern.compile(productPat);
			Matcher mat = pat.matcher(content);
			while(mat.find())
			{
				String itemUrl = mat.group(1);
				String title = mat.group(2);
				Pattern pat1 = Pattern.compile(asinPat);
				Matcher mat1 = pat1.matcher(itemUrl);
				String asin = "";
				while(mat1.find())
				{
					asin = mat1.group(1);
					 break;
				}
//				System.out.println(title + "\t" + asin + "\t" + itemUrl);
				ItemMeta tmpMeta = new ItemMeta();
				tmpMeta.addColumn(ItemMeta.CURRENCY_CODE, MyBytes.toBytes(ItemMeta.CNY));
				tmpMeta.addColumn(ItemMeta.MERCHANT, MyBytes.toBytes(ItemMeta.AMAZON));
				if(!asin.equals(""))
				{
					tmpMeta.addColumn(ItemMeta.ASIN, MyBytes.toBytes(asin));
				}
				tmpMeta.addColumn(ItemMeta.URL, MyBytes.toBytes(itemUrl));
				tmpMeta.addColumn(ItemMeta.TITLE, MyBytes.toBytes(title));
				//parse each item page
				metaList.add(tmpMeta);
			}
		}
		return metaList;
	}

	public static int getItemPageCnt(String content)
	{
		String numPagePat = "class=\"resultCount\">[^\\d\\,\\-]+?[\\d\\-]+[^\\d\\-\\,]+([\\d\\,]+)[^\\d\\,]+?<\\/div>";
		Pattern pat = Pattern.compile(numPagePat);
		Matcher mat = pat.matcher(content);
		int numPage = 0;
		if(mat.find())
		{
			String numStr = mat.group(1);
			numStr = numStr.replace(",", "");
			numPage = Integer.valueOf(numStr);
			numPage = (int)(numPage/12 + 1);
			if(numPage > 400)
			{
				numPage = 400;
			}
		}
		return numPage;
	}
}
