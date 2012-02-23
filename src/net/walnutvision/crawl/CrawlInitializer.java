package net.walnutvision.crawl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import net.walnutvision.conf.GlobalConfiguration;
import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.crawl.common.Item;
import net.walnutvision.crawl.common.ItemImage;
import net.walnutvision.nutch.Initializer;
import net.walnutvision.sys.HBaseImageIdAssigner;
import net.walnutvision.sys.ImageIdAssigner;

public class CrawlInitializer extends Initializer {

	protected static final String TABLE_POOL_SIZE_PARAM = "table.pool.size";
	protected static final String NULL_STRING = "";
	@Override
	public void initialize() {
		///setup the global configuration object
		Configuration conf = HBaseConfiguration.create(this.getConf());
		GlobalConfiguration.CONFIG = conf;
		///initializer HBaseInstance
		int tablePoolSize = Integer.valueOf(this.getConf().get(TABLE_POOL_SIZE_PARAM, "10"));
		///specify the configuration object explicitly 
		HBaseInstance.getInstance().init(conf);
		HBaseInstance.getInstance().createTablePool(tablePoolSize);
		///initialize the hbase based image id assigner
		ImageIdAssigner idAssigner = HBaseImageIdAssigner.getInstance();
		idAssigner.setUp(NULL_STRING);
		///create tables for item and image if not existing
		try {
			Item item = new AmazonItem();
			String itemTableName = item.getTableName();
			HBaseInstance.getInstance().createTableIfNotExist(itemTableName);
			ItemImage image = new ItemImage();
			String imageTableName = image.getTableName();
			HBaseInstance.getInstance().createTableIfNotExist(imageTableName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		Initializer ci = new CrawlInitializer();
		ci.setConf(new Configuration());
		ci.initialize();
	}

}
