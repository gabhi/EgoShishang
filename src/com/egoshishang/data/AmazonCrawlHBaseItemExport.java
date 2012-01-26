package com.egoshishang.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.egoshishang.amazon.CategoryCrawl;
import com.egoshishang.amazon.ProductCrawl;
import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.data.AmazonHBaseItemExport.LongCounter;
import com.egoshishang.orm.ItemOperator;
import com.egoshishang.orm.HBaseObject.ItemImage;
import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.sys.CrawlTimestamp;
import com.egoshishang.sys.MongoImageIdAssigner;
import com.egoshishang.sys.WorkQueue;

public class AmazonCrawlHBaseItemExport extends HBaseItemExport {

	protected String categoryFile = null;
	protected BufferedReader br = null;
	CategoryCrawl cc = null;
	protected LongCounter numImageToDownload = new LongCounter();
	protected WorkQueue imageDownloadQueue = null;
	protected int numDownloadThreads = 100;

	public AmazonCrawlHBaseItemExport(String categoryFile,
			int numDownloadThreads) {
		this.numDownloaded = numDownloadThreads;
		this.categoryFile = categoryFile;
		try {
			br = new BufferedReader(new FileReader(this.categoryFile));
			imageDownloadQueue = new WorkQueue(numDownloadThreads);
			//setup the imageid assigner
			ItemImage.setIdAssigner(MongoImageIdAssigner.getInstance());
			//generate a new timestamp
			CrawlTimestamp.getInstance().generateTimestamp();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			//get meta information like price and photo url
			//no need to parse extra pages to get the photo url
//			ProductCrawl.crawlItem(itemMeta);
			try{
				if( !itemMeta.getColumnList(ItemMeta.PHOTO_URL).isEmpty())
				{
					ItemOperator.insertItem(itemMeta);
				}				
			}catch(Exception e)
			{
				System.err.println("failed to insert item");
			}
			finally
			{
			synchronized (numImageToDownload) {
				numImageToDownload.value--;
				System.out.println(".");
				numImageToDownload.notifyAll();
			}
			}
		}
	}

	@Override
	public List<ItemMeta> generateItemMeta() {
		String line = null;
		if (cc == null || !cc.hasMorePage()) {
			try {
				if ((line = br.readLine()) != null) {
					cc = new CategoryCrawl(line);
				} else {
					return null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<ItemMeta> pageMeta = cc.nextPage();
		if (pageMeta == null) {
			pageMeta = new LinkedList<ItemMeta>();
		}
		return pageMeta;
	}

	@Override
	public void export() {
		List<ItemMeta> metaList = null;
		while ((metaList = generateItemMeta()) != null) {
			// spawn a download thread
			for (ItemMeta meta : metaList) {
				//get the image information
				ImageDownloadThread downloadThread = new ImageDownloadThread(
						meta, this);
				synchronized(this.numImageToDownload)
				{
					numImageToDownload.value ++;
				}
				this.imageDownloadQueue.execute(downloadThread);
			}
			//wait until it's done
			synchronized (this.numImageToDownload) {
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

	}

	public static void main(String[] args) {
		String blc = null;
		int numThread = 100;
		if(args.length >= 2)
		{
			blc = args[0];
			numThread = Integer.valueOf(args[1]);
		}
		else
		{
			blc = "/Users/qizhao/Documents/code/Egoshishang/data/book_leaf_cid";
		}
		HBaseInstance hbaseInst = HBaseInstance.getInstance();
		hbaseInst.createTablePool(Integer.MAX_VALUE);
		AmazonCrawlHBaseItemExport ac = new AmazonCrawlHBaseItemExport(blc,numThread);
		ac.export();
	}

}
