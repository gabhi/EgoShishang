package com.egoshishang.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.io.IOUtils;

import com.egoshishang.amazon.ItemProfileFetcher;
import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.sys.ImageIdAssigner;
import com.egoshishang.sys.LocalFileImageIdAssigner;
import com.egoshishang.sys.WorkQueue;
import com.egoshishang.util.DataUtility;

public class AmazonHBaseItemExport extends HBaseItemExport {

	protected String asinFile = null;
	protected String imageIdFile = null;

	protected BufferedReader asinReader = null;
	protected ItemProfileFetcher profileFetcher = null;
	protected Map<String, byte[]> imageDownloadMap = null;
	protected LongCounter numImageToDownload = new LongCounter();
	protected WorkQueue imageDownloadQueue = null;


	public AmazonHBaseItemExport(String asinFile, String imageIdFile,
			int numDownloadThreads) {
		this.asinFile = asinFile;
		this.imageIdFile = imageIdFile;
		profileFetcher = new ItemProfileFetcher();
		imageDownloadMap = new HashMap<String, byte[]>();
		// /create image download queue
		imageDownloadQueue = new WorkQueue(numDownloadThreads);
	}

	@Override
	public boolean init() {
		if (super.init()) {
			this.imageIdAssigner.setUp(this.imageIdFile);
			try {
				asinReader = new BufferedReader(new FileReader(asinFile));
				return true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public TableConfiguration provideTableConfiguration() {
		// TODO Auto-generated method stub
		return TableConfiguration.getHardcodeConfiguration();
	}

	@Override
	public ImageIdAssigner provideImageIdAssigner() {
		// TODO Auto-generated method stub
		return LocalFileImageIdAssigner.getInstance();
	}

	public static class LongCounter {
		public long value;
	}

	private class ImageDownloadThread implements Runnable {
		private ItemMeta itemMeta = null;
		HBaseItemExport itemExport = null;

		public ImageDownloadThread(ItemMeta itemMeta, HBaseItemExport itemExport) {
			this.itemMeta = itemMeta;
			this.itemExport = itemExport;
		}

		@Override
		public void run() {

			// download the image from internet and get its raw bytes
			String imageUrl = itemMeta.photoUrl;
			String asin = itemMeta.asin;
			byte[] imageBytes = DataUtility.getInternetImage(imageUrl);
			// System.out.println("download image from : " + imageUrl);
			imageDownloadMap.put(asin, imageBytes);
			// insert current image and item meta information to the database
			ItemImage itemImage = new ItemImage();
			itemImage.imageByte = imageBytes;
			itemImage.itemUrl = itemMeta.url;
			itemImage.itemMeta = DataUtility.objectToByteArray(itemMeta);
			try {
				if (imageBytes != null && imageBytes.length > 0)
					itemExport.addItem(itemImage);
			} finally {
				synchronized (numImageToDownload) {
					numImageToDownload.value--;
					System.out.println("# of images to download:" + numImageToDownload.value);
					numImageToDownload.notifyAll();
				}
			}

		}
	}



	@Override
	public void cleanUp() {
		// stop the work queue
		super.cleanUp();
		this.imageDownloadQueue.shutDown();
	}

	@Override
	public List<ItemMeta> generateItemMeta() {
		List<ItemMeta> itemMetaList = null;
		String line = null;
		try {
			if ((line = asinReader.readLine()) != null) {
				String[] asins = line.split("\t");
				itemMetaList = profileFetcher.batchProductQuery(asins);
				if (itemMetaList.size() == 0) {
					System.err.println("failed to grab item list: " + line);
					return itemMetaList;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return itemMetaList;
	}

	@Override
	public void export() {
		// export item using a the downloading thread pool
		List<ItemMeta> metaList = null;
		numImageToDownload.value = 0;
		while ((metaList = this.generateItemMeta()) != null) {
			// spawn a download thread
			for (ItemMeta meta : metaList) {
				ImageDownloadThread downloadThread = new ImageDownloadThread(
						meta, this);
				synchronized (this.numImageToDownload) {
					numImageToDownload.value++;
					while (numImageToDownload.value > 10000) {
						try {
							numImageToDownload.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				this.imageDownloadQueue.execute(downloadThread);
			}
		}
		synchronized (numImageToDownload) {
			while (numImageToDownload.value > 0) {
				try {
					numImageToDownload.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args1) {
		
		String dataRoot = "/Users/qizhao/Workspaces/MyEclipse 9/Egoshishang/data1";
		String[] args = { dataRoot + "/asin_file", dataRoot + "/image_id" ,
				dataRoot + "/image_counter", dataRoot + "/image_download", "10"};
		if(args1.length > 0)
		{
			args = args1;
		}
		if (args.length != 4 && args.length != 5) {
			System.out
					.println("arguments: asinFile imageIdFile downloadCounterFile [local download path] numThreads");
			return;
		}

		String asinFile = args[0];
		String imageIdFile = args[1];
		String downloadCounterFile = args[2];
		int numThreads;
		String localImageDir = null;
		
		if(args.length == 5)
		{
			localImageDir = args[3];
			numThreads = Integer.valueOf(args[4]);
		}
		else
		{
			numThreads = Integer.valueOf(args[3]);
			HBaseInstance.getInstance().createTablePool(100);
		}
		
		HBaseItemExport itemExporter = new AmazonHBaseItemExport(asinFile,
				imageIdFile, numThreads);
		itemExporter.setDownloadCounterFile(downloadCounterFile);

		if(localImageDir != null)
		{
			//local mode enabled
			itemExporter.setImageLocalDir(localImageDir);
		}
		
		if (itemExporter.init() == false) {
			System.err.println("failed to initialize the exporter");
			return;
		}

		System.out.println("start to exporting images to hbase");
		itemExporter.export();
		System.out.println("congrats! exporting done, cleanup");
		itemExporter.cleanUp();
	}
}
