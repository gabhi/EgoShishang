package com.egoshishang.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.util.HBaseUtil;
import com.egoshishang.util.MyBytes;

import com.egoshishang.orm.HBaseObject.*;

public class RowWritableDumper {
	
	protected String dumpRoot = null;
	protected String metaFile = null;
	protected TableConfiguration tableConfig = null;
	protected HBaseInstance hbaseInst = null;
	protected BufferedWriter metaWriter = null;

	public RowWritableDumper(String dumpRoot, String metaFile) throws IOException
	{
		this.dumpRoot = dumpRoot;
		this.metaFile = metaFile;
		tableConfig = TableConfiguration.getHardcodeConfiguration();
		hbaseInst = HBaseInstance.getInstance();
		hbaseInst.createTablePool(Integer.MAX_VALUE);
		metaWriter = new BufferedWriter(new FileWriter(dumpRoot + "/" + metaFile));
	}
	
	protected void saveImage(byte[] imageByte, String imageName) throws IOException
	{
		FileOutputStream ofs = new FileOutputStream(dumpRoot + "/" + imageName + ".jpg");
		MyBytes.streamCopy(new ByteArrayInputStream(imageByte), ofs, 10240);
		ofs.close();
	}

	public void dump() throws IOException
	{
		Scan scan = new Scan();
		ItemImage itemImage = new ItemImage();
		String imageTableName = itemImage.getTableName();
		ItemMeta itemMeta = new ItemMeta();
		String metaTableName = itemMeta.getTableName();
		
		HTable imageTable = (HTable) hbaseInst.getTableFromPool(imageTableName);
		HTable metaTable = (HTable) hbaseInst.getTableFromPool(metaTableName);
		
		ResultScanner resultScanner = imageTable.getScanner(scan);

		for(Result res : resultScanner)
		{
			ItemImage tmpImage = new ItemImage();
			tmpImage.resultToField(res);
			//now save the image
			String imageName = (String)MyBytes.toObject(tmpImage.getColumnFirst(ItemImage.ITEM_ID),MyBytes.getDummyObject(String.class));
			this.saveImage(tmpImage.getColumnFirst(ItemImage.IMAGE_DATA), imageName);
		}
		resultScanner.close();

		resultScanner = metaTable.getScanner(scan);
		for(Result res : resultScanner)
		{
			ItemMeta tmpMeta = new ItemMeta();
			tmpMeta.resultToField(res);
			metaWriter.append(tmpMeta.toString()+ "\n");
		}
		resultScanner.close();
		metaWriter.close();
		
	}
	public static void main(String[] args)
	{
		String dumpRoot = args[0];
		String metaFile = args[1];
		try {
			RowWritableDumper dumper = new RowWritableDumper(dumpRoot, metaFile);
			dumper.dump();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
