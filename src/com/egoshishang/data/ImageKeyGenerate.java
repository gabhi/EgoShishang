package com.egoshishang.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class  ImageKeyGenerate {
	public abstract byte[] generate(byte[] imageByte);

	private static ImageKeyGenerate md5Inst = new MD5ImageKeyGen();
	public static ImageKeyGenerate getMD5KeyGenerator()
	{
		return md5Inst;
	}
	private static class MD5ImageKeyGen extends ImageKeyGenerate
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
