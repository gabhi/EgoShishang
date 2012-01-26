package com.egoshishang.orm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore.Entry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.util.StringUtils;

import com.egoshishang.data.ImageKeyGenerate;
import com.egoshishang.mongodb.MongoUtils;
import com.egoshishang.sys.CrawlTimestamp;
import com.egoshishang.sys.ImageIdAssigner;
import com.egoshishang.util.CommonUtils;
import com.egoshishang.util.MyBytes;

public class HBaseObject {
	
	public  static class ItemImage extends RowSerializable
	{
	///column name for item id(list)
	public static final String ITEM_ID = "ii";
	public static final String ADD_IMAGE = "ai";
	///image id, a long type, globally unique and never re-use
	public static final String IMAGE_ID = "id";
	protected byte[] imageData = null;
	protected String imageFileName = null;
	protected static ImageIdAssigner idAssigner = null;
	///compute the image hash key
	public  byte[] generateKey()
	{
		ImageKeyGenerate ikg = ImageKeyGenerate.getMD5SHA256();
		this.rowKey = ikg.generate(imageData);
		return this.rowKey;
	}
	///
	public static void setIdAssigner(ImageIdAssigner idAssigner)
	{
		if(ItemImage.idAssigner == null)
			ItemImage.idAssigner = idAssigner;
	}
	
	public void generateId() throws NullPointerException
	{
		if(idAssigner == null)
			throw new NullPointerException("image id assigner not specified");
		if(!this.colIndexMap.containsKey(IMAGE_ID))
		{
			//add the id
			this.addColumn(IMAGE_ID, MyBytes.toBytes(idAssigner.nextId()));
		}
	}
	
	public String getImageFileName()
	{
		if(imageFileName == null)
		{
			imageFileName = CommonUtils.byteArrayToHexString(this.rowKey);
		}
		return imageFileName;
	}
	
	public boolean saveImage()
	{
		//TODO: add code to save image to gridfs
		return MongoUtils.saveItemImage(this);
	}
	
	public boolean removeImage()
	{
		//TODO: add code to remove image from gridfs
		return MongoUtils.removeItemImage(CommonUtils.byteArrayToHexString(this.getRowKey()) + ".jpg");
	}
	
	public void setImageData(byte[] imageData)
	{
		this.imageData = imageData;
	}
	
	public byte[] getImageData()
	{
		return this.imageData;
	}
	
	@Override
	public String getTableName() {
		return "image1";
	}
	}
	
	public static class ItemMeta extends RowSerializable
	{
		public static final  String TITLE = "t";
		public static final String URL = "u";
		public static final String ASIN = "a";
		///the default merchant is amazon.com
		public static final String AMAZON = "AMZ";
		public static final String CNY = "CNY";
		public static final String MERCHANT =  "mc";
		public static final String CURRENCY_CODE = "cc";
		public static final String LIST_PRICE = "lp";
		public static final String LOWEST_NEW_PRICE = "lnp";
		public static final String LOWEST_REFURBISHED_PRICE = "lrp";
		public static final String LOWEST_USED_PRICE = "lup";
		public static final String LOWEST_COLLECTIBLE_PRICE = "lcp";
		public static final String TOTAL_NEW = "tn";
		public static final String TOTAL_REFURBISHED = "tr";
		public static final String TOTAL_COLLECTIBLE = "tc";
		public static final String TOTAL_USED = "tu";
		public static final String EXTRA = "e";
		public static final String PHOTO_URL = "pu";
		private static final String FOREIGN_IMAGE_ID = "fii";
		private static final String FOREIGN_IMAGE_COLUMN = "fic";
		private static final String META_HASH = "mh";
		private static final String PHOTO_HASH = "ph";
		
		@Override
		public String getTableName() {
			return "meta1";
		}
		
		protected void addPhoto(String photoUrl)
		{
			//first download the image data
			byte[] imageData = CommonUtils.getInternetImage(photoUrl);
			if(imageData != null)
			{
				ItemImage image = new ItemImage();
				image.setImageData(imageData);
				//first generate the hash key
				image.generateKey();
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
				}
				//save the image
				image.commitUpdate();
				image.commitDelete();
				///save the image to gridfs
				image.saveImage();
	
				//add photo url
				this.addColumn(ItemMeta.PHOTO_URL, MyBytes.toBytes(photoUrl));
				//now update ItemMeta
				this.addColumn(ItemMeta.FOREIGN_IMAGE_ID, image.getRowKey());
				this.addColumn(ItemMeta.FOREIGN_IMAGE_COLUMN, MyBytes.toBytes(foreignColName));
			}
		}
		
