package net.walnutvision.sys;

import java.io.IOException;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.RowLock;

import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.orm.RowSerializable;
import net.walnutvision.util.MyBytes;

public class HBaseImageIdAssigner extends ImageIdAssigner {

	protected static final String IMAGE_ID_TABLE_NAME = "id_assigner";
	protected static final String MAX_IMAGE_ID_ROW_KEY = "max_image_id";
	protected static final byte[] MAX_IMAGE_ID_ROW_KEY_BYTES = MyBytes.toBytes(MAX_IMAGE_ID_ROW_KEY);
	
	public static final HBaseInstance HBASE_INSTANCE = HBaseInstance.getInstance();
	
	public static class MaxImageId extends RowSerializable
	{
		public static final String MAX_IMAGE_ID = "mii";
		@Override
		public String getTableName() {
			return IMAGE_ID_TABLE_NAME;
		}
		
	}
	
	
	public static ImageIdAssigner getInstance()
	{
		if(ImageIdAssigner.assignerInst == null)
		{
			assignerInst = new HBaseImageIdAssigner();
		}
		return assignerInst;
	}
	@Override
	public void setUp(String configFilePath) {
		///check the availability of id table
		try {
			///create the table if not exist
			HBASE_INSTANCE.createTableIfNotExist(IMAGE_ID_TABLE_NAME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void readId() {
	}

	@Override
	public void writeId() {
		// TODO Auto-generated method stub

	}

	@Override
	public long nextId() {
		// TODO Auto-generated method stub
		long nextId = 0;
		
		try {
			MaxImageId maxImageId = new MaxImageId();
			maxImageId.setRowKey(MAX_IMAGE_ID_ROW_KEY_BYTES);
			HTable table = maxImageId.getTable();
			RowLock	rowLock = table.lockRow(MAX_IMAGE_ID_ROW_KEY_BYTES);
			if(!maxImageId.retrieveFromHBaseWithLock(table, rowLock))
			{
				maxImageId.addColumn(MaxImageId.MAX_IMAGE_ID, MyBytes.toBytes((long)0));
			}
			else
			{
				nextId = MyBytes.toObject(maxImageId.getColumnFirst(MaxImageId.MAX_IMAGE_ID),(long)0);
				nextId++;
				maxImageId.updateColumn(RowSerializable.synthesizeFullColumnName(MaxImageId.MAX_IMAGE_ID, 0), MyBytes.toBytes(nextId));
			}
			maxImageId.commitUpdateWithLock(table, rowLock);
			maxImageId.putTable(table);
			table.unlockRow(rowLock);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nextId;
	}

	@Override
	public void tearDown() {

	}

}
