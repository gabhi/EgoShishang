package com.egoshishang.orm;

import java.util.Arrays;
import java.util.List;

import com.egoshishang.orm.HBaseObject.ItemImage;
import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.orm.HBaseObject.ItemMetaHash;
import com.egoshishang.util.CommonUtils;
import com.egoshishang.util.MyBytes;

public class ItemOperator {
	
	public static void insertItem(ItemMeta meta)
	{
		//get all photo urls
		List<byte[]> photoUrls = meta.getColumnList(ItemMeta.PHOTO_URL);
		//generate the item key, a combination of the merchant and item id
		meta.generateKey();
		//keep the key
		byte[] metaKey = meta.getRowKey();
		
		//download the image
		for(byte[] urlByte : photoUrls)
		{
			String url = (String)MyBytes.toObject(urlByte, MyBytes.getDummyObject(String.class));
			byte[] imageData = CommonUtils.getInternetImage(url);
			if(imageData != null )
			{
				//insert an image to the data base
				ItemImage itemImage = new ItemImage();
				itemImage.addColumn(ItemImage.IMAGE_DATA, imageData);
				//generate the key
				itemImage.generateKey();
				//query the existence of the image
				ItemImage queryImage = new ItemImage();
				queryImage.setRowKey(itemImage.getRowKey());
				
				boolean exist = queryImage.retrieve(Arrays.asList(new String[]{ItemImage.ITEM_ID}));
				if(exist)
				{
					//we are only interested in updating the item id
					itemImage = queryImage;
				}
				String itemIdCol = itemImage.addColumn(ItemImage.ITEM_ID, metaKey);
				itemImage.commitUpdate();
				meta.addColumn(ItemMeta.PHOTO_KEY, itemImage.getRowKey());
				meta.addColumn(ItemMeta.FOREIGN_PHOTO_COL, MyBytes.toBytes(itemIdCol));
			}
			else
			{
				//no image data available, nothing to save
				System.err.println("bad meta:" + meta);
				return;
			}
		}
		//generate hash
		ItemMetaHash metaHash = new ItemMetaHash();
		metaHash.setRowKey(meta.getRowKey());
		byte[] metaHashByte = meta.computeMetaHash();
		byte[] photoHashByte = meta.computePhotoHash();
		if(metaHashByte != null && photoHashByte != null)
		{
			metaHash.addColumn(ItemMetaHash.META_HASH, meta.computeMetaHash());
			metaHash.addColumn(ItemMetaHash.PHOTO_HASH, meta.computePhotoHash());
			metaHash.commitUpdate();
		}
		//
		meta.commitUpdate();
	}
}