		protected void removePhoto(int photoIdx)
		{
			String photoColumn = RowSerializable.synthesizeFullColumnName(ItemMeta.PHOTO_URL, photoIdx);
			String imageIdColumn = RowSerializable.synthesizeFullColumnName(ItemMeta.FOREIGN_IMAGE_ID, photoIdx);
			String imageColColumn = RowSerializable.synthesizeFullColumnName(ItemMeta.FOREIGN_IMAGE_COLUMN, photoIdx);
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
		
		private void updatePhoto(ItemMeta meta2)
		{
			List<byte[]> photoList1 = this.getColumnList(ItemMeta.PHOTO_URL);
			List<byte[]> photoList2 = meta2.getColumnList(ItemMeta.PHOTO_URL);
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
		
		private void updateMeta(ItemMeta meta2)
		{
			//remove existing meta
			String[] metaFields = {TITLE, URL, LIST_PRICE, LOWEST_NEW_PRICE,
					LOWEST_REFURBISHED_PRICE, LOWEST_USED_PRICE, LOWEST_COLLECTIBLE_PRICE,
					TOTAL_NEW, TOTAL_REFURBISHED, TOTAL_USED, TOTAL_COLLECTIBLE, EXTRA};
			for(String field : metaFields)
			{
				this.removeColumnList(field);
			}
			//now replace the new values
			for(String field : metaFields)
			{
				List<byte[]> colValList = meta2.getColumnList(field);
				if(colValList.size() > 0)
					this.addColumnList(field, colValList);	
			}
			//
		}
		
		public void updateItem(ItemMeta item2)
		{
			
			this.computeMetaHash();
			this.computePhotoHash();
			item2.computeMetaHash();
			item2.computePhotoHash();
			///
			byte[] metaHash1 = this.getColumnFirst(ItemMeta.META_HASH);
			byte[] metaHash2 = item2.getColumnFirst(ItemMeta.META_HASH);
			
			byte[] photoHash1 = this.getColumnFirst(ItemMeta.PHOTO_HASH);
			byte[] photoHash2 = item2.getColumnFirst(ItemMeta.PHOTO_HASH);

			if(CommonUtils.byteArrayCompare(metaHash1, metaHash2) != 0)
			{
				this.updateMeta(item2);				
			}
			if(CommonUtils.byteArrayCompare(photoHash1, photoHash2) != 0)
				this.updatePhoto(item2);
		}
		
		@Override
		public String toString()
		{
			String[] strFields = {TITLE, URL, ASIN, PHOTO_URL};
			String[] floatFields = {LIST_PRICE,LOWEST_NEW_PRICE,LOWEST_USED_PRICE,LOWEST_REFURBISHED_PRICE,
					LOWEST_COLLECTIBLE_PRICE};
			String[] intFields = {TOTAL_NEW, TOTAL_USED, TOTAL_REFURBISHED, TOTAL_COLLECTIBLE};
			StringBuffer sb = new StringBuffer();
			for(String field : strFields)
			{
				List<byte[]> columnList = this.getColumnList(field);
				for(int i = 0; i < columnList.size(); i++)
				{
					String fieldName = field +"_" + i;
					byte[] fieldValue = columnList.get(i);
					String strField = (String)MyBytes.toObject(fieldValue, MyBytes.getDummyObject(String.class));
					sb.append(fieldName + ":" + strField + "\t");
				}
			}
			for(String field : intFields)
			{
				List<byte[]> columnList = this.getColumnList(field);
				for(int i = 0; i < columnList.size(); i++)
				{
					String fieldName = field +"_" + i;
					byte[] fieldValue = columnList.get(i);
					String strField = "" + (int)MyBytes.toObject(fieldValue,(int)0);
					sb.append(fieldName + ":" + strField + "\t");
				}
			}
			
			for(String field : floatFields)
			{
				List<byte[]> columnList = this.getColumnList(field);
				for(int i = 0; i < columnList.size(); i++)
				{
					String fieldName = field +"_" + i;
					byte[] fieldValue = columnList.get(i);
					String strField = " " + (float)MyBytes.toObject(fieldValue, (float)0.0f);
					sb.append(fieldName + ":" + strField + "\t");
				}
			}
			return sb.toString();
			
		}
		public void generateKey()
		{
			String merchant = (String)MyBytes.toObject(this.getColumnFirst(ItemMeta.MERCHANT),MyBytes.getDummyObject(String.class));
			String asin = (String)MyBytes.toObject(this.getColumnFirst(ItemMeta.ASIN),MyBytes.getDummyObject(String.class));
			String fullKeyName = merchant + "_" + asin;
			this.rowKey = MyBytes.toBytes(fullKeyName);
		}
		
		
		public  byte[] computeMetaHash()
		{
			if(this.colIndexMap.containsKey(ItemMeta.META_HASH))
			{
				return this.getColumnFirst(ItemMeta.META_HASH);
			}
			String[] fields = {
					TITLE, LOWEST_NEW_PRICE, LOWEST_USED_PRICE, LOWEST_REFURBISHED_PRICE, LOWEST_COLLECTIBLE_PRICE,
					TOTAL_NEW, TOTAL_REFURBISHED,TOTAL_USED,TOTAL_COLLECTIBLE,EXTRA,
			};
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
				this.addColumn(ItemMeta.META_HASH, hash);
				return hash;
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public byte[] computePhotoHash()
		{
			if(this.colIndexMap.containsKey(ItemMeta.PHOTO_HASH))
			{
				return this.getColumnFirst(ItemMeta.PHOTO_HASH);
			}
			
			List<byte[]> photoUrlByteList = this.getColumnList(ItemMeta.PHOTO_URL);
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
				this.addColumn(ItemMeta.PHOTO_HASH, hash);
				return hash;
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}
	
	public static class ItemMetaHash extends RowSerializable
	{
		public static final String META_HASH = "mh";
		public static final String PHOTO_HASH = "ph";
		@Override
		public String getTableName() {
			return "meta_hash";
		}
	}
	
}
