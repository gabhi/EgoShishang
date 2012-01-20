package com.egoshishang.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CommonUtils {
	public static String arrayToString(Object[] objArr)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < objArr.length; i++)
		{
			sb.append(i == 0? "" : "\t");
			sb.append(objArr[i].toString());
		}
		return sb.toString();
	}
	public static int byteArrayCompare(byte[] arr1, byte[] arr2)
	{
		int result = 0;
		for(int i =  0; i < arr1.length; i++)
		{
			if(arr1[i] < arr2[i])
			{
				result = -1;
				break;
			}
			if(arr1[i] > arr2[i])
			{
				result = 1;
				break;
			}
		}
		return result;
	}
	
	public static byte[] getWebPage(String url) {
		byte[] imageByte = null;
		int tries = 3;
		while (tries > 0) {
			try {
				URL server = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) server
						.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection
						.addRequestProperty(
								"Accept",
								"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
				connection.addRequestProperty("Accept-Language",
						"en-us,zh-cn;q=0.5");
				connection.addRequestProperty("Accept-Encoding",
						"gzip, deflate");
				connection
						.addRequestProperty(
								"User-Agent",
								"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 2.0.50727; MS-RTC LM 8)");
				connection.connect();
				InputStream is = connection.getInputStream();
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				MyBytes.streamCopy(is,byteStream,102400);
//				IOUtils.copyBytes(is, byteStream, 102400);
				imageByte = byteStream.toByteArray();
				break;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.err.println("err:"  + url);
//				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
				System.err.println("retry: " + url);
				tries--;
			}
		}
		return imageByte;
	}
	
	public static byte[] getInternetImage(String url) {
			byte[] imageByte = null;
			int tries = 3;
			while (tries > 0) {
				try {
					URL server = new URL(url);
					HttpURLConnection connection = (HttpURLConnection) server
							.openConnection();
					connection.setRequestMethod("GET");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					connection
							.addRequestProperty(
									"Accept",
									"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
					connection.addRequestProperty("Accept-Language",
							"en-us,zh-cn;q=0.5");
					connection.addRequestProperty("Accept-Encoding",
							"gzip, deflate");
					connection
							.addRequestProperty(
									"User-Agent",
									"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 2.0.50727; MS-RTC LM 8)");
					connection.connect();
					InputStream is = connection.getInputStream();
					ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
					MyBytes.streamCopy(is,byteStream,102400);
	//				IOUtils.copyBytes(is, byteStream, 102400);
					imageByte = byteStream.toByteArray();
					break;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					System.err.println("err:"  + url);
//					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
	//				e.printStackTrace();
					System.err.println("retry: " + url);
					tries--;
				}
			}
			return imageByte;
		}
}
