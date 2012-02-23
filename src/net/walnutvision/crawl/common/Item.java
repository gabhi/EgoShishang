package net.walnutvision.crawl.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.walnutvision.orm.RowSerializable;
import net.walnutvision.sys.CrawlTimestamp;
import net.walnutvision.util.CommonUtils;
import net.walnutvision.util.MyBytes;

public abstract class Item extends RowSerializable{

	///item id
	public static final String ID = "id";
	///item name
	public static final String NAME = "nm";
	///item price
	public static final String PRICE = "pr";
	///currency code
	public static final String CURRENCY_CODE = "cc";
	///some popular currency code
	public static final String CNY = "cny";
	public static final String USD = "usd";
	///item url
	public static final String URL = "u";
	///merchant
	public static final String MERCHANT = "m";
	///category path consisting of multiple nodes, each take id and name
	public static final String CATEGORY_PATH = "cp";
	///where is the item sold
	public static final String LOCATION = "l";
	///item photos
	public static final String PHOTO_URL = "pu";
	///foreign image id
	public static final String FOREIGN_IMAGE_ID = "fii";
	///column index in the image object(one image can be owned by multiple items)
	public static final String FOREIGN_IMAGE_COLUMN = "fic";
	///hash value for meta information
	public static final String META_HASH = "mh";
	///hash value for photo list
	public static final String PHOTO_HASH = "ph";
	///extra information
	public static final String EXTRA = "e";

	public abstract String getMerchant();
	public abstract Item getInstance();
	public Item()
	{
		///specify the merchant 
		this.addColumn(MERCHANT, MyBytes.toBytes(this.getMerchant()));
	}
	protected void addPhoto(String photoUrl) throws IOException
	{
		//first download the image data
		byte[] imageData = CommonUtils.getInternetImage(photoUrl);
		if(imageData != null)
		{
			ItemImage image = new ItemImage();
			image.setImageData(imageData);
			//first generate the hash key
			image.generateKey();
			///generate the integer image id
			image.generateId();
			//check existence of the image
			ItemImage queryImage = new ItemImage();
			queryImage.setRowKey(image.getRowKey());
			queryImage.setImageData(imageData);
			String[] imageColumns = {ItemImage.IMAGE_ID, ItemImage.UPDATE_TIME};
			String foreignColName = null;
			//if this image already exists, 
			if(queryImage.retrieve(Arrays.asList(imageColumns)))
			{
				///add current Item
				foreignColName = queryImage.addColumn(ItemImage.ITEM_ID,this.getRowKey());
				queryImage.updateColumn(RowSerializable.synthesizeFullColumnName(ItemImage.UPDATE_TIME,0), MyBytes.toBytes(CrawlTimestamp.getInstance().getTimestamp()));
				image = queryImage;
			}
			else
			{					
				foreignColName = image.addColumn(ItemImage.ITEM_ID, this.getRowKey());
				image.addColumn(ItemImage.UPDATE_TIME, MyBytes.toBytes(CrawlTimestamp.getInstance().getTimestamp()));
				///save this image
				image.saveImage();
			}
			//save the image
			image.commitUpdate();
			image.commitDelete();

			//add photo url
			this.addColumn(Item.PHOTO_URL, MyBytes.toBytes(photoUrl));
			//now update Item
			this.addColumn(Item.FOREIGN_IMAGE_ID, image.getRowKey());
			this.addColumn(Item.FOREIGN_IMAGE_COLUMN, MyBytes.toBytes(foreignColName));
		}
	}
	
	protected void removePhoto(int photoIdx)
	{
		String photoColumn = RowSerializable.synthesizeFullColumnName(Item.PHOTO_URL, photoIdx);
		String imageIdColumn = RowSerializable.synthesizeFullColumnName(Item.FOREIGN_IMAGE_ID, photoIdx);
		String imageColColumn = RowSerializable.synthesizeFullColumnName(Item.FOREIGN_IMAGE_COLUMN, photoIdx);
		//get the image id
		ItemImage image = new ItemImage();
		image.setRowKey(this.getColumnValue(imageIdColumn));
		String[] imageFields = {ItemImage.ITEM_ID,ItemImage.UPDATE_TIME};
		image.retrieve(Arrays.asList(imageFields));
		//now delete the image
		image.removeColumn(imageColColumn);
		List<byte[]> itemList = image.getColumnList(ItemImage.ITEM_ID);
		if(itemList.isEmpty())
		{
			image.removeImage();
			image.delete();
		}
		else
		{
			long upTime = CrawlTimestamp.getInstance().getTimestamp();
			//update the updating time
			image.updateColumn(RowSerializable.synthesizeFullColumnName(ItemImage.UPDATE_TIME, 0), MyBytes.toBytes(upTime));
			image.commitUpdate();
			image.commitDelete();
		}
		//remove photo column
		this.removeColumn(photoColumn);
		//remove image id column
		this.removeColumn(imageIdColumn);
		this.removeColumn(imageColColumn);
	}
	
