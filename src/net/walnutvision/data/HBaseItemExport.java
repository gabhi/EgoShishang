package net.walnutvision.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import net.walnutvision.conf.HBaseInstance;
import net.walnutvision.conf.TableConfiguration;
import net.walnutvision.orm.HBaseObject.ItemImage;
import net.walnutvision.orm.HBaseObject.ItemMeta;
import net.walnutvision.sys.ImageIdAssigner;
import net.walnutvision.util.MyBytes;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IOUtils;


public abstract class HBaseItemExport {
	protected long numDownloaded = 0;	
	public abstract List<ItemMeta> generateItemMeta();	
	public abstract void export();	
	
}
