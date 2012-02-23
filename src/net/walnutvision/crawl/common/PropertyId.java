package net.walnutvision.crawl.common;

import net.walnutvision.orm.RowSerializable;
import net.walnutvision.util.MyBytes;

/**
 * maintain a mapping between property name and identity
 * @author qizhao
 *
 */
public class PropertyId extends RowSerializable {
	///property id as an integer
	public static final String PROPERTY_ID = "pi";
	///tracking how many properties met so far
	public static final String MAX_PROPERTY_ID_ROW = "maximum_property_id";
	public static final byte[] MAX_PROPERTY_ID_ROW_BYTE = MyBytes.toBytes(MAX_PROPERTY_ID_ROW);
	
	public static long getMaxPropertyId()
	{
		long id = 0;
		PropertyId maxIdRow = new PropertyId();
		maxIdRow.setRowKey(MAX_PROPERTY_ID_ROW_BYTE);
		boolean propertyExist = maxIdRow.retrieveFromHBase();
		if(!propertyExist)
		{
			byte[] idByte = MyBytes.toBytes(id);
			maxIdRow.addColumn(PROPERTY_ID, idByte);
			///save this property to hbase
			maxIdRow.commitUpdate();
		}
		else
		{
			///retrieve the property id
			byte[] idByte = maxIdRow.getColumnFirst(PROPERTY_ID);
			id = MyBytes.toObject(idByte, (long)0);
		}
		return id;
	}
	
	/**
	 * insert a property to the property<->id map table
	 * 
	 * @param propertyName name of the property which is extracted from product detail page
	 * @return
	 */
	public static long addPropertyWithCheck(String propertyName)
	{
		long id = 0;
		byte[] rowKey = MyBytes.toBytes(propertyName);
		PropertyId property = new PropertyId();
		property.setRowKey(rowKey);
		boolean propertyExist = property.retrieveFromHBase();
		if(!propertyExist)
		{
			///if this property not exist, add it
			long maxId = getMaxPropertyId();
			id = maxId;
			byte[] idBytes = MyBytes.toBytes(maxId);
			property.addColumn(PROPERTY_ID, idBytes);
			property.commitUpdate();
			///also update the maxim category id
			PropertyId maxIdRow = new PropertyId();
			maxId ++;
			///specify the max id row key
			maxIdRow.setRowKey(MAX_PROPERTY_ID_ROW_BYTE);
			maxIdRow.addColumn(PROPERTY_ID, MyBytes.toBytes(maxId));
			maxIdRow.commitUpdate();
		}
		else
		{
			id = MyBytes.toObject(property.getColumnFirst(PROPERTY_ID), (long)0);
		}
		///return property id
		return id;
	}
	
	@Override
	public String getTableName() {
		return "property_id";
	}
}
