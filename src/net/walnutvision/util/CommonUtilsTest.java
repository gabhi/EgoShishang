package net.walnutvision.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

public class CommonUtilsTest {

	@Test
	public void testByteToString()
	{
		String testStr = "hello,world, compute my digest";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(MyBytes.toBytes(testStr));
			String hashHex = CommonUtils.byteArrayToHexString(hash);
			System.out.println(hashHex);
			System.out.println(hashHex.length());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testToASCII()
	{
		String str = "hello,world";
		byte[] strBytes = CommonUtils.StringToASCII(str);
		String str1 = CommonUtils.ASCIIToString(strBytes);
		System.out.println(str1);
	}
	@Test
	public void testHexString()
	{
		byte[] byteArr = new byte[]{1,2,3,16,(byte)255};
		String hexStr = CommonUtils.byteArrayToHexString(byteArr);
		System.out.println(hexStr);
		byte[] byteArr2 = CommonUtils.hexStringToByteArray(hexStr);
		String hexStr2 = CommonUtils.byteArrayToHexString(byteArr2);
		System.out.println(hexStr2);
	}
	@Test
	public void testDownloadImage()
	{
		String imageUrl = "http://images.amazon.com/images/P/B0016KFXGO.01._SCLZZZZZZZ_PU_PU-5_.jpg";
		byte[] data = CommonUtils.getInternetImage(imageUrl);
		if(data != null)
		{
			System.out.println(data.length);
		}
	}
}
