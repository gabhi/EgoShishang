package com.egoshishang.sys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LocalFileImageIdAssigner extends ImageIdAssigner{

	protected String configFilePath = null;
	protected BufferedWriter idWriter = null;
	private LocalFileImageIdAssigner()
	{	
		
	}
	public static ImageIdAssigner getInstance()
	{
		if(ImageIdAssigner.assignerInst == null)
		{
			assignerInst = new LocalFileImageIdAssigner();
		}
		return assignerInst;
	}
	@Override
	public void init(String filePath)
	{
		this.configFilePath  = filePath;
		readId();
	}
	
	@Override
	public long nextId() {
		long curVal = this.id;
		this.id++;
		return curVal;
	}

	@Override
	public void readId() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.configFilePath));
			String line = br.readLine();
			if(line != null)
			{
				this.id = Long.valueOf(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	@Override
	public void writeId() {
		try {
			if(idWriter == null)
			{
				idWriter = new BufferedWriter(new FileWriter(this.configFilePath));				
			}
			idWriter.write(String.valueOf(this.id));
			idWriter.flush();
			idWriter.close();
			idWriter = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void finalize()
	{
		writeId();
	}
	
}
