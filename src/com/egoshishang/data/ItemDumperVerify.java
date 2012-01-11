package com.egoshishang.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.util.DataUtility;

public class ItemDumperVerify {
	protected String dumpRoot = null;
	protected String metaFile = null;
	protected TableConfiguration tableConfig = null;
	protected HBaseInstance hbaseInst = null;
	protected BufferedWriter metaWriter = null;
	public ItemDumperVerify(String dumpRoot, String metaFile) throws IOException
	{
		this.dumpRoot = dumpRoot;
		this.metaFile = metaFile;
		tableConfig = TableConfiguration.getHardcodeConfiguration();
		hbaseInst = HBaseInstance.getInstance();
		metaWriter = new BufferedWriter(new FileWriter(dumpRoot + "/" + metaFile));
	}
	public void dump() throws IOException
	{
		Scan scan = new Scan();
		HTable contentTable = hbaseInst.getTable(Bytes.toString(tableConfig.getParam(TableConfiguration.CONTENT_TABLE_KEY)));
		ResultScanner resultScanner = contentTable.getScanner(scan);

		byte[] cf = tableConfig.getParam(TableConfiguration.CONTENT_TABLE_CF);
		byte[] urlCol = tableConfig.getParam(TableConfiguration.CONTENT_ITEM_URL_QUALIFIER);
		byte[] imageCol = tableConfig.getParam(TableConfiguration.CONTENT_IMAGE_DATA_QUALIFIER);
		byte[] metaCol = tableConfig.getParam(TableConfiguration.CONTENT_META_DATA_QUALIFIER);

		for(Result res : resultScanner)
		{
			String detailUrl = "";
			long rowKey = -1;
			ItemMeta metaInfo = new ItemMeta();
			byte[] imageData = null;
			rowKey = Bytes.toLong(res.getRow());
			if(res.containsColumn(cf, metaCol))
			{
			
				metaInfo = (ItemMeta)DataUtility.byteArrayToObject(res.getValue(cf, metaCol));
			}
			if(res.containsColumn(cf, urlCol))
			{
				detailUrl = Bytes.toString(res.getValue(cf, urlCol));
			}
			if(res.containsColumn(cf, imageCol))
			{
				imageData = res.getValue(cf,imageCol);
			}
			metaWriter.write(rowKey + "\t" + metaInfo.toString() + "\t" + detailUrl);
			metaWriter.newLine();
			String imageName = metaInfo.asin;
			if(imageData != null)
			{
				saveImage(imageData, imageName + ".jpg");
			}
		}
		resultScanner.close();
		metaWriter.close();
		
	}
	protected void saveImage(byte[] imageByte, String imageName) throws IOException
	{
		FileOutputStream ofs = new FileOutputStream(dumpRoot + "/" + imageName);
		DataUtility.streamCopy(new ByteArrayInputStream(imageByte), ofs, 10240);
		ofs.close();
	}
	public static void main(String[] args)
	{
		String dumpRoot = args[0];
		String metaFile = args[1];
		try {
			ItemDumperVerify dumper = new ItemDumperVerify(dumpRoot, metaFile);
			dumper.dump();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
