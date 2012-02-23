package net.walnutvision.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.regionserver.StoreFile;

public class HBaseInstance extends Configured{
	protected HTable table = null;
	protected Map<String,HTable> tableMap = null;
	protected HTablePool tablePool = null;
	protected HBaseAdmin admin = null;
	private static HBaseInstance instance = new HBaseInstance();
	public static final String COLUMN_FAMILY_NAME = "d";
	@SuppressWarnings("deprecation")
	
	private HBaseInstance()
	{
	}
	
	public static  HBaseInstance getInstance()
	{
		return instance;
	 }
	
	public void init(Configuration conf)
	{
		this.setConf(conf);
		try {
			admin = new HBaseAdmin(this.getConf());
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tableMap = new HashMap<String,HTable>();		
	}
	
	public HTablePool createTablePool(int max)
	{
		tablePool = new HTablePool(this.getConf(),max);
		return tablePool;
	}
	
	public HTableInterface getTableFromPool(String tableName)
	{
		return tablePool.getTable(tableName);
	}
	
	public void createAdmin() throws MasterNotRunningException, ZooKeeperConnectionException
	{
		admin = new HBaseAdmin(this.getConf());
	}
	
	public boolean createTableIfNotExist(String tableName) throws IOException
	{
		if(admin == null)
		{
				createAdmin();
		}
		if(!admin.tableExists(tableName))
		{
			HTableDescriptor table = new HTableDescriptor(tableName);
			HColumnDescriptor columnFamily = new HColumnDescriptor(COLUMN_FAMILY_NAME);
			table.addFamily(columnFamily);
			columnFamily.setBloomFilterType(StoreFile.BloomType.ROWCOL);
			///create the table using default parameters
			admin.createTable(table);
			return false;
		}
		return true;
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
				HTable newTable = new HTable(this.getConf(), tableName);
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
