package net.walnutvision.amazon;

import static org.junit.Assert.*;

import java.util.List;

import javax.swing.Timer;

import net.walnutvision.data.AmazonHBaseItemExport;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.sys.WorkQueue;
import net.walnutvision.util.CommonUtils;
import net.walnutvision.util.MyBytes;
import net.walnutvision.util.SimpleTimer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class CategoryCrawlTest {

	protected CategoryCrawl cc = null;
	protected WorkQueue wq = null;
	protected AmazonHBaseItemExport.LongCounter counter = null;
	@Before
	public void setUp() throws Exception {
		final String categoryId = "664842051";
		cc = new CategoryCrawl(categoryId);
		wq = new WorkQueue(12);
		counter = new AmazonHBaseItemExport.LongCounter();
	}

	protected class DownloadThread implements Runnable
	{
		protected ItemMeta meta;
		public DownloadThread(ItemMeta meta)
		{
			this.meta = meta;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//download the image
			String photoUrl = (String)MyBytes.toObject(meta.getColumnFirst(ItemMeta.PHOTO_URL),MyBytes.getDummyObject(String.class));
			System.out.println("downloading: " + photoUrl);
			if(photoUrl != null)
			{
				//download the image
				byte[] imageByte = CommonUtils.getInternetImage(photoUrl);
				synchronized(counter)
				{
					counter.value--;
					counter.notifyAll();
				}
			}
		}
		
	}
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextPage() {
		while(cc.hasMorePage())
		{
			SimpleTimer t = new SimpleTimer();
			t.start();
			List<ItemMeta> metaList = cc.nextPage();
			t.end();
			System.out.println("get meta list takes: " + t.elapse() + " ms");
			t.start();
			for(ItemMeta meta : metaList)
			{
				synchronized(this.counter)
				{
					counter.value++;
				}
				wq.execute(new DownloadThread(meta));
			}
			synchronized(this.counter)
			{
				while(counter.value > 0)
					try {
						counter.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			t.end();
			System.out.println("downding images take: " + t.elapse() + " ms");
		}
	}

}
