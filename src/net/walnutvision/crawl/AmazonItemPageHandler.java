package net.walnutvision.crawl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.protocol.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.walnutvision.crawl.common.Item;
import net.walnutvision.nutch.PageHandler;
import net.walnutvision.orm.RowSerializable;
import net.walnutvision.util.MyBytes;

public class AmazonItemPageHandler extends PageHandler {

	/**
	 * get the url for small versioned image
	 * @param asin
	 * @return
	 */
	public static String assemberSmallImageUrl(String asin)
	{
		String url = "http://images.amazon.com/images/P/" + asin + ".01._AA175_PU_PU-5_.jpg";
		return url;
	}
	
	/**
	 * get the url for the larged version image
	 * @param asin
	 * @return
	 */
	public static String assembleAmazonImageUrl(String asin)
	{
		String url = "http://images.amazon.com/images/P/" + asin + ".01._SCLZZZZZZZ_PU_PU-5_.jpg";
		return url;
	}
	
	
	protected static String getPrice(String priceText)
	{
		Pattern pat = Pattern.compile("([\\d\\.\\,]+)");
		Matcher mat = pat.matcher(priceText);
		if(mat.find())
		{
			String price = mat.group(1);
			///replace , with null string
			price = price.replaceAll(",", "");
			return price;
		}
		return null;
	}
	
	protected void parsePrice(Document doc, Item item)
	{
		Elements prodTab = doc.select("table.product");
//		<td class="priceBlockLabel">市场价:</td>
//		<td><span id="listPriceValue"  class="listprice">￥ 25.00</span></td>
//		</tr>
		if(!prodTab.isEmpty())
		{
			String listPriceText = prodTab.select("tr>td>span#listPriceValue").get(0).text();
			String listPrice = getPrice(listPriceText);
			String actualPriceText = prodTab.select("tr>td>span#actualPriceValue").get(0).text();
			String actualPrice = getPrice(actualPriceText);
//			System.out.println("lsit price:" + listPrice + " actual price:" + actualPrice);
			///set currency code
			item.addColumn(Item.CURRENCY_CODE, MyBytes.toBytes(Item.CNY));
			///add price information
			item.addColumn(Item.PRICE, MyBytes.toBytes(actualPrice));
			item.addColumn(AmazonItem.LIST_PRICE, MyBytes.toBytes(listPrice));
			item.addColumn(AmazonItem.ACTUAL_PRICE, MyBytes.toBytes(actualPrice));
		}
	}
	/**
	 * @param doc html page represented as DOM
	 */
	protected void parseTitle(Document doc,Item item)
	{
		///extract product title
		String titleText = doc.select("noscript>ul.bxgyInfoBlockNonJS>li>span#bxgy_x_titleNonJS").get(0).text();
		Pattern pat = Pattern.compile("[^<>]+");
		Matcher mat = pat.matcher(titleText);
		if(mat.find())
		{
			String title = mat.group();
			item.addColumn(Item.NAME, MyBytes.toBytes(title));
		}
	}
	/**
	 * get the product title by analyzing the product url
	 * an example url would be : http://www.amazon.cn/%E6%A2%A6%E5%9B%9E%E5%A4%A7%E6%B8%85-%E9%87%91%E5%AD%90/dp/B001146VLQ
	 * 
	 * @throws UnsupportedEncodingException
	 */
	protected void parseAsin(Item item, String url) throws UnsupportedEncodingException
	{
		Pattern pat = Pattern.compile("([^/]+)/dp/([\\w\\d]+)");
		Matcher mat = pat.matcher(url);
		if(mat.find())
		{
			String title = mat.group(1);
			title = URLDecoder.decode(title,"UTF-8");
			String asin = mat.group(2);
			///add title
			item.addColumn(Item.NAME, MyBytes.toBytes(title));
			///also get the photo url
			String photoUrl = AmazonItemPageHandler.assembleAmazonImageUrl(asin);
			item.addColumn(Item.PHOTO_URL, MyBytes.toBytes(photoUrl));
			///add asin as ID
			item.addColumn(Item.ID, MyBytes.toBytes(asin));
		}
	}
	/**
	 * extract properties about the product
	 * @param doc
	 */
	protected void parseProperty(Document doc, Item item)
	{
		///
		 Elements basicInfoElements = doc.select("td.bucket>div.content>ul>li");
		 for(Element propertyElement : basicInfoElements)
		 {
//			 ///split property name and id
			 String propertyName = propertyElement.select("b").text();
			 propertyName = propertyName.replaceAll(":", "");
			 String propertyValue = propertyElement.ownText();
//			 System.out.println(propertyName + "<=>" + propertyValue);
			 if(propertyValue != null && !propertyValue.equals(""))
			 {
				 ///attach this property to current item
				 item.addColumn(propertyName, MyBytes.toBytes(propertyValue));
			 }
		 }
	}
	/**
	 * get the category path(s) of the product by analyzing the salesrank field
	 * @param doc html page represented as DOM
	 */
	protected void parseCategoryPath(Document doc, Item item)
	{
		Element salseRankNode = doc.select("li#SalesRank").get(0);
		Elements pathNodes = salseRankNode.select("ul.zg_hrsr>li.zg_hrsr_item");
		
		for(Element pathNode : pathNodes)
		{
			Elements ladderNodes = pathNode.select("span.zg_hrsr_ladder");
			String categoryPath = "";
			for(Element ladder : ladderNodes)
			{
				Elements hrefNodes = ladder.children();
				String tmpCategory;
				for(Element hrefNode : hrefNodes)
				{
					if(hrefNode.tagName().equals("a"))
					{
						tmpCategory  = hrefNode.text();
					}
					else
					{
						tmpCategory = hrefNode.select("a").get(0).text();
					}
					categoryPath += (categoryPath.equals("")? "" : "|");
					categoryPath += tmpCategory;
				}
			}
//			System.out.println(categoryPath);
			///add category path to item
			item.addColumn(Item.CATEGORY_PATH, MyBytes.toBytes(categoryPath));
		}
	}
	/**
	 * parse the raw html page to extract product information
	 */
	protected void parsePage(String url, Content content1, Item item)
	{
		
		///represent content as string
		String content = new String(content1.getContent());
		Document doc = Jsoup.parse(content);
		///parse price information
		this.parsePrice(doc, item);
		try {
			///parse the product url to get the ASIN number and product title
			parseAsin(item, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		///parse other information, asin, publisher, etc
		this.parseProperty(doc,item);
		///parse category path information
		this.parseCategoryPath(doc,item);
		
	}
	
	
	@Override
	public Object process(String url, CrawlDatum datum, Content content) {
		// TODO Auto-generated method stub
		AmazonItem item = new AmazonItem();
		parsePage(url,content,item);
		try {
			ItemOperation.insertItem(item);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return item;
	}

}
