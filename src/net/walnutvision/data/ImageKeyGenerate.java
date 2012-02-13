package net.walnutvision.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.hadoop.hbase.util.Bytes;

public abstract class  ImageKeyGenerate {
	public abstract byte[] generate(byte[] imageByte);

	private static ImageKeyGenerate md5Inst = new MD5ImageKeyGen();
	private static ImageKeyGenerate sha256Inst = new SHA2ImageKeyGen();
	private static ImageKeyGenerate md5Sha256Inst = new MD5SHA256();
	public static ImageKeyGenerate getMD5KeyGenerator()
	{
		return md5Inst;
	}
	public static ImageKeyGenerate getSHA256KeyGenerator()
	{
		return sha256Inst;
	}
	public static ImageKeyGenerate getMD5SHA256()
	{
		return md5Sha256Inst;
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
	
	private static class SHA2ImageKeyGen extends ImageKeyGenerate
	{

		@Override
		public byte[] generate(byte[] imageByte) {
			byte[] key = null;
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				key = md.digest(imageByte);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return key;
		}
	}
	private static class MD5SHA256 extends ImageKeyGenerate
	{

		@Override
		public byte[] generate(byte[] imageByte) {
			byte[] md5Key = null;
			byte[] sha256Key = null;
			byte[] combinedKey = null;
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				sha256Key = md.digest(imageByte);
				md = MessageDigest.getInstance("MD5");
				md5Key = md.digest(imageByte);
				combinedKey = new byte[sha256Key.length + md5Key.length];
				for(int i = 0; i < md5Key.length; i++)
				{
					combinedKey[i] = md5Key[i];
				}
				for(int i = 0; i < sha256Key.length; i++)
				{
					combinedKey[md5Key.length + i] = sha256Key[i];
				}
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return combinedKey;
		}
	}
}
