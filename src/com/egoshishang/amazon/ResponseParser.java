package com.egoshishang.amazon;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.util.MyBytes;


public abstract class ResponseParser {
	public static float INVALID_PRICE  = -1;
	public  abstract List<ItemMeta> extractMeta(Document doc);
	protected static ResponseParser amazonParser = new AmazonResponseParser();
	
	//
	public static ResponseParser getAmazonResponseParser()
	{
		return amazonParser;
	}
	
	protected static class AmazonResponseParser extends ResponseParser
	{
		protected static Pattern pricePattern = Pattern.compile("([\\d\\.\\,]+)");	

		public static String getNodeAttrAssert(Element node, String nodeName)
		{
			NodeList nl = node.getElementsByTagName(nodeName);
			String attrVal = "";
			if(nl != null)
			{
				attrVal = nl.item(0).getTextContent();
			}
			
			return attrVal;
		}
		
		public static Element getChildNodeAssert(Element node, String nodeName)
		{
			NodeList childNode = node.getElementsByTagName(nodeName);
			if(childNode != null)
			{
				return (Element)childNode.item(0);
			}
			else
			{
				return null;
			}
		}
		
		public static float getItemPrice(String priceStr)
		{
			Matcher mat = pricePattern.matcher(priceStr);
			if(mat.find())
			{
				String numStr = mat.group();
				numStr = numStr.replaceAll(",", "");
				return Float.valueOf(numStr);
			}
			return INVALID_PRICE;
		}
		
		public NodeList getItemNodes(Document doc)
		{
			return doc.getElementsByTagName("Item");
		}

		public String getItemAsin(Element itemNode)
		{
			return getNodeAttrAssert(itemNode,"ASIN");
		}
		public String getItemUrl(Element itemNode)
		{
			return getNodeAttrAssert(itemNode,"DetailPageURL");
		}
		public Element getItemAttributeNode(Element itemNode)
		{
			return getChildNodeAssert(itemNode,"ItemAttributes");
		}
		public String getItemTitle(Element attrNode)
		{
			return getNodeAttrAssert(attrNode,"Title");
		}
		public float getItemListPrice(Element attrNode)
		{
			//get the listed price
			Element listPriceNode = getChildNodeAssert(attrNode, "ListPrice");
			if(listPriceNode != null)
			{
				String priceStr = getNodeAttrAssert(listPriceNode, "FormattedPrice");
				return getItemPrice(priceStr);
			}
			return INVALID_PRICE;
		}
		public void setItemCondition(ItemMeta meta, Element itemNode)
		{
			Element osNode = getChildNodeAssert(itemNode, "OfferSummary");
			if(osNode != null)
			{
			
				int totalNew = Integer.valueOf(AmazonResponseParser.getNodeAttrAssert(osNode, "TotalNew"));
				int totalRefurbished = Integer.valueOf(AmazonResponseParser.getNodeAttrAssert(osNode, "TotalRefurbished"));
				int totalUsed = Integer.valueOf(AmazonResponseParser.getNodeAttrAssert(osNode,"TotalUsed"));
				int totalCollectible = Integer.valueOf(AmazonResponseParser.getNodeAttrAssert(osNode,"TotalCollectible"));
				meta.addColumn(ItemMeta.TOTAL_NEW, MyBytes.toBytes(totalNew));
				meta.addColumn(ItemMeta.TOTAL_REFURBISHED, MyBytes.toBytes(totalRefurbished));
				meta.addColumn(ItemMeta.TOTAL_USED, MyBytes.toBytes(totalUsed));
				meta.addColumn(ItemMeta.TOTAL_COLLECTIBLE, MyBytes.toBytes(totalCollectible));
				//get the price for each condition
				Element node ;
				node = AmazonResponseParser.getChildNodeAssert(osNode, "LowestNewPrice");
				//specify as amazon currency code
				meta.addColumn(ItemMeta.CURRENCY_CODE, MyBytes.toBytes(ItemMeta.CNY));
				if(node != null)
				{
					String priceStr = getNodeAttrAssert(node, "FormattedPrice");
					String curCode = getNodeAttrAssert(node, "CurrencyCode");
					String currencyCode = curCode;
					float lowestNewPrice = AmazonResponseParser.getItemPrice(priceStr);
					meta.addColumn(ItemMeta.LOWEST_NEW_PRICE, MyBytes.toBytes(lowestNewPrice));
				}
				
				node = AmazonResponseParser.getChildNodeAssert(osNode, "LowestRefurbishPrice");
				if(node != null)
				{
					String priceStr = getNodeAttrAssert(node, "FormattedPrice");
					String curCode = getNodeAttrAssert(node, "CurrencyCode");
					float lowestRefurbishedPrice = AmazonResponseParser.getItemPrice(priceStr);
					meta.addColumn(ItemMeta.LOWEST_REFURBISHED_PRICE, MyBytes.toBytes(lowestRefurbishedPrice));
				}

				node = AmazonResponseParser.getChildNodeAssert(osNode, "LowestUsedPrice");
				if(node != null)
				{
					String priceStr = getNodeAttrAssert(node, "FormattedPrice");
					String curCode = getNodeAttrAssert(node, "CurrencyCode");
					float lowestUsedPrice = AmazonResponseParser.getItemPrice(priceStr);
					meta.addColumn(ItemMeta.LOWEST_USED_PRICE, MyBytes.toBytes(lowestUsedPrice));					
				}
				
				node = AmazonResponseParser.getChildNodeAssert(osNode, "LowestCollectiblePrice");
				if(node != null)
				{
					String priceStr = getNodeAttrAssert(node, "FormattedPrice");
					String curCode = getNodeAttrAssert(node, "CurrencyCode");
					float lowestCollectiblePrice = AmazonResponseParser.getItemPrice(priceStr);
					meta.addColumn(ItemMeta.LOWEST_COLLECTIBLE_PRICE, MyBytes.toBytes(lowestCollectiblePrice));
				}
			}
		}
		
