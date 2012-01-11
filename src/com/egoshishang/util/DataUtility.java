package com.egoshishang.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.hadoop.io.IOUtils;

public class DataUtility {

	public static byte[] objectToByteArray(Object obj) {
		byte[] objByte = null;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(obj);
			objectStream.flush();
			objByte = byteStream.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objByte;
	}

	public static Object byteArrayToObject(byte[] byteArray) {
		Object obj = null;
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(
					byteArray);
			ObjectInputStream objectStream = new ObjectInputStream(byteStream);
			obj = objectStream.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	public static void streamCopy(InputStream is, OutputStream os, int bufSize)
	{
		byte[] buffer = new byte[bufSize];
		int readLen = 0;
		try {
			while( (readLen = is.read(buffer)) > 0)
			{
				os.write(buffer, 0, readLen);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				streamCopy(is,byteStream,102400);
//				IOUtils.copyBytes(is, byteStream, 102400);
				imageByte = byteStream.toByteArray();
				break;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
