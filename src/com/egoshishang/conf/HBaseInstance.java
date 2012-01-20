package com.egoshishang.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;

public class HBaseInstance {
	protected HTable table = null;
	protected Map<String,HTable> tableMap = null;
	protected HTablePool tablePool = null;
	private static HBaseInstance instance = new HBaseInstance();
	Configuration config = null;
	@SuppressWarnings("deprecation")
	
	private HBaseInstance()
	{
		config = HBaseConfiguration.create();
		tableMap = new HashMap<String,HTable>();
	}
	
	public static  HBaseInstance getInstance()
	{
		return instance;
	 }
	
	public HTablePool createTablePool(int max)
	{
		tablePool = new HTablePool(this.config,max);
		return tablePool;
	}
	
	public HTableInterface getTableFromPool(String tableName)
	{
		return tablePool.getTable(tableName);
	}
	
	public HTablePool getTablePool()
	{
		return tablePool;
	}
	public  HTable getTable(String tableName)
	{
		HTable tableReturn = null;
		if(tableMap.containsKey(tableName))
		{
			tableReturn = tableMap.get(tableName);
		}
		else
		{
			try {
				System.out.println("requested table: " + tableName);
				HTable newTable = new HTable(this.config, tableName);
				tableMap.put(tableName, newTable);
				tableReturn = newTable;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tableReturn;
	}
	
}
