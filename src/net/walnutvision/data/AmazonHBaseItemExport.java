package net.walnutvision.data;

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

import net.walnutvision.amazon.ItemProfileFetcher;
import net.walnutvision.amazon.ResponseParser;
import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.conf.TableConfiguration;
import net.walnutvision.orm.ItemOperator;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.sys.ImageIdAssigner;
import net.walnutvision.sys.LocalFileImageIdAssigner;
import net.walnutvision.sys.WorkQueue;
import net.walnutvision.util.CommonUtils;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.io.IOUtils;


public class AmazonHBaseItemExport extends HBaseItemExport {

	protected String asinFile = null;

	protected BufferedReader asinReader = null;
	protected ItemProfileFetcher profileFetcher = null;
	protected Map<String, byte[]> imageDownloadMap = null;
	protected LongCounter numImageToDownload = new LongCounter();
	protected WorkQueue imageDownloadQueue = null;


	public AmazonHBaseItemExport(String asinFile,
			int numDownloadThreads) {
		this.asinFile = asinFile;
		try {
			this.asinReader = new BufferedReader(new FileReader(this.asinFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		profileFetcher = new ItemProfileFetcher();
		profileFetcher.setResponseParser(ResponseParser.getAmazonResponseParser());
		imageDownloadMap = new HashMap<String, byte[]>();
		// /create image download queue
		imageDownloadQueue = new WorkQueue(numDownloadThreads);
	}

	public static class LongCounter {
		public long value = 0;
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
				ItemOperator.insertItem(itemMeta);				
				synchronized (numImageToDownload) {
					numImageToDownload.value--;
//					System.out.println("" + numImageToDownload.value);
					System.out.println(".");
					numImageToDownload.notifyAll();
				}
		}
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
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out
					.println("arguments: asinFile  numThreads");
			return;
		}
		String asinFile = args[0];
		int numThreads = Integer.valueOf(args[1]);
		HBaseInstance.getInstance().createTablePool(Integer.MAX_VALUE);
		HBaseItemExport itemExporter = new AmazonHBaseItemExport(asinFile,numThreads);
		System.out.println("start to exporting images to hbase");
		itemExporter.export();
		System.out.println("congrats! exporting done, cleanup");
	}
}
