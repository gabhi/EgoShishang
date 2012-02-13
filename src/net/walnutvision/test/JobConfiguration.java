package net.walnutvision.test;

import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class JobConfiguration extends Configured implements Tool{

	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf();
		for(Entry<String,String> entry: conf)
		{
			System.out.printf("%s=%s\n",entry.getKey(), entry.getValue());
		}
		return 0;
	}
	
	public static void main(String[] args)
	{
		try {
			int exitCode = ToolRunner.run(new JobConfiguration(), args);
			System.exit(exitCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