		public String getItemPhoto(Element itemNode)
		{
			Element imageNode = getChildNodeAssert(itemNode, "LargeImage");
			if(imageNode == null)
			{
				return "";
			}
			return getNodeAttrAssert(imageNode,"URL");
		}
		
		@Override
		public List<ItemMeta> extractMeta(Document doc) {
			List<ItemMeta> itemList = new LinkedList<ItemMeta>();
			//parse the document
			NodeList itemNodes = doc.getElementsByTagName("Item");
			if(itemNodes == null)
				return itemList;
			
			for(int i = 0; i < itemNodes.getLength(); i++)
			{
				Element itemNode = (Element)itemNodes.item(i);
				//get the title of item, price, detailed url, large photo url
				ItemMeta tmpMeta = new ItemMeta();
				tmpMeta.addColumn(ItemMeta.MERCHANT, MyBytes.toBytes(ItemMeta.AMAZON));
				String asin = this.getItemAsin(itemNode);
				tmpMeta.addColumn(ItemMeta.ASIN, MyBytes.toBytes(asin));
				String url = this.getItemUrl(itemNode);
				tmpMeta.addColumn(ItemMeta.URL, MyBytes.toBytes(url));
				String photoUrl = this.getItemPhoto(itemNode);
				tmpMeta.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(photoUrl));
				Element attrNode = this.getItemAttributeNode(itemNode);
				if(attrNode != null)
				{
					String title = this.getItemTitle(attrNode);
					///get the extra information
					byte[] extra = attrNode.getTextContent().getBytes();
					///
					float listPrice = this.getItemListPrice(attrNode);
					tmpMeta.addColumn(ItemMeta.TITLE, MyBytes.toBytes(title));
					tmpMeta.addColumn(ItemMeta.EXTRA, extra);
					tmpMeta.addColumn(ItemMeta.LIST_PRICE, MyBytes.toBytes(listPrice));
				}
				this.setItemCondition(tmpMeta, itemNode);
				itemList.add(tmpMeta);
			}

			return itemList;
		}
		
	}
}
