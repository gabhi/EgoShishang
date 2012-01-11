package com.egoshishang.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.sys.ImageIdAssigner;
import com.egoshishang.util.DataUtility;

public abstract class HBaseItemExport {
	protected TableConfiguration tableConfig = null;
	protected ImageIdAssigner imageIdAssigner = null;
	protected String downloadCounterFile = null;
	protected boolean localMode = false;
	protected RandomAccessFile downloadCounterWriter = null;
	protected String metaLocalFile = null;
	protected String imageLocalDir = null;
	protected BufferedWriter metaWriter = null;
	protected long numDownloaded = 0;
	public String getMetaLocalFile() {
		return metaLocalFile;
	}

	public void setMetaLocalFile(String metaLocalFile) {
		this.localMode = true;
		this.metaLocalFile = metaLocalFile;
	}

	public String getImageLocalDir() {
		return imageLocalDir;
	}

	public void setImageLocalDir(String imageLocalDir) {
		this.localMode = true;
		this.metaLocalFile = imageLocalDir + "/item_meta";
		this.imageLocalDir = imageLocalDir;
	}

	
	public abstract TableConfiguration provideTableConfiguration();
	public abstract ImageIdAssigner provideImageIdAssigner();
	
	protected HTable getContentTable()
	{
		String contentTableName = Bytes.toString(tableConfig.getParam(TableConfiguration.CONTENT_TABLE_KEY));		
		return (HTable)HBaseInstance.getInstance().getTableFromPool(contentTableName);
	}

	protected HTable getMapTable()
	{
		String mapTableName = Bytes.toString(tableConfig.getParam(TableConfiguration.MAP_TABLE_KEY));		
		return (HTable)HBaseInstance.getInstance().getTableFromPool(mapTableName);
	}

	public boolean init()
	{
		this.imageIdAssigner = provideImageIdAssigner();
		this.tableConfig = provideTableConfiguration();
		
		if(localMode)
		{
			//create a local meta file writer
			try {
				this.metaWriter = new BufferedWriter(new FileWriter(this.metaLocalFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			downloadCounterWriter = new RandomAccessFile(new File(this.downloadCounterFile),"rw");
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public abstract List<ItemMeta> generateItemMeta();
	
	public abstract void export();	
	public  void cleanUp()
	{
		try {
			this.downloadCounterWriter.close();
			if(localMode)
			{
				this.metaWriter.close();
			}
			else
			{
				String contentTableName = Bytes.toString(tableConfig.getParam(TableConfiguration.CONTENT_TABLE_KEY));		
				HBaseInstance.getInstance().getTablePool().closeTablePool(contentTableName);
				String mapTableName = Bytes.toString(tableConfig.getParam(TableConfiguration.MAP_TABLE_KEY));
				HBaseInstance.getInstance().getTablePool().closeTablePool(mapTableName);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setDownloadCounterFile(String fileName)
	{
		this.downloadCounterFile = fileName;
	}
	
	public void addItem(ItemImage itemImage)
	{
		if(localMode)
		{
			addItemLocal(itemImage);
		}
		else
		{
			addItemHBase(itemImage);
		}
	}
	protected void addItemHBase(ItemImage itemImage){
		ImageKeyGenerate keyGen = ImageKeyGenerate.getMD5KeyGenerator();
		byte[] imageData = itemImage.getImageByte();
		//image download fail, just skip this image
		if(imageData == null || imageData.length == 0)
		{
			System.err.println("failed to download the image:" + itemImage.itemUrl);
			return;
		}
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
		long t1 = System.currentTimeMillis();
		Put put = new Put(contentRowKey);
		//add the columns
		put.add(contentCF, imageDataCol, imageData);
		put.add(contentCF, itemMetaCol, imageMeta);
		put.add(contentCF, itemUrlCol, itemUrl);
		try {
			HTable contentTable = getContentTable();
			contentTable.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//now update the map table, adding a mapping between image hash and the image id obtained above
		byte[] mapCF = tableConfig.getParam(TableConfiguration.CONTENT_TABLE_CF);
		//it will not be used here, just a placeholder
		byte[] idCol = tableConfig.getParam(TableConfiguration.MAP_IMAGE_ID_QUALIFIER);
		byte[] imageHashKey = keyGen.generate(imageData);
		put = new Put(imageHashKey);
		//we use the image id as the column qualifier
		put.add(mapCF, contentRowKey,Bytes.toBytes(true));
		//add the map key
		try {
			HTable mapTable = getMapTable();
			mapTable.put(put);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void addItemLocal(ItemImage item) {
		ItemMeta meta = (ItemMeta) DataUtility.byteArrayToObject(item.itemMeta);
		// get the image binary data
		try {
			//write the meta information to the file
			synchronized(metaWriter)
			{
				this.metaWriter.write(meta.toString() + "\n");				
			}
			FileOutputStream ofs = new FileOutputStream(this.imageLocalDir + "/"
					+ meta.asin + ".jpg");
			ByteArrayInputStream bis = new ByteArrayInputStream(item.imageByte);
			// copy to the file
//			IOUtils.copyBytes(bis, ofs, 10240);
			DataUtility.streamCopy(bis, ofs, 10240);
			ofs.close();
			bis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void addItems(List<ItemImage> itemList) throws IOException
	{
		for(ItemImage item : itemList)
		{
			addItem(item);
			synchronized(this)
			{
				this.downloadCounterWriter.seek(0);
				this.downloadCounterWriter.write(String.valueOf(this.numDownloaded).getBytes());
			}
		}
	}
	
}
