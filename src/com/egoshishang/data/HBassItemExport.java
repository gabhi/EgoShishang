package com.egoshishang.data;

import java.io.IOException;
import java.util.List;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.sys.ImageIdAssigner;

public abstract class HBassItemExport {
	protected HTable mapTable = null;
	protected HTable contentTable = null;
	protected TableConfiguration tableConfig = null;
	protected ImageIdAssigner imageIdAssigner = null;
	
	public abstract TableConfiguration provideTableConfiguration();
	public abstract ImageIdAssigner provideImageIdAssigner();
	
	public void init()
	{
		this.imageIdAssigner = provideImageIdAssigner();
		this.tableConfig = provideTableConfiguration();

		HBaseInstance hbaseInst = HBaseInstance.getInstance();
		String mapTableName = String.valueOf(tableConfig.getParam(TableConfiguration.MAP_TABLE_KEY));
		mapTable = hbaseInst.getTable(mapTableName);
		
		String contentTableName = String.valueOf(tableConfig.getParam(TableConfiguration.CONTENT_TABLE_KEY));
		contentTable = hbaseInst.getTable(contentTableName);
	}
	public abstract List<ItemImage> generateItems();
	
	public void export() throws IOException
	{
		List<ItemImage> itemList ;
		while( (itemList = generateItems()) != null && itemList.size() > 0)
		{
			addItems(itemList);
		}
	}
	
	public void addItem(ItemImage itemImage) throws IOException{
		ImageKeyGenerate keyGen = ImageKeyGenerate.getMD5KeyGenerator();
		byte[] imageData = itemImage.getImageByte();
		byte[] imageMeta = itemImage.getItemMeta();
		byte[] itemUrl = Bytes.toBytes(itemImage.getItemUrl());

		//column family name for the content table
		byte[] contentCF = tableConfig.getParam(TableConfiguration.CONTENT_TABLE_CF);
		//three columns: image binary data, url data and meta data
		byte[] imageDataCol = tableConfig.getParam(TableConfiguration.CONTENT_IMAGE_DATA_QUALIFIER);
		byte[] itemUrlCol = tableConfig.getParam(TableConfiguration.CONTENT_ITEM_URL_QUALIFIER);
		byte[] itemMetaCol = tableConfig.getParam(TableConfiguration.CONTENT_META_DATA_QUALIFIER);
		//row key for the image, it's computed as the md5 signature of the image bytes
		///obtain an Id for current image, each image is absolute unique
		long imageId = imageIdAssigner.nextId();
		itemImage.setKey(imageId);
		byte[] contentRowKey = itemImage.getKeyByte();
		Put put = new Put(contentRowKey);
		//add the columns
		put.add(contentCF, imageDataCol, imageData);
		put.add(contentCF, itemMetaCol, imageMeta);
		put.add(contentCF, itemUrlCol, itemUrl);
		contentTable.put(put);
		
		//now update the map table, adding a mapping between image hash and the image id obtained above
		byte[] mapCF = tableConfig.getParam(TableConfiguration.CONTENT_TABLE_CF);
		//it will not be used here, just a placeholder
		byte[] idCol = tableConfig.getParam(TableConfiguration.MAP_IMAGE_ID_QUALIFIER);
		byte[] imageHashKey = keyGen.generate(imageData);
		put = new Put(imageHashKey);
		//we use the image id as the column qualifier
		put.add(mapCF, contentRowKey,Bytes.toBytes(true));
		//add the map key
		mapTable.put(put);
	}
	public void addItems(List<ItemImage> itemList) throws IOException
	{
		for(ItemImage item : itemList)
		{
			addItem(item);
		}
	}
	
}
