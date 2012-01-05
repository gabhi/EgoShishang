package com.egoshishang.data;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.egoshishang.amazon.ItemProfileFetcher;
import com.egoshishang.util.*;

@SuppressWarnings("deprecation")
public class ExportItem extends Configured implements Tool{

	private Options _constructOptions()
	{
		Options opts = new Options();
		//need input hdfs path
		Option o = MapRedUtility.constructOption("i","input file",true,"input hdfs file path");
		opts.addOption(o);
		//hbase table
		o = MapRedUtility.constructOption("t", "table name", true, "the hbase table where the data sinks to ");
		opts.addOption(o);
		//column family
		o = MapRedUtility.constructOption("cf", "column family", true, "the column family of the table");
		opts.addOption(o);
		return opts;
	}

	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] args) throws Exception {
		CommandLineParser cmdParser = new PosixParser();
		CommandLine cmd = cmdParser.parse(_constructOptions(),args);
		String inputFilePath = cmd.getOptionValue("i");
		String tableName = cmd.getOptionValue("t");
		String columnFamily = cmd.getOptionValue("cf");
		JobConf job = new JobConf(getConf(),ImportFromFile.class);
		job.setJobName("Import files from hdfs");
		job.setOutputFormat(NullOutputFormat.class);
		job.setOutputKeyClass(ImmutableBytesWritable.class);
		job.setOutputValueClass(Writable.class);
		job.set("conf.cf", columnFamily);
		job.set("conf.tab", tableName);
		job.setMapperClass(ItemQueryMapper.class);
		job.setNumReduceTasks(0);
		FileInputFormat.addInputPath(job, new Path(inputFilePath));
		JobClient.runJob(job);
		return 0;
	}
	
	public static void main(String[] args)
	{
		try {			
			ToolRunner.run(HBaseConfiguration.create(), new ImportFromFile(), args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class ItemQueryMapper extends MapReduceBase implements Mapper<Object, Text, ImmutableBytesWritable, Writable>
	{
		private byte[] columnFamily = null;
		private String tableName = null;
		private HTable table = null;
		private ItemProfileFetcher itemFetcher = null;
		private WorkQueue wq = null;
		private int numDownloadThreads = 10;
		
		@Override
		public void configure(JobConf conf)
		{
			columnFamily = Bytes.toBytes(conf.get("conf.cf"));
			tableName = conf.get("conf.tab");
			numDownloadThreads = conf.getInt("conf.thread.num",10);
			//get a htable instance
			this.table = HBaseInstance.getInstance().getTable(tableName);
			itemFetcher = new ItemProfileFetcher();
			wq = new WorkQueue(numDownloadThreads);
		}
		
		private class DownloadThread implements Runnable
		{
			private byte[] asin;
			private byte[] qualifier;
			private String imageUrl;
			
			public DownloadThread(final byte[] asin, final byte[] qualifier, final String imageUrl)
			{
				this.asin = asin;
				this.qualifier = qualifier;
				this.imageUrl = imageUrl;
			}
			
			@Override
			public void run()
			{
				//download the image from internet and get its raw bytes
				byte[] imageBytes = DataUtility.getInternetImage(imageUrl);
				if(imageBytes != null && imageBytes.length > 0)
				{
					//write to hbase
					Put put = new Put(asin);
					put.add(columnFamily,qualifier, imageBytes);
					try {
						table.put(put);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		@Override
		public void map(Object arg0, Text line,
				OutputCollector<ImmutableBytesWritable, Writable> output,
				Reporter reporter) throws IOException {
			String[] asins = line.toString().split("\t");
			List<String> asinList = new LinkedList<String>();
			for(String asin : asins)
			{
				asinList.add(asin);
			}
			//fetch items from amazon 
			List<ItemMeta> metaList = itemFetcher.batchProductQuery(asinList);
			//now add the meta data to the hbase and also generate threads to download the photo url
			byte[] metaQualifier = Bytes.toBytes("mt");
			byte[] imageBinQualifier = Bytes.toBytes("im");
			for(int i = 0; i < metaList.size(); i++)
			{
				byte[] rowKey = Bytes.toBytes(asins[i]);
				ItemMeta meta = metaList.get(i);
				Put put = new Put(rowKey);
				put.add(this.columnFamily, metaQualifier, DataUtility.objectToByteArray(meta));
				table.put(put);
				wq.execute(new DownloadThread(rowKey,imageBinQualifier,meta.photoUrl));
			}
		}
	}
}
