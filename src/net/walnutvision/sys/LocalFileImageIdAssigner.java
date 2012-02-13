package net.walnutvision.sys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LocalFileImageIdAssigner extends ImageIdAssigner{

	protected String configFilePath = null;
	protected RandomAccessFile idReaderWriter = null;
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
	public void setUp(String filePath)
	{
		this.configFilePath  = filePath;
		try {
			idReaderWriter = new RandomAccessFile(new File(configFilePath), "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readId();
	}
	
	@Override
	public long nextId() {
		long curVal;
		synchronized(this)
		{
			curVal = this.id;
			this.id++;
			writeId();
		}
		return curVal;
	}

	@Override
	public void readId() {
		try {
			idReaderWriter.seek(0);
			this.id = Long.valueOf(idReaderWriter.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void writeId() {
		try {
			idReaderWriter.seek(0);
			idReaderWriter.write(String.valueOf(this.id).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void tearDown() {
		try {
			this.idReaderWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