	private void updatePhoto(Item meta2) throws IOException
	{
		List<byte[]> photoList1 = this.getColumnList(Item.PHOTO_URL);
		List<byte[]> photoList2 = meta2.getColumnList(Item.PHOTO_URL);
		//
		Map<String, Integer> photoMap1 = new HashMap<String,Integer>();
		
		for(int i = 0; i < photoList1.size() ;i++)
		{
			byte[] strByte = photoList1.get(i);
			photoMap1.put((String)MyBytes.toObject(strByte,MyBytes.getDummyObject(String.class)), i);
		}
		
		for(byte[] strByte : photoList2)
		{
			String url = (String)MyBytes.toObject(strByte, MyBytes.getDummyObject(String.class));
			if(!photoMap1.containsKey(url))
			{
				//add a new photo url
				this.addPhoto(url);
			}
			else
			{
				//mark as exist
				photoMap1.put(url, -1);
			}
		}
		///now remove expired photos
		for(java.util.Map.Entry<String, Integer> ent : photoMap1.entrySet())
		{
			int idx = ent.getValue();
			if(idx != -1)
				this.removePhoto(idx);
		}
		
	}
	
	private void updateMeta(Item meta2)
	{
		//remove existing meta
		for(Entry<String, Integer> ent : meta2.colIndexMap.entrySet())
		{
			String field = ent.getKey();
			if(field.equals(Item.PHOTO_URL))
			{
				continue;
			}
			this.removeColumnList(field);
			List<byte[]> colValList = meta2.getColumnList(field);
			if(colValList.size() > 0)
			{
				this.addColumnList(field, colValList);
			}
		}
		
//		this.computeMetaHash();
//		String[] metaFields = {NAME, URL, PRICE, MERCHANT, CATEGORY_PATH,LOCATION, EXTRA};
//		for(String field : metaFields)
//		{
//			this.removeColumnList(field);
//		}
//		//now replace the new values
//		for(String field : metaFields)
//		{
//			List<byte[]> colValList = meta2.getColumnList(field);
//			if(colValList.size() > 0)
//				this.addColumnList(field, colValList);	
//		}
//		//
	}
	
	public void updateItem(Item item2) throws IOException
	{
		
		this.computeMetaHash();
		this.computePhotoHash();
		item2.computeMetaHash();
		item2.computePhotoHash();
		///
		byte[] metaHash1 = this.getColumnFirst(Item.META_HASH);
		byte[] metaHash2 = item2.getColumnFirst(Item.META_HASH);
		
		byte[] photoHash1 = this.getColumnFirst(Item.PHOTO_HASH);
		byte[] photoHash2 = item2.getColumnFirst(Item.PHOTO_HASH);

		if(CommonUtils.byteArrayCompare(metaHash1, metaHash2) != 0)
		{
			this.updateMeta(item2);				
		}
		if(CommonUtils.byteArrayCompare(photoHash1, photoHash2) != 0)
			this.updatePhoto(item2);
	}
	
//	@Override
//	public String toString()
//	{
//		String[] strFields = {NAME, PRICE, URL, MERCHANT, CATEGORY_PATH, LOCATION, PHOTO_URL};
//		StringBuffer sb = new StringBuffer();
//		for(String field : strFields)
//		{
//			List<byte[]> columnList = this.getColumnList(field);
//			for(int i = 0; i < columnList.size(); i++)
//			{
//				String fieldName = field +"_" + i;
//				byte[] fieldValue = columnList.get(i);
//				String strField = (String)MyBytes.toObject(fieldValue, MyBytes.getDummyObject(String.class));
//				sb.append(fieldName + ":" + strField + "\t");
//			}
//		}
//
//		return sb.toString();
//		
//	}
//		
	public  byte[] computeMetaHash()
	{
		if(this.colIndexMap.containsKey(Item.META_HASH))
		{
			return this.getColumnFirst(Item.META_HASH);
		}
		String[] fields = {NAME, URL, PRICE, CURRENCY_CODE,MERCHANT, CATEGORY_PATH, LOCATION};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(String field : fields)
		{
			
			List<byte[]> colList = this.getColumnList(field);
			for(byte[] col : colList)
			{
				try {
					baos.write(col);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(baos.toByteArray());
			this.addColumn(Item.META_HASH, hash);
			return hash;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] computePhotoHash()
	{
		if(this.colIndexMap.containsKey(Item.PHOTO_HASH))
		{
			return this.getColumnFirst(Item.PHOTO_HASH);
		}
		
		List<byte[]> photoUrlByteList = this.getColumnList(Item.PHOTO_URL);
		List<String> photoUrlList = new LinkedList<String>();
		for(byte[] urlByte : photoUrlByteList)
		{
			String url = (String)MyBytes.toObject(urlByte, MyBytes.getDummyObject(String.class));
			photoUrlList.add(url);
		}
		Collections.sort(photoUrlList);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(String url : photoUrlList)
		{
			try {
				baos.write(MyBytes.toBytes(url));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//compute the url(s) hash
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(baos.toByteArray());
			this.addColumn(Item.PHOTO_HASH, hash);
			return hash;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * generate the row key for amazon item. 
	 * the row key is simply a combination of merchant and product asin number
	 * @return
	 */
	public byte[] generateKey()
	{
		byte[] asinBytes = this.getColumnFirst(ID);
		if(asinBytes != null)
		{
			String asinString = MyBytes.toObject(asinBytes, new String());
			String combinedKey = getMerchant() + "_" + asinString;
			this.rowKey =  MyBytes.toBytes(combinedKey);
			return this.rowKey;
		}
		return null;
	}
	
	@Override
	public String getTableName() {
		return "item";
	}

}
