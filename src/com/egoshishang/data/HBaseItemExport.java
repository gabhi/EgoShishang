package com.egoshishang.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;

import com.egoshishang.conf.HBaseInstance;
import com.egoshishang.conf.TableConfiguration;
import com.egoshishang.orm.HBaseObject.ItemImage;
import com.egoshishang.orm.HBaseObject.ItemMeta;
import com.egoshishang.sys.ImageIdAssigner;
import com.egoshishang.util.MyBytes;

public abstract class HBaseItemExport {
	protected long numDownloaded = 0;	
	public abstract List<ItemMeta> generateItemMeta();	
	public abstract void export();	
	
}
