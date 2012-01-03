package com.egoshishang.test;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class CustomInputOutFormat {
	public static class MyTextInputFormat extends FileInputFormat<LongWritable, Text>
	{
		@Override
		public RecordReader<LongWritable, Text> getRecordReader(
				InputSplit arg0, JobConf arg1, Reporter arg2)
				throws IOException {
			return new MyTextRecorder((FileSplit)arg0,arg1);
		}
		
	}
	public static class MyTextRecorder implements RecordReader<LongWritable,Text>
	{
		private FileSplit split = null;
		private JobConf conf = null;
		
		public MyTextRecorder(FileSplit split, JobConf conf)
		{
			this.split = split;
			 this.conf = conf;
		}
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public LongWritable createKey() {
			// TODO Auto-generated method stub
			return new LongWritable();
		}

		@Override
		public Text createValue() {
			// TODO Auto-generated method stub
			return new Text();
		}

		@Override
		public long getPos() throws IOException {
			// TODO Auto-generated method stub
			return this.getPos();
		}

		@Override
		public float getProgress() throws IOException {
			// TODO Auto-generated method stub
			return this.getPos()/split.getLength();
		}

		@Override
		public boolean next(LongWritable pos, Text line) throws IOException {
			// TODO Auto-generated method stub
			if(this.getPos() >= split.getLength())
				return false;
			pos.set(this.getPos());
			
			return true;
		}
		
	}
}
