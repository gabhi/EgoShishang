package net.walnutvision.data;


import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
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
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


@SuppressWarnings("deprecation")
public class ImportFromFile extends Configured implements Tool{

	private static Option _optionHelper(String optShort, String optName, boolean isRequired, String description)
	{
		Option opt = new Option(optShort, optName, isRequired, description);
		return opt;
	}
	private Options _constructOptions()
	{
		Options opts = new Options();
		//need input hdfs path
		Option o = _optionHelper("i","input file",true,"input hdfs file path");
		opts.addOption(o);
		//hbase table
		o = _optionHelper("t", "table name", true, "the hbase table where the data sinks to ");
		opts.addOption(o);
		//column family
		o = _optionHelper("cf", "column family", true, "the column family of the table");
		opts.addOption(o);
		return opts;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] arg0) throws Exception {
		CommandLineParser cmdParser = new PosixParser();
		CommandLine cmd = cmdParser.parse(_constructOptions(),arg0);
		String inputFilePath = cmd.getOptionValue("i");
		String tableName = cmd.getOptionValue("t");
		String columnFamily = cmd.getOptionValue("cf");
		JobConf job = new JobConf(getConf(),ImportFromFile.class);
		job.setJobName("Import files from hdfs");
		job.setOutputFormat(TableOutputFormat.class);
		job.set(TableOutputFormat.OUTPUT_TABLE, tableName);
		job.set("conf.cf", columnFamily);
		job.setMapperClass(ProductMapper.class);
		job.setNumReduceTasks(0);
		job.setOutputKeyClass(ImmutableBytesWritable.class);
		job.setOutputValueClass(Writable.class);
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

	public static class ProductMapper extends MapReduceBase implements Mapper<Object, Text, ImmutableBytesWritable, Writable>
	{
		private byte[] columnFamily = null;
		@Override
		public void configure(JobConf conf)
		{
			columnFamily = Bytes.toBytes(conf.get("conf.cf"));
		}
		@Override
		public void map(Object arg0, Text line,
				OutputCollector<ImmutableBytesWritable, Writable> output,
				Reporter reporter) throws IOException {
			StringTokenizer tokens = new StringTokenizer(line.toString(),"\t");
			//each token is a k:v pair, k is the column name
			byte[] rowKey = null;
			
			Put put = null;
			while(tokens.hasMoreTokens())
			{
				String token = tokens.nextToken();
				String[] splits = token.split("=>");
				String key = splits[0];
				String val = splits[1];
				if(key.equals("pid"))
				{
					rowKey = Bytes.toBytes(val);
					put = new Put(rowKey);
				}
				else
				{
					put.add(this.columnFamily, Bytes.toBytes(key), Bytes.toBytes(val));
				}
			}
			//write to the output collector
			output.collect(new ImmutableBytesWritable(rowKey), put);
		}
	}
}
