package com.egoshishang.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;

public class HBaseInstance {
	protected HTable table = null;
	protected Map<String,HTable> tableMap = null;
	private static HBaseInstance instance = new HBaseInstance();
	Configuration config = null;
	@SuppressWarnings("deprecation")
	
	private HBaseInstance()
	{
		config = new HBaseConfiguration();
		tableMap = new HashMap<String,HTable>();
	}
	
	public static  HBaseInstance getInstance()
	{
		return instance;
		
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
