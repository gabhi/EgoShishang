package com.egoshishang.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;


public class IOFormat {
	private static final String[] DATA = { "One, two, buckle my shoe", "Three, four, shut the door", "Five, six, pick up sticks",
	"Seven, eight, lay them straight", "Nine, ten, a big fat hen"
	};
	protected static void _testMapFile(String[] args)
	{
		String uri = args[0];
		Configuration conf = new Configuration();
		try {
			FileSystem fs = FileSystem.get(URI.create(uri),conf);
			IntWritable key = new IntWritable();
			Text value = new Text();
			MapFile.Writer writer = null;
			writer = new MapFile.Writer(conf, fs, uri, key.getClass(), value.getClass());
			for(int i = 0; i < 1024; i++)
			{
				key.set(i+1);
				value.set(DATA[i % DATA.length]);
				writer.append(key, value);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected static void _dealWithHDFS(String[] args)
	{
		FSDataInputStream in = null;
		try {
			FileSystem fs = FileSystem.get(new Configuration());
			in = fs.open(new Path(args[0]));
		
			IOUtils.copyBytes(in, System.out, 4096,false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			IOUtils.closeStream(in);
		}
	}
	public static void main(String[] args)
	{
		_dealWithHDFS(args);
	}

}
