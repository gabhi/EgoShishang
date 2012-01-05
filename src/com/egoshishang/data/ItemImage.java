package com.egoshishang.data;

import java.security.*;

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
	
	public void genKey(ImageKeyGenerate keyGen)
	{
		this.key = keyGen.generate(imageByte);
	}
	
	public static class MD5ImageKeyGen implements ImageKeyGenerate
	{

		@Override
		public byte[] generate(byte[] imageByte) {
			byte[] key = null;
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				key = md.digest(imageByte);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return key;
		}
	}
}
