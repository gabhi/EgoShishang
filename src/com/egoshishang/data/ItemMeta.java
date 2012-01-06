package com.egoshishang.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.io.Writable;

public class ItemMeta implements Serializable,Writable{
	private static final long serialVersionUID = 492130592168373580L;
	public String title = "";
	public String url = "";
	public String photoUrl = "";
	public String extra = "";
	public String asin = "";
	@Override
	public String toString()
	{
		return asin + "\t" + title + "\t" + url + "\t" + photoUrl;
	}
	@Override
	public void readFields(DataInput input) throws IOException {
		// TODO Auto-generated method stub
		title = input.readLine();
		url = input.readLine();
		photoUrl = input.readLine();
		extra = input.readLine();
	}
	@Override
	public void write(DataOutput output) throws IOException {
		// TODO Auto-generated method stub
		output.writeBytes(title);
		output.writeBytes(url);
		output.writeBytes(photoUrl);
		output.writeBytes(extra);
	}
}

