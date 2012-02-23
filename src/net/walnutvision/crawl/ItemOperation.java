package net.walnutvision.crawl;


import java.io.IOException;

import net.walnutvision.crawl.common.Item;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.orm.RowSerializable;
import net.walnutvision.sys.CrawlTimestamp;
import net.walnutvision.util.MyBytes;


public class ItemOperation {
	
	public static void insertItem(Item meta) throws IOException
	{
		if(meta.getRowKey() == null || meta.getRowKey().length == 0)
		{
			meta.generateKey();
		}

		//first check the existence of meta
		Item queryMeta = meta.getInstance();
		queryMeta.setRowKey(meta.getRowKey());
		boolean exist = queryMeta.retrieveFromHBase();
		CrawlTimestamp ct = CrawlTimestamp.getInstance();
		queryMeta.updateItem(meta);
		
		if(exist)
		{
			meta.updateColumn(RowSerializable.synthesizeFullColumnName(RowSerializable.UPDATE_TIME, 0),
					MyBytes.toBytes(ct.getTimestamp()));
			meta = queryMeta;
		}
		else
		{
			meta = queryMeta;
			meta.addColumn(RowSerializable.UPDATE_TIME,
					MyBytes.toBytes(ct.getTimestamp()));		
		}
		System.out.println(meta);
		meta.commitUpdate();

	}
}
