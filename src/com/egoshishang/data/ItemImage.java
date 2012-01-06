package com.egoshishang.data;

import org.apache.hadoop.hbase.util.Bytes;

public class ItemImage {
	protected byte[] imageByte = null;
	protected byte[] key = null;
	protected String itemUrl = null;
	protected byte[] itemMeta = null;

	public byte[] getImageByte() {
		return imageByte;
	}
	public void setImageByte(byte[] imageByte) {
		this.imageByte = imageByte;
	}
	public String getItemUrl() {
		return itemUrl;
	}
	public void setItemUrl(String itemUrl) {
		this.itemUrl = itemUrl;
	}
	public byte[] getItemMeta() {
		return itemMeta;
	}
	
	public void setItemMeta(byte[] itemMeta) {
		this.itemMeta = itemMeta;
	}
	
	public ItemImage()
	{
		
	}
	
	public void setKey(long key)
	{
		this.key = Bytes.toBytes(key);
	}
	public long getKey()
	{
		return Bytes.toLong(this.key);
	}
	public byte[] getKeyByte()
	{
		return this.key;
	}
	

}
