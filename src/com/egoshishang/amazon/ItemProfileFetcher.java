package com.egoshishang.amazon;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.hadoop.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import com.egoshishang.data.ItemMeta;

public class ItemProfileFetcher {
	protected SignedRequestsHelper helper = null;
	protected Map<String,String> params = null;
	
	public ItemProfileFetcher()
	{
		helper = AmazonPPA.getRequesetHelper();
		params = new HashMap<String,String>();
	}
	
	protected List<ItemMeta> parseResponse(Document doc)
	{
		List<ItemMeta> itemList = new LinkedList<ItemMeta>();
		NodeList itemNodes = doc.getElementsByTagName("Item");
		for(int i = 0; i < itemNodes.getLength(); i++)
		{
			Element itemNode = (Element)itemNodes.item(i);
			//get the title of item, price, detailed url, large photo url
			ItemMeta tmpMeta = new ItemMeta();
			tmpMeta.asin = itemNode.getElementsByTagName("ASIN").item(0).getTextContent();
			tmpMeta.url = itemNode.getElementsByTagName("DetailPageURL").item(0).getTextContent();
			Element imageNode = (Element)itemNode.getElementsByTagName("LargeImage").item(0);
			tmpMeta.photoUrl = imageNode.getElementsByTagName("URL").item(0).getTextContent();
			Element attrNode = (Element)itemNode.getElementsByTagName("ItemAttributes").item(0);
			tmpMeta.title = attrNode.getElementsByTagName("Title").item(0).getTextContent();
			tmpMeta.extra = attrNode.getTextContent();
			itemList.add(tmpMeta);
		}
		return itemList;
	}
	public List<ItemMeta> batchProductQuery(String[] asinList)
	{
		AmazonPPA.setCommonParams(params);
		AmazonPPA.setItemLookup(params, asinList);
		List<ItemMeta> metaList = new LinkedList<ItemMeta>();
		try {
			Document response = AmazonPPA.retrieveDocument(helper, params);
			if(response == null)
			{
				return metaList;
			}
			metaList = parseResponse(response);
//			for(ItemMeta item : metaList)
//			{
//				System.out.println(item);
//			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metaList;
		
	}
	public static void main(String[] args)
	{
		ItemProfileFetcher pc = new ItemProfileFetcher();
		List<String> asinList = new LinkedList<String>();
		String[] asinArray = {
		"B005ZB6P6M",
		"B006DUVZPA",
		"B002AVJJ0O",
		"B005O64RTA",
		"B0034G4VQG",
		"B006FPZIZ6",
		"B005D68CLU",
		"B0011C7AWW",
		"B0011BY7M4",
		"B0016KFXGO"
		};
		pc.batchProductQuery(asinArray);
	}

}
