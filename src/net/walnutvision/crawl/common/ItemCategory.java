package net.walnutvision.crawl.common;

import net.walnutvision.orm.RowSerializable;

public class ItemCategory extends RowSerializable {

	///node name
	public static final String NODE_NAME = "nm";
	///node child list
	public static final String NODE_CHILD = "cl";
	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "ItemCategory";
	}
}
