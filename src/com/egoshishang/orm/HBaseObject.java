package com.egoshishang.orm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.egoshishang.data.ImageKeyGenerate;
import com.egoshishang.util.MyBytes;

public class HBaseObject {
	
	public  static class ItemImage extends RowSerializable
	{
	///column name for item id(list)
	public static final String ITEM_ID = "ii";
	///column name for image data
	public static final String IMAGE_DATA = "id";
	///
	public static final String UPDATE_CODE = "up";
	public static final String ADD_IMAGE = "ai";
	public static final String REMOVE_IMAGE = "ri";
	///compute the image hash key
	public  byte[] generateKey()
	{
		byte[] imageData = this.getColumnFirst(ItemImage.IMAGE_DATA);
		ImageKeyGenerate ikg = ImageKeyGenerate.getMD5SHA256();
		this.rowKey = ikg.generate(imageData);
		return this.rowKey;
	}
	
	@Override
	public String getTableName() {
		return "image";
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
		public static final String PHOTO_KEY = "pk";
		public static final String PHOTO_URL = "pu";
		public static final String FOREIGN_PHOTO_COL = "fpc";
		
		@Override
		public String getTableName() {
			return "meta";
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
				return hash;
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		public byte[] computePhotoHash()
		{
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
