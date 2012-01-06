package com.egoshishang.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Put;

import com.egoshishang.amazon.ItemProfileFetcher;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.sys.ImageIdAssigner;
import com.egoshishang.sys.LocalFileImageIdAssigner;
import com.egoshishang.sys.WorkQueue;
import com.egoshishang.util.DataUtility;

public class AmazonHBaseItemExport extends HBassItemExport {

	protected String asinFile = null;
	protected String imageIdFile = null;
	
	protected BufferedReader asinReader = null;
	protected ItemProfileFetcher profileFetcher = null;
	protected Map<String, byte[]> imageDownloadMap = null;
	protected LongCounter numImageDownload = new LongCounter();
	protected WorkQueue imageDownloadQueue = null;
	
	public AmazonHBaseItemExport(String asinFile, String imageIdFile, int numDownloadThreads)
	{
		this.asinFile = asinFile;
		this.imageIdFile = imageIdFile;
		profileFetcher = new ItemProfileFetcher();
		imageDownloadMap = new HashMap<String,byte[]>();
		///create image download queue
		imageDownloadQueue = new WorkQueue(numDownloadThreads);
		init();
	}
	
	@Override
	public void init()
	{
		super.init();
		this.imageIdAssigner.init(this.imageIdFile);
		try {
			asinReader = new BufferedReader(new FileReader(asinFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public List<ItemImage> generateItems() {
		List<ItemImage> itemList = new LinkedList<ItemImage>();
		String line = null;
		try {
			if( (line = asinReader.readLine()) != null)
			{
				String[] asins = line.split("\t");
				List<ItemMeta> itemMetaList = profileFetcher.batchProductQuery(asins);
				//now grab the images for each item
				this.numImageDownload.value = itemMetaList.size();
				this.imageDownloadMap.clear();
				for(int i = 0; i < itemMetaList.size(); i++)
				{
					ItemMeta im = itemMetaList.get(i);
					imageDownloadQueue.execute(new DownloadThread(im.asin, im.photoUrl));
				}
				synchronized(this.numImageDownload)
				{
					while(numImageDownload.value > 0)
						numImageDownload.wait();
				}
				//now combine the meta data and image binary data
				for(ItemMeta itemMeta : itemMetaList)
				{
					byte[] itemImageByte = imageDownloadMap.get(itemMeta.asin);
					ItemImage itemImage = new ItemImage();
					itemImage.setImageByte(itemImageByte);
					itemImage.setItemMeta(DataUtility.objectToByteArray(itemMeta));
					itemImage.setItemUrl(itemMeta.url);
					itemList.add(itemImage);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return itemList;
	}

	@Override
	public TableConfiguration provideTableConfiguration() {
		// TODO Auto-generated method stub
		return TableConfiguration.getHardcodeConfiguration();
	}

	@Override
	public ImageIdAssigner provideImageIdAssigner() {
		// TODO Auto-generated method stub
		return  LocalFileImageIdAssigner.getInstance();
	}
	
	@Override
	public void finalize()
	{
		if(asinReader != null)
		{
			try {
				asinReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static class LongCounter
	{
		public long value;
	}
	
	private class DownloadThread implements Runnable
	{
		private String asin;
		private String imageUrl;
		
		public DownloadThread(String asin, String imageUrl)
		{
			this.asin = asin;
			this.imageUrl = imageUrl;
		}
		
		@Override
		public void run()
		{
			//download the image from internet and get its raw bytes
			byte[] imageBytes = DataUtility.getInternetImage(imageUrl);
			System.out.println("download image from : " + imageUrl);
			synchronized(numImageDownload)
			{
				imageDownloadMap.put(asin, imageBytes);
				numImageDownload.value--;
				numImageDownload.notify();
			}
		}
	}
}
