package net.walnutvision.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.walnutvision.util.MyBytes;

public abstract class RowWritable {
	protected byte[] rowKey = null;
	public static final byte[] CF_BYTES = MyBytes.toBytes("d");
	protected Map<String,String> mcMap = new HashMap<String,String>();
	protected Map<String,String> cmMap = new HashMap<String,String>();
	protected abstract void setupCFMap();
	public abstract String getTableName();

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
	
	protected byte[] getFieldQualifier(String fldName)
	{
		return MyBytes.toBytes(mcMap.get(fldName));
	}
	
	protected final void setupFCMap()
	{
		for(Entry<String, String> cf : this.cmMap.entrySet())
		{
			mcMap.put(cf.getValue(), cf.getKey());
		}
	}
}
