package com.egoshishang.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class TestHBase {
	public static void main(String[] args) {
		Put put = null;
		Configuration conf = HBaseConfiguration.create();
		try {
			HTable htable = new HTable(conf, "egoshishang_product");
			BufferedReader br = new BufferedReader(new FileReader(
					"product.dump_c"));
			htable.setAutoFlush(false);
			String line = null;
			byte[] rowKey = null;
			byte[] columnFamily = Bytes.toBytes("info");
			while ((line = br.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(line, "\t");
				while (tokens.hasMoreTokens()) {
					String token = tokens.nextToken();
					String[] splits = token.split("=>");
					String key = splits[0];
					String val = splits[1];
					if (key.equals("pid")) {
						rowKey = Bytes.toBytes(val);
						put = new Put(rowKey);
					} else {
						put.add(columnFamily, Bytes.toBytes(key),
								Bytes.toBytes(val));
					}
				}
				htable.put(put);
			}
			htable.flushCommits();
			htable.close();
			System.out.println("done");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
