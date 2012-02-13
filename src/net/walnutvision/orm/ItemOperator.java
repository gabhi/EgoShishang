package net.walnutvision.orm;


import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.sys.CrawlTimestamp;
import net.walnutvision.util.MyBytes;


public class ItemOperator {
	
	public static void insertItem(ItemMeta meta)
	{
		meta.generateKey();
		//first check the existence of meta
		ItemMeta queryMeta = new ItemMeta();
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
		meta.commitUpdate();
		meta.commitDelete();
	}
}
