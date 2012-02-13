package net.walnutvision.amazon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.orm.ItemOperator;
import net.walnutvision.orm.HBaseObject.ItemMeta;

import org.w3c.dom.Document;


public class ItemProfileFetcher {
	protected SignedRequestsHelper helper = null;
	protected Map<String,String> params = null;
	protected ResponseParser responseParser = null;
	
	public ItemProfileFetcher()
	{
		helper = AmazonPPA.getRequesetHelper();
		params = new HashMap<String,String>();
	}
	public void setResponseParser(ResponseParser rp)
	{
		this.responseParser = rp;
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
			metaList = this.responseParser.extractMeta(response);
//			for(ItemMeta item : metaList)
//			{
//				System.out.println(item);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metaList;
		
	}
	public static void main(String[] args)
	{
		

		String asinFile = "/Users/qizhao/Documents/code/Egoshishang/data/1m_asin_jn";
		
		ItemProfileFetcher pc = new ItemProfileFetcher();
		pc.setResponseParser(ResponseParser.getAmazonResponseParser());
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
		HBaseInstance instance = HBaseInstance.getInstance();
		instance.createTablePool(Integer.MAX_VALUE);
		
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(asinFile));
			while((line = br.readLine()) != null)
			{
				String[] asins = line.split("\t");
				List<ItemMeta> metaList = pc.batchProductQuery(asins);
				for(ItemMeta meta : metaList)
				{
//					ItemOperator.insertItem(meta);
					System.out.println(meta.toString() + "\n");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		List<ItemMeta> metaList = pc.batchProductQuery(asinArray);
//		String idFile = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data/image_id";
//		if(args.length > 0)
//		{
//			idFile = args[0];
//		}
		
	}

}
