package net.walnutvision.crawl.common;

import net.walnutvision.orm.RowSerializable;
import net.walnutvision.util.MyBytes;

/**
 * maintain all properties for certain category
 * @author qizhao
 *
 */
public class CategoryProperty extends RowSerializable{
	
	public void addProperty(String propertyName)
	{
		long propertyId = PropertyId.addPropertyWithCheck(propertyName);
		this.addColumn(propertyName, MyBytes.toBytes(true));
	}
	
	@Override
	public String getTableName() {
		return "category_property";
	}

}
