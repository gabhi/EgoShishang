package com.egoshishang.orm;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.util.StringUtils;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.util.MyBytes;

public abstract class RowSerializable {
	protected byte[] rowKey = {};
	protected byte[] colIndex = {};
	protected Map<String, Integer> colIndexMap = new HashMap<String, Integer>();
	protected Map<String, byte[]> colValueMap = new HashMap<String, byte[]>();
	protected List<String> removeList = new LinkedList<String>();
	public static final String COLUMN_MAX_INDEX = "cmi";
	public static final byte[] COLUMN_FAMILY = MyBytes.toBytes("d");

	public void setRowKey(byte[] rowKey) {
		this.rowKey = rowKey;
	}

	public byte[] getRowKey() {
		return this.rowKey;
	}
	public String getColumnIndexAsString()
	{
		return (String)MyBytes.toObject(colIndex,MyBytes.getDummyObject(String.class));
	}

	public byte[] getColumnValue(String columnName) {
		return this.colValueMap.get(columnName);
	}

	protected void deflateColumnIndexMap() {
		// generate a string and convert it to byte array
		List<String> colList = new LinkedList<String>();
		for (Entry<String, Integer> ent : colIndexMap.entrySet()) {
			String colName = ent.getKey();
			Integer maxIndex = ent.getValue();
			colList.add(synthesizeFullColumnName(colName, maxIndex));
		}
		String joinedStr = StringUtils.join("|", colList);
		colIndex = MyBytes.toBytes(joinedStr);
	}

	protected void inflateColumnIndexMap() {
		String joinedStr = (String) MyBytes.toObject(colIndex,
				MyBytes.getDummyObject(String.class));
		String[] colSplit = joinedStr.split("\\|");
		for (String col : colSplit) {
			String[] colIdx = col.split("_");
			colIndexMap.put(colIdx[0], Integer.valueOf(colIdx[1]));
		}
	}

	protected static String[] splitColumn(String fullColumnName) {
		String[] splits = fullColumnName.split("\\|");
		return splits;
	}

	public String addColumn(String colName, byte[] colunmValue) {
		Integer maxCol = 0;
		if (colIndexMap.containsKey(colName)) {
			maxCol = colIndexMap.get(colName);
			maxCol++;
		} else {
			maxCol = 0;
		}
		// update the columnn index
		colIndexMap.put(colName, maxCol);
		String fullColumnName = synthesizeFullColumnName(colName, maxCol);
		colValueMap.put(fullColumnName, colunmValue);
		return fullColumnName;
	}

	public void updateColumn(String fullColName, byte[] columnValue) {
		colValueMap.put(fullColName, columnValue);
	}

	public void removeColumn(String fullColName) {
		this.removeList.add(fullColName);
	}

	public void commitUpdate() {
		// deflate the column index map
		this.deflateColumnIndexMap();
		// add all columns
		Put put = new Put(this.rowKey);
		// /column max index must be tracked
		colValueMap.put(COLUMN_MAX_INDEX, this.colIndex);
		for (Entry<String, byte[]> ent : this.colValueMap.entrySet()) {
			String colName = ent.getKey();
			byte[] colValue = ent.getValue();
			put.add(COLUMN_FAMILY, MyBytes.toBytes(colName), colValue);
		}
		HTable table = this.getTable();
		try {
			table.put(put);
			this.putTable(table);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected HTable getTable() {
		return (HTable) HBaseInstance.getInstance().getTableFromPool(
				this.getTableName());
	}

	protected void putTable(HTable table) {
		HBaseInstance.getInstance().getTablePool().putTable(table);
	}

	public void commitDelete() {
		Delete delete = new Delete(this.rowKey);
		for (String col : this.removeList) {
			delete.deleteColumn(COLUMN_FAMILY, MyBytes.toBytes(col));
		}
		HTable table = getTable();
		try {
			table.delete(delete);
			putTable(table);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void retrieveFromHBase() {
		Get get = new Get();
		HTable table = getTable();
		try {
			Result res = table.get(get);
			for (KeyValue kv : res.list()) {
				byte[] qualifier = kv.getKey();
				String colName = (String) MyBytes.toObject(qualifier,
						MyBytes.getDummyObject(String.class));
				byte[] value = kv.getValue();

				if (colName.equals(RowSerializable.COLUMN_MAX_INDEX)) {
					this.colIndex = value;
					// /populate the key-value map
					this.inflateColumnIndexMap();
				} else {
					this.colValueMap.put(colName, value);
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean retrieveColumnInformation() {
		String[] colArr = {};
		return retrieve(Arrays.asList(colArr));
	}

	public void resultToField(Result res) {
		
		this.rowKey = res.getRow();
		// map result to column
		for (KeyValue kv : res.list()) {
			String key = (String) MyBytes.toObject(kv.getQualifier(),
					MyBytes.getDummyObject(String.class));
//			System.out.println(key);
			byte[] val = kv.getValue();
			if (key.equals(RowSerializable.COLUMN_MAX_INDEX)) {
				// also inflate the column index map
				this.colIndex = val;
				this.inflateColumnIndexMap();
				continue;
			}
			this.colValueMap.put(key, val);
		}
	}

	public boolean retrieve(List<String> cols) {
		Get get = new Get(this.rowKey);
		get.addColumn(COLUMN_FAMILY, MyBytes.toBytes(COLUMN_MAX_INDEX));
		for (String col : cols) {
			get.addColumn(COLUMN_FAMILY, MyBytes.toBytes(col));
		}

		HTable table = getTable();
		try {
			Result res = table.get(get);
			if (res.isEmpty())
				return false;
			resultToField(res);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	protected static String synthesizeFullColumnName(String columnName,
			Integer index) {
		return columnName + "_" + index;
	}

	public List<byte[]> getColumnList(String columnName) {
		List<byte[]> columnList = new LinkedList<byte[]>();
		if (this.colIndexMap.containsKey(columnName)) {
			Integer maxIndex = this.colIndexMap.get(columnName);

			for (int i = 0; i <= maxIndex; i++) {
				String fullColName = RowSerializable.synthesizeFullColumnName(
						columnName, i);
				if (this.colValueMap.containsKey(fullColName)) {
					columnList.add(this.colValueMap.get(fullColName));
				}
			}
		}
		return columnList;
	}

	public byte[] getColumnFirst(String columnName) {
		List<byte[]> columnList = getColumnList(columnName);
		if (columnList != null && columnList.size() > 0) {
			return columnList.get(0);
		}
		return null;
	}

	public abstract String getTableName();
}
