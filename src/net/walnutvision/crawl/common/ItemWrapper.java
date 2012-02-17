package net.walnutvision.crawl.common;

import net.walnutvision.orm.RowSerializable;

/**
 * 
 * @author qizhao
 *
 * 
 */
public abstract class ItemWrapper extends Item {
	///keeps a reference of object mapped to hbase row
	protected RowSerializable rawItem = null;

	public RowSerializable getRawItem() {
		return rawItem;
	}

	public void setRawItem(RowSerializable rawItem) {
		this.rawItem = rawItem;
	}
}
