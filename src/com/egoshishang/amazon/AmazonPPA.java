package com.egoshishang.amazon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AmazonPPA {
	private static final String AWS_ACCESS_KEY_ID = "AKIAJHZD7FOWMGMYDHPQ";
	private static final String AWS_SECRET_KEY = "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";
	private static final String ENDPOINT = "webservices.amazon.cn";
	private static SignedRequestsHelper helper = null;
	static {
		try {
			helper = SignedRequestsHelper.getInstance(ENDPOINT,
					AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SignedRequestsHelper getRequesetHelper() {
		return helper;
	}

	public static void setCommonParams(Map<String, String> params) {
		params.put("Service", "AWSECommerceService");
		params.put("Version", "2011-08-01");
		params.put("AssociateTag", "books09f-23");
	}

	public static Map<String, String> setBrowseNodeParams(
			Map<String, String> params, long nodeId, String responseGroup) {
		params.put("BrowseNodeId", String.valueOf(nodeId));
		params.put("Operation", "BrowseNodeLookup");
		params.put("ResponseGroup", responseGroup);
		return params;
	}
	
	public static Map<String,String> setSearchByBrowseNode(Map<String,String> params, long browseNodeId)
	{
		params.put("Operation", "ItemSearch");
		params.put("BrowseNode", String.valueOf(browseNodeId));
		params.put("SearchIndex", "Books");
		params.put("ItemPage",String.valueOf(1));
		return params;
	}

	public static Document retrieveDocument(SignedRequestsHelper helper,
			Map<String, String> params) throws InterruptedException {
		String requestUrl = helper.sign(params);
		System.out.println(requestUrl);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		int retries = 10;
		while (retries > 0) {
			try {
				db = dbf.newDocumentBuilder();
				URL connect = new URL(requestUrl);
				InputSource is = new InputSource(new BufferedReader(
						new InputStreamReader(connect.openStream(), "utf-8")));
				long startTime = System.currentTimeMillis();
				Document doc = db.parse(is);
				long endTime = System.currentTimeMillis();
				if(endTime - startTime < 100)
				{
					Thread.sleep(150 - endTime + startTime);					
				}
				return doc;
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				retries--;
				System.out.println("error");
				Thread.sleep(1000);
				// e.printStackTrace();
			}
		}
		return null;
	}
}
