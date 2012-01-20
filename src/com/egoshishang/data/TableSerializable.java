package com.egoshishang.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.util.Bytes;

import com.egoshishang.util.MyBytes;

public abstract class TableSerializable{
	protected byte[] rowKey;
	protected Map<String,String> mcMap = new HashMap<String,String>();
	protected Map<String,String> cmMap = new HashMap<String,String>();
	public void setup()
	{
		this.setupCFMap();
		this.setupFCMap();
	}
	public void setRowKey(byte[] rowKey)
	{
		this.rowKey = rowKey;
	}
	public byte[] getRowKey()
	{
		return this.rowKey;
	}
	public Map<String,String> getFieldColumnMap()
	{
		return mcMap;
	}
	public Map<String,String> getColumnFieldMap()
	{
		return cmMap;
	}
	
	protected final void setupFCMap()
	{
		for(Entry<String, String> cf : this.cmMap.entrySet())
		{
			mcMap.put(cf.getValue(), cf.getKey());
		}
	}
	protected byte[] getMemberQualifier(String member)
	{
		return Bytes.toBytes(this.mcMap.get(member));
	}
	protected abstract void setupCFMap();
	protected abstract String getTableName();
	public abstract void writeToTable();
	public abstract void readFromTable();
	public static final byte[] cfByte = MyBytes.toBytes("cf");
}
