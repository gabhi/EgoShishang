package net.walnutvision.amazon;

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

import net.walnutvision.orm.HBaseObject.ItemImage;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.util.CommonUtils;
import net.walnutvision.util.MyBytes;
import net.walnutvision.util.WebFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


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
			if( wf != null && wf.getContent() !=null)
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
	

	public static void parsePrice(ItemMeta item, String content)
	{
		
		String listPricePat = "<span\\s+id=\"listPriceValue\"\\s+class=\"listprice\">.*?([\\d\\,\\.]+)</span>";
		String actualPricePat = "<span\\s+id=\"actualPriceValue\">.*?<b class=\"priceLarge\">.*?([\\d\\,\\.]+)</b>";
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
	/**
	 * 
	 * @param imageBytes
	 * @param imagePath
	 * @return
	 */
	
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

	/**
	 * extract ASIN from the product url
	 * 
	 * @param href url href for the product
	 * @return ASIN string for the product
	 */
	protected static String extractAsin(String href)
	{
		String asinPat = "/dp/([\\d\\w]+)";
		Pattern pat= Pattern.compile(asinPat);
		Matcher mat = pat.matcher(href);
		String asin = null;
		if(mat.find())
		{
			asin = mat.group(1);
		}
		return asin;
	}
	
	/**
	 * extract the price number from a raw string in the form like ¥3.14
	 * @param priceString price input string
	 * @return a float number, eg. 3.14
	 */
	protected static String parsePrice(String priceString)
	{
		String patString = "([\\d\\.]+)";
		Pattern pat = Pattern.compile(patString);
		Matcher mat = pat.matcher(priceString);
		String priceStr = null;
		if(mat.find())
		{
			priceStr = mat.group(1);
		}
		return priceStr;
	}
	public static String assemberSmallImageUrl(String asin)
	{
		String url = "http://images.amazon.com/images/P/" + asin + ".01._AA175_PU_PU-5_.jpg";
		return url;
	}
	
	public static String assembleAmazonImageUrl(String asin)
	{
		String url = "http://images.amazon.com/images/P/" + asin + ".01._SCLZZZZZZZ_PU_PU-5_.jpg";
		return url;
	}
	
	/**
	 * @brief extract title, price(list and new), asin information for products listed in current page
	 * 
	 * @param content raw web page content, which will be parsed 
	 * @return a list of items with meta information 
	 */
	public static List<ItemMeta> parsePage(String content)
	{
		Document html = Jsoup.parse(content);
//		final String resultListId = "atfResults";
//		Element resultList = html.getElementById(resultListId);
		final int maxItemId = 11;
		List<ItemMeta> metaList = new LinkedList<ItemMeta>();
		for(int i = 0; i <= maxItemId; i++)
		{
			ItemMeta tmpMeta = new ItemMeta();
			Element result = html.getElementById("result_" + i);
			try{
			if(result != null)
			{
				//get the data div
				Element data = result.getElementsByClass("productData").get(0);
				Element title = data.getElementsByClass("productTitle").get(0);
				Element titleUrl = title.select("a").get(0);
				String href = titleUrl.attr("href");
				//extract asin from href
				String asin = extractAsin(href);
				//get the product title
				String titleText = titleUrl.text();
				//set up the item
				tmpMeta.addColumn(ItemMeta.CURRENCY_CODE, MyBytes.toBytes(ItemMeta.CNY));
				tmpMeta.addColumn(ItemMeta.MERCHANT, MyBytes.toBytes(ItemMeta.AMAZON));
				if(asin != null)
				{
					tmpMeta.addColumn(ItemMeta.ASIN, MyBytes.toBytes(asin));
				}
				tmpMeta.addColumn(ItemMeta.URL, MyBytes.toBytes(href));
				tmpMeta.addColumn(ItemMeta.TITLE, MyBytes.toBytes(titleText));
				//get the price information
				Element priceNode = data.getElementsByClass("newPrice").get(0);
				Elements strikeNode = priceNode.select("strike");
				String listPriceText = null;
				if(!strikeNode.isEmpty())
				{
					 listPriceText = strikeNode.get(0).text();
				}
				Elements newPriceNode = priceNode.select("span");
				String newPriceText = "-1";
				if(!newPriceNode.isEmpty())
				{
					newPriceText = priceNode.select("span").get(0).text();
				}
				String newPrice = parsePrice(newPriceText);
				if(listPriceText == null)
					listPriceText = newPriceText;
				String listPrice = parsePrice(listPriceText);
				tmpMeta.addColumn(ItemMeta.LIST_PRICE, MyBytes.toBytes(listPrice));
				tmpMeta.addColumn(ItemMeta.LOWEST_NEW_PRICE, MyBytes.toBytes(newPrice));
				String photoUrl = assembleAmazonImageUrl(asin);
				tmpMeta.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(photoUrl));
				metaList.add(tmpMeta);
			}
			}catch(Exception e)
			{
				e.printStackTrace();
				System.err.println("ItemError:" + MyBytes.toObject(tmpMeta.getColumnFirst(ItemMeta.ASIN),MyBytes.getDummyObject(String.class)));
			}
		}
		return metaList;
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
